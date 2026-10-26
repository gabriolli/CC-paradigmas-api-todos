(ns todo-api.handlers
  "Handlers HTTP: funções request (map) -> response (map).
   Não há objetos de contexto nem `return` antecipado: cada handler é uma
   única expressão que devolve um valor."
  (:require [todo-api.db :as db]
            [todo-api.http :as http]
            [todo-api.todos :as todos]))

;; ---------------------------------------------------------------------------
;; Função de alta ordem: recebe uma função (id -> resposta) e devolve um
;; handler completo que já valida o id da URL. Evita repetir a validação.
;; ---------------------------------------------------------------------------

(defn com-id [f]
  (fn [requisicao]
    (if-let [id (todos/ler-id (get-in requisicao [:params :id]))]
      (f id)
      (http/requisicao-invalida "ID inválido"))))

;; ---------------------------------------------------------------------------
;; Handlers
;; ---------------------------------------------------------------------------

(defn health
  "GET /health"
  [_requisicao]
  (http/ok {:status "ok" :paradigm "funcional"}))

(defn listar-todos
  "GET /todos"
  [_requisicao]
  (http/ok (db/listar)))

(defn criar-todo
  "POST /todos — a validação devolve dados ({:titulo} ou {:erro}),
   e a desestruturação decide o caminho."
  [{:keys [body]}]
  (let [{:keys [titulo erro]} (todos/validar-criacao body)]
    (if erro
      (http/requisicao-invalida erro)
      (http/criado (db/inserir! titulo)))))

;; O domínio devolve um Result ({:ok v} ou {:erro {:tipo ...}}). Esta função
;; traduz o ERRO em resposta HTTP com `case` sobre o tipo (pattern matching),
;; e o sucesso em resposta via a função recebida: sem nil, sem exceção.
(defn resposta-do-resultado [resultado ok->resposta]
  (if-let [erro (:erro resultado)]
    (case (:tipo erro)
      :nao-encontrada (http/nao-encontrado "Todo não encontrado"))
    (ok->resposta (:ok resultado))))

(def alternar-todo
  "PATCH /todos/:id/toggle"
  (com-id (fn [id]
            (resposta-do-resultado (db/alternar! id) http/ok))))

(def remover-todo
  "DELETE /todos/:id"
  (com-id (fn [id]
            (resposta-do-resultado
             (db/remover! id)
             (constantly (http/ok {:message "Todo removido com sucesso"}))))))
