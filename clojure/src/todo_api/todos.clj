(ns todo-api.todos
  "Domínio da aplicação: SOMENTE funções puras.

   Nenhuma função deste arquivo lê arquivo, acessa rede ou altera estado.
   Todas recebem dados imutáveis e devolvem dados NOVOS; o valor de entrada
   nunca é modificado. Por isso podem ser testadas sem mock nenhum.

   O estado da aplicação é um map imutável:
     {:todos      [{:id 1 :title \"...\" :completed false} ...]
      :proximo-id 4}"
  (:require [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; Construção do estado
;; ---------------------------------------------------------------------------

(def estado-vazio
  {:todos [] :proximo-id 1})

(defn maior-id
  "Maior id da lista, ou 0 se vazia. RECURSÃO DE CAUDA: a chamada `recur` é a
   última coisa feita, então não consome pilha. O acumulador `maximo` é um
   parâmetro novo a cada volta; nenhuma variável é alterada (compare com o
   `let mut max_id` do Rust)."
  [todos]
  (loop [[todo & resto] (seq todos)
         maximo         0]
    (if todo
      (recur resto (max maximo (:id todo)))
      maximo)))

(defn proximo-id [todos]
  (inc (maior-id todos)))

(defn estado-inicial
  "Monta o estado a partir de uma coleção de todos já carregada."
  [todos]
  {:todos      (vec todos)
   :proximo-id (proximo-id todos)})

(defn novo-todo [id titulo]
  {:id id :title titulo :completed false})

;; ---------------------------------------------------------------------------
;; Validação: devolve dados descrevendo o resultado, em vez de lançar exceção
;; ---------------------------------------------------------------------------

(defn titulo-valido? [titulo]
  (and (string? titulo)
       (not (str/blank? titulo))))

(defn validar-criacao
  "Recebe o corpo da requisição e devolve {:titulo t} ou {:erro msg}."
  [corpo]
  (let [titulo (get corpo :title)]
    (if (titulo-valido? titulo)
      {:titulo (str/trim titulo)}
      {:erro "Título não pode ser vazio"})))

(defn- positivo [n]
  (when (pos? n) n))

(defn ler-id
  "Converte o texto da URL em um id positivo, ou nil se for inválido.
   `some->>` interrompe o pipeline no primeiro nil: sem `if` encadeados."
  [texto]
  (some->> texto
           (re-matches #"\d+")
           parse-long
           positivo))

;; ---------------------------------------------------------------------------
;; Consultas
;; ---------------------------------------------------------------------------

(defn tem-id? [id todo]
  (= id (:id todo)))

(defn buscar
  "Primeiro todo com o id informado, ou nil."
  [estado id]
  (->> (:todos estado)
       (filter (partial tem-id? id))
       first))

(defn ultimo-criado [estado]
  (peek (:todos estado)))

(defn pendentes
  "Só os todos ainda não concluídos. `remove` cria uma coleção nova; a original
   permanece intacta."
  [estado]
  (->> (:todos estado)
       (remove :completed)
       vec))

;; ---------------------------------------------------------------------------
;; Result: a falha é um VALOR no contrato da função, não uma exceção nem um nil
;; solto. Toda transição que pode falhar devolve um de dois formatos:
;;   {:ok   <valor>}
;;   {:erro {:tipo :nao-encontrada :id 99}}
;; ---------------------------------------------------------------------------

(defn ok [valor]
  {:ok valor})

(defn nao-encontrada [id]
  {:erro {:tipo :nao-encontrada :id id}})

(defn mapear-ok
  "Aplica `f` ao valor de um {:ok ...}; um {:erro ...} passa adiante intacto."
  [resultado f]
  (if (contains? resultado :ok)
    (update resultado :ok f)
    resultado))

;; ---------------------------------------------------------------------------
;; Transições de estado: (estado, argumento) -> novo estado
;; São estas funções que o atom aplica em `db.clj` via `swap!`.
;; ---------------------------------------------------------------------------

(defn adicionar
  "Novo estado com o todo incluído e o contador avançado.
   Não pode falhar (a validação do título é feita antes), logo não usa Result."
  [estado titulo]
  (let [id (:proximo-id estado)]
    (-> estado
        (update :todos conj (novo-todo id titulo))
        (update :proximo-id inc))))

(defn- atualizar-todo
  "Aplica `f` ao todo do id informado. {:ok novo-estado} ou {:erro ...}."
  [estado id f]
  (if (buscar estado id)
    (ok (update estado :todos
                (partial mapv #(if (tem-id? id %) (f %) %))))
    (nao-encontrada id)))

(defn alternar
  "Inverte :completed. Chamar duas vezes devolve o estado original."
  [estado id]
  (atualizar-todo estado id #(update % :completed not)))

(defn concluir
  "Marca como concluído. IDEMPOTENTE: concluir de novo dá o mesmo estado."
  [estado id]
  (atualizar-todo estado id #(assoc % :completed true)))

(defn remover
  "Novo estado sem o todo do id informado."
  [estado id]
  (if (buscar estado id)
    (ok (update estado :todos
                (partial filterv (complement (partial tem-id? id)))))
    (nao-encontrada id)))
