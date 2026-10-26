(defproject todo-api "0.1.0"
  :description "Todo API em Clojure (Paradigma Funcional)"
  :dependencies [[org.clojure/clojure "1.11.4"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [org.clojure/data.json "2.5.0"]]
  ;; Silencia os logs INFO do Jetty, deixando só as mensagens da API.
  :jvm-opts ["-Dorg.eclipse.jetty.LEVEL=WARN"]
  :main todo-api.core
  :profiles {:uberjar {:aot :all}})
