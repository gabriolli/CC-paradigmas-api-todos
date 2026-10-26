(ns todo-api.core
  "Ponto de entrada: lê a configuração, monta a aplicação e sobe o servidor."
  (:require [ring.adapter.jetty :as jetty]
            [todo-api.db :as db]
            [todo-api.middleware :as middleware]
            [todo-api.routes :as routes])
  (:gen-class))

(defn configuracao
  "Função pura: recebe as variáveis de ambiente (um map) e devolve a config.
   Valores ausentes ou inválidos caem no padrão, sem `if` encadeados."
  [env]
  {:host (get env "HOST" "127.0.0.1")
   :port (or (some-> (get env "PORT") parse-long) 5000)})

;; A aplicação inteira é UMA função: request -> response.
;; Ela nasce da composição dos middlewares aplicada ao roteador.
(def app
  (middleware/pilha (routes/rotear routes/rotas)))

(defn -main [& _args]
  (let [{:keys [host port]} (configuracao (into {} (System/getenv)))]
    (db/iniciar!)
    (println (str "Servidor funcional (Ring + Jetty) escutando em http://"
                  host ":" port))
    (jetty/run-jetty app {:host host :port port :join? true})))
