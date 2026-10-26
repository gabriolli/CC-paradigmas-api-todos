(ns todo-api.routes
  "Rotas como DADOS: a tabela é um vetor comum, e o roteamento é feito por
   funções puras que casam a URL com cada padrão."
  (:require [clojure.string :as str]
            [todo-api.handlers :as h]
            [todo-api.http :as http]))

;; [método  padrão-do-caminho  handler]
;; Strings precisam ser iguais; keywords capturam o segmento como parâmetro.
(def rotas
  [[:get    ["health"]             h/health]
   [:get    ["todos"]              h/listar-todos]
   [:post   ["todos"]              h/criar-todo]
   [:patch  ["todos" :id "toggle"] h/alternar-todo]
   [:delete ["todos" :id]          h/remover-todo]])

;; ---------------------------------------------------------------------------
;; Casamento de caminho (puro)
;; ---------------------------------------------------------------------------

(defn segmentos
  "\"/todos/3/toggle\" -> (\"todos\" \"3\" \"toggle\")"
  [uri]
  (remove str/blank? (str/split uri #"/")))

(defn- casar-segmento
  "Devolve os params acumulados, ou nil se o segmento não casar."
  [params [padrao valor]]
  (cond
    (keyword? padrao) (assoc params padrao valor)
    (= padrao valor)  params
    :else             nil))

(defn casar-caminho
  "Params capturados (ex.: {:id \"3\"}) se o caminho casar; senão nil.
   `reduced` interrompe o `reduce` no primeiro segmento que falhar."
  [padrao uri]
  (let [partes (segmentos uri)]
    (when (= (count padrao) (count partes))
      (reduce (fn [params par]
                (or (casar-segmento params par) (reduced nil)))
              {}
              (map vector padrao partes)))))

(defn- casar-rota [uri [metodo padrao handler]]
  (when-let [params (casar-caminho padrao uri)]
    {:metodo metodo :params params :handler handler}))

;; ---------------------------------------------------------------------------
;; Roteador: função de alta ordem que recebe a tabela e devolve um handler
;; ---------------------------------------------------------------------------

(defn rotear [tabela]
  (fn [{:keys [uri request-method] :as requisicao}]
    (let [candidatas (keep (partial casar-rota uri) tabela)
          escolhida  (first (filter #(= request-method (:metodo %)) candidatas))]
      (cond
        escolhida
        ((:handler escolhida) (assoc requisicao :params (:params escolhida)))

        (seq candidatas)
        (http/metodo-nao-permitido "Método não permitido")

        :else
        (http/nao-encontrado "Rota não encontrada")))))
