(ns todo-api.middleware
  "Middlewares: funções de ALTA ORDEM. Cada uma recebe um handler e devolve
   um novo handler que acrescenta um comportamento. A pilha final é montada
   por composição de funções (`comp`)."
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [todo-api.http :as http]))

;; ---------------------------------------------------------------------------
;; JSON de entrada
;; ---------------------------------------------------------------------------

(defn- ler-json
  "Corpo vazio vira nil; JSON inválido vira ::invalido (um valor, não exceção)."
  [stream]
  (let [texto (some-> stream (slurp :encoding "UTF-8"))]
    (if (str/blank? texto)
      nil
      (try
        (json/read-str texto :key-fn keyword)
        (catch Exception _ ::invalido)))))

(defn wrap-json-body [handler]
  (fn [requisicao]
    (let [corpo (ler-json (:body requisicao))]
      (if (= corpo ::invalido)
        (http/requisicao-invalida "JSON inválido")
        (handler (assoc requisicao :body corpo))))))

;; ---------------------------------------------------------------------------
;; JSON de saída
;; ---------------------------------------------------------------------------

(defn- serializar [resposta]
  (-> resposta
      (update :body json/write-str :escape-unicode false)
      (assoc-in [:headers "Content-Type"] "application/json; charset=utf-8")))

(defn wrap-json-response [handler]
  (fn [requisicao]
    (let [resposta (handler requisicao)]
      (cond-> resposta
        (some? (:body resposta)) serializar))))

;; ---------------------------------------------------------------------------
;; CORS (mesma configuração da API em Rust)
;; ---------------------------------------------------------------------------

(def cabecalhos-cors
  {"Access-Control-Allow-Origin"  "*"
   "Access-Control-Allow-Methods" "GET, POST, PATCH, DELETE"
   "Access-Control-Allow-Headers" "Content-Type"})

(defn wrap-cors [handler]
  (fn [requisicao]
    (-> (if (= :options (:request-method requisicao))
          (http/sem-conteudo)
          (handler requisicao))
        (update :headers merge cabecalhos-cors))))

;; ---------------------------------------------------------------------------
;; Erros inesperados viram resposta 500 em vez de derrubar a requisição
;; ---------------------------------------------------------------------------

(defn wrap-erros [handler]
  (fn [requisicao]
    (try
      (handler requisicao)
      (catch Exception e
        (binding [*out* *err*]
          (println "Erro interno:" (.getMessage e)))
        (http/erro-interno "Erro interno do servidor")))))

;; ---------------------------------------------------------------------------
;; Composição: (pilha h) = (wrap-json-response (wrap-cors (wrap-erros (wrap-json-body h))))
;; ---------------------------------------------------------------------------

(def pilha
  (comp wrap-json-response
        wrap-cors
        wrap-erros
        wrap-json-body))
