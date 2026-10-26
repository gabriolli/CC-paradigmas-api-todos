(ns todo-api.db
  "Fronteira com o mundo externo: o ÚNICO estado mutável da aplicação.

   - O estado fica em um `atom`, que guarda um valor imutável.
   - Mudar o estado = aplicar uma função pura de `todo-api.todos` com `swap!`.
   - Gravar no disco é um efeito colateral isolado em um `add-watch`: a lógica
     de negócio nem sabe que existe um arquivo."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [todo-api.todos :as todos]))

(def arquivo "data/todos.json")

(defonce estado (atom todos/estado-vazio))

;; ---------------------------------------------------------------------------
;; Efeitos de I/O (impuros por natureza, mantidos pequenos e isolados)
;; ---------------------------------------------------------------------------

(defn- ler-arquivo
  "Lê a lista de todos do JSON. Arquivo ausente, vazio ou inválido vira []."
  [caminho]
  (let [f (io/file caminho)]
    (if-not (.exists f)
      []
      (let [conteudo (slurp f :encoding "UTF-8")]
        (if (str/blank? conteudo)
          []
          (try
            (json/read-str conteudo :key-fn keyword)
            (catch Exception e
              (binding [*out* *err*]
                (println (str "Aviso: falha ao ler " caminho ": "
                              (.getMessage e) ". Iniciando vazio.")))
              [])))))))

(defn- gravar-arquivo! [caminho todos]
  (io/make-parents caminho)
  (spit caminho
        (json/write-str todos :indent true :escape-unicode false)
        :encoding "UTF-8"))

(defn- persistir!
  "Watcher do atom: chamado após toda mudança. Só grava se a lista mudou."
  [_chave _ref antigo novo]
  (when (not= (:todos antigo) (:todos novo))
    (gravar-arquivo! arquivo (:todos novo))))

(defn iniciar!
  "Carrega o arquivo, popula o atom e liga a persistência automática."
  []
  (reset! estado (todos/estado-inicial (ler-arquivo arquivo)))
  (add-watch estado ::persistencia persistir!)
  (let [{lista :todos id :proximo-id} @estado]
    (println (str "DB local carregado: " (count lista)
                  " todo(s), próximo ID = " id))))

;; ---------------------------------------------------------------------------
;; Operações: cada uma é `swap!` + função pura do domínio
;; ---------------------------------------------------------------------------

(defn listar []
  (:todos @estado))

(defn inserir!
  "Retorna o todo criado. `swap!` devolve exatamente o estado que ELA gerou."
  [titulo]
  (todos/ultimo-criado (swap! estado todos/adicionar titulo)))

(defn- transicionar!
  "Aplica no atom uma transição pura que devolve Result ({:ok estado} ou
   {:erro ...}). Em caso de erro o estado fica como está (e, como a lista não
   muda, nada é regravado no disco).

   `swap-vals!` devolve [antes depois]. Como a transição é pura, reaplicá-la
   ao `antes` reproduz exatamente o Result que o atom calculou."
  [transicao & args]
  (let [[antes _depois] (swap-vals! estado
                                    (fn [atual]
                                      (get (apply transicao atual args) :ok atual)))]
    (apply transicao antes args)))

(defn alternar!
  "{:ok todo-alterado} ou {:erro {:tipo :nao-encontrada :id id}}."
  [id]
  (-> (transicionar! todos/alternar id)
      (todos/mapear-ok #(todos/buscar % id))))

(defn remover!
  "{:ok true} ou {:erro {:tipo :nao-encontrada :id id}}."
  [id]
  (-> (transicionar! todos/remover id)
      (todos/mapear-ok (constantly true))))
