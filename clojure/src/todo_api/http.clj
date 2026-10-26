(ns todo-api.http
  "Respostas HTTP como dados. Uma resposta Ring é só um map:
   {:status 200 :headers {} :body ...}")

(defn resposta [status corpo]
  {:status status :headers {} :body corpo})

;; Aplicação parcial: cada função abaixo é `resposta` com o status já fixado.
(def ok          (partial resposta 200))
(def criado      (partial resposta 201))
(def sem-conteudo (partial resposta 204 nil))

(defn erro [status mensagem]
  (resposta status {:error mensagem}))

(def requisicao-invalida (partial erro 400))
(def nao-encontrado      (partial erro 404))
(def metodo-nao-permitido (partial erro 405))
(def erro-interno        (partial erro 500))
