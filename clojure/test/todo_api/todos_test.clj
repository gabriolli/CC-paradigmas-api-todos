(ns todo-api.todos-test
  "Funções puras se testam só com dados: sem mock, sem banco, sem servidor.
   Seguindo o roteiro: não basta testar o retorno, testa-se também o valor
   que entrou (preservação)."
  (:require [clojure.test :refer [deftest is testing]]
            [todo-api.routes :as routes]
            [todo-api.todos :as todos]))

(def exemplo
  (todos/estado-inicial
   [{:id 1 :title "Primeira" :completed false}
    {:id 2 :title "Segunda"  :completed true}]))

(defn- estado-de [resultado]
  (:ok resultado))

;; --- Recursão -------------------------------------------------------------

(deftest maior-id-test
  (is (= 0 (todos/maior-id [])))
  (is (= 9 (todos/maior-id [{:id 3} {:id 9} {:id 5}])))
  (testing "recursão de cauda: lista grande não estoura a pilha"
    (is (= 100000 (todos/maior-id (map (fn [n] {:id n}) (range 1 100001)))))))

(deftest proximo-id-test
  (is (= 1 (todos/proximo-id [])))
  (is (= 3 (:proximo-id exemplo))))

;; --- Coleções: map / filter sem efeitos escondidos -----------------------

(deftest pendentes-test
  (testing "devolve só a tarefa 1"
    (is (= [{:id 1 :title "Primeira" :completed false}]
           (todos/pendentes exemplo))))
  (testing "a entrada continua com as duas tarefas"
    (is (= 2 (count (:todos exemplo))))))

;; --- Adicionar -------------------------------------------------------------

(deftest adicionar-test
  (let [novo (todos/adicionar exemplo "Terceira")]
    (is (= {:id 3 :title "Terceira" :completed false} (todos/ultimo-criado novo)))
    (is (= 4 (:proximo-id novo)))
    (testing "o estado original não muda (imutabilidade)"
      (is (= 2 (count (:todos exemplo))))
      (is (= 3 (:proximo-id exemplo))))))

;; --- Concluir / alternar ---------------------------------------------------

(deftest concluir-test
  (let [resultado (todos/concluir exemplo 1)
        novo      (estado-de resultado)]
    (testing "nova versão: 1 concluída"
      (is (true? (:completed (todos/buscar novo 1)))))
    (testing "a entrada mantém 1 pendente"
      (is (false? (:completed (todos/buscar exemplo 1)))))
    (testing "concluir de novo: estado final igual ao da primeira conclusão"
      (is (= novo (estado-de (todos/concluir novo 1)))))))

(deftest alternar-test
  (let [novo (estado-de (todos/alternar exemplo 1))]
    (is (true? (:completed (todos/buscar novo 1))))
    (is (false? (:completed (todos/buscar exemplo 1))))
    (testing "alternar duas vezes volta ao estado original"
      (is (= exemplo (estado-de (todos/alternar novo 1)))))))

;; --- Remover ---------------------------------------------------------------

(deftest remover-test
  (let [novo (estado-de (todos/remover exemplo 1))]
    (testing "nova versão sem a tarefa 1"
      (is (nil? (todos/buscar novo 1)))
      (is (= 1 (count (:todos novo)))))
    (testing "a entrada permanece intacta"
      (is (some? (todos/buscar exemplo 1)))
      (is (= 2 (count (:todos exemplo)))))))

;; --- Ausência explícita (Result) ------------------------------------------

(deftest id-ausente-test
  (let [erro {:erro {:tipo :nao-encontrada :id 99}}]
    (is (= erro (todos/concluir exemplo 99)))
    (is (= erro (todos/alternar exemplo 99)))
    (is (= erro (todos/remover exemplo 99)))))

(deftest mapear-ok-test
  (is (= {:ok 2} (todos/mapear-ok {:ok 1} inc)))
  (testing "um erro passa adiante sem executar a função"
    (is (= (todos/nao-encontrada 7)
           (todos/mapear-ok (todos/nao-encontrada 7) inc)))))

;; --- Validação e rotas -----------------------------------------------------

(deftest validar-criacao-test
  (is (= {:titulo "Estudar"} (todos/validar-criacao {:title "  Estudar "})))
  (is (contains? (todos/validar-criacao {:title "   "}) :erro))
  (is (contains? (todos/validar-criacao {:title 42}) :erro))
  (is (contains? (todos/validar-criacao {}) :erro))
  (is (contains? (todos/validar-criacao nil) :erro)))

(deftest ler-id-test
  (is (= 3 (todos/ler-id "3")))
  (is (nil? (todos/ler-id "0")))
  (is (nil? (todos/ler-id "abc")))
  (is (nil? (todos/ler-id "-1")))
  (is (nil? (todos/ler-id nil))))

(deftest casar-caminho-test
  (is (= {} (routes/casar-caminho ["todos"] "/todos")))
  (is (= {:id "3"} (routes/casar-caminho ["todos" :id "toggle"] "/todos/3/toggle")))
  (is (nil? (routes/casar-caminho ["todos" :id "toggle"] "/todos/3")))
  (is (nil? (routes/casar-caminho ["health"] "/todos"))))
