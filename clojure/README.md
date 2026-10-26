# Todo API em Clojure (Paradigma Funcional)

Esta é a mesma API de tarefas das versões em Rust e TypeScript, agora construída para demonstrar o **paradigma funcional** com Clojure, usando [Ring](https://github.com/ring-clojure/ring) e Jetty. Não há framework de rotas: o roteador e os middlewares foram escritos à mão para deixar o paradigma visível.

A lógica de negócio é feita só de **funções puras** sobre **dados imutáveis**. O único estado mutável da aplicação é um `atom`, que muda aplicando essas funções puras. A gravação no arquivo JSON é um efeito colateral isolado, que fica fora da lógica.

## Como Executar

### 1. Pré-requisitos
- Java 8 ou superior.
- [Leiningen](https://leiningen.org/) no PATH (`lein version` deve funcionar).
  - **Com Java 8, use o Leiningen 2.11.2.** O 2.12 e versões mais novas exigem um Java recente. No Windows, basta baixar o [`lein.bat` 2.11.2](https://codeberg.org/leiningen/leiningen/raw/tag/2.11.2/bin/lein.bat) para uma pasta do PATH e rodar `lein self-install`.

### 2. Preparação
- A base de dados (`data/todos.json`) já vem populada com tarefas iniciais de demonstração (seed).
- As dependências são baixadas automaticamente na primeira execução.

### 3. Rodando o Servidor
1. Abra um terminal na pasta da API (`clojure`):
   ```bash
   cd clojure
   ```
2. Execute:
   ```bash
   lein run
   ```
3. Ao concluir, você verá no terminal:
   ```
   DB local carregado: 3 todo(s), próximo ID = 4
   Servidor funcional (Ring + Jetty) escutando em http://127.0.0.1:5000
   ```

Para usar outro host ou porta, defina `HOST` e/ou `PORT` antes de rodar (ex.: `PORT=5001 lein run`). As APIs em Rust e TypeScript também usam a porta 5000, então suba apenas uma delas por vez nessa porta.

### 4. Testes
```bash
lein test
```
Os testes cobrem os cenários do roteiro da disciplina: `pendentes` só com a tarefa 1, `concluir` gera uma nova versão e preserva a entrada, `concluir` de novo dá o mesmo estado, `remover` gera nova versão sem alterar a entrada, e ID ausente devolve `{:erro {:tipo :nao-encontrada :id 99}}`.

> `concluir` e `pendentes` fazem parte do domínio puro e são testadas, mas a API só expõe `alternar` (o `PATCH .../toggle`, igual ao Rust).

---

## Estrutura

```
clojure/
├── project.clj            ; dependências e ponto de entrada
├── data/todos.json        ; "banco de dados" local
├── src/todo_api/
│   ├── core.clj           ; -main: configuração + servidor
│   ├── todos.clj          ; domínio: SOMENTE funções puras
│   ├── db.clj             ; atom (único estado mutável) + persistência
│   ├── http.clj           ; respostas HTTP como dados
│   ├── handlers.clj       ; request (map) -> response (map)
│   ├── routes.clj         ; rotas como dados + roteador
│   └── middleware.clj     ; JSON, CORS e erros como funções de alta ordem
└── test/todo_api/
    └── todos_test.clj     ; testes das funções puras
```

## Onde o paradigma funcional aparece

| Conceito | Onde |
|---|---|
| **Funções puras** | `todos.clj`: `adicionar`, `alternar`, `concluir`, `remover`, `pendentes`, `buscar`, `validar-criacao` e `ler-id` só recebem dados e devolvem dados. Nenhuma faz I/O. |
| **Imutabilidade** | As tarefas são um vetor de maps. Nada é alterado no lugar: `assoc`, `update`, `conj`, `mapv` e `filterv` sempre devolvem valores **novos**. Os testes conferem que o estado original continua igual. |
| **Estado isolado** | `db.clj`: um único `atom`. Toda mudança é `(swap! estado funcao-pura arg)`. É o contraponto ao `Mutex` + `iter_mut` da versão em Rust. |
| **Efeitos colaterais na borda** | A gravação no JSON acontece em um `add-watch` do atom. A lógica de negócio não sabe que o arquivo existe. |
| **Funções de alta ordem** | `map`, `filter`, `reduce`, `some` e `keep` no lugar de laços `for`. `com-id` (handlers) e os middlewares recebem uma função e devolvem outra. |
| **Composição** | Pipelines com `->`, `->>` e `some->>`. A pilha de middlewares é `(comp wrap-json-response wrap-cors wrap-erros wrap-json-body)`. |
| **Aplicação parcial** | `(def ok (partial resposta 200))` e `(partial tem-id? id)`. |
| **Código como dados** | As rotas são um vetor `[método padrão handler]`, casado por funções puras (`casar-caminho` usa `reduce` + `reduced`). |
| **Recursão** | `maior-id` em `todos.clj` usa `loop`/`recur` (recursão de cauda, não consome pilha) no lugar do `for` com `max_id` mutável do Rust. |
| **Result (ausência explícita)** | `alternar`, `concluir` e `remover` devolvem `{:ok estado}` ou `{:erro {:tipo :nao-encontrada :id 99}}`. A falha faz parte do contrato da função. O handler traduz o erro em 404 com `case` sobre o tipo (pattern matching). |
| **Idempotência** | `concluir` marca como concluída: chamar de novo dá o mesmo estado. `alternar` é o que o endpoint expõe e volta ao original na segunda chamada. |
| **Erros como valores** | A validação devolve `{:titulo t}` ou `{:erro msg}`, e JSON inválido vira `::invalido`, em vez de exceções circulando pela lógica. |
| **A aplicação é uma função** | `app` em `core.clj` é uma única função `request -> response`, resultado da composição dos middlewares com o roteador. |

---

## Endpoints Disponíveis

### `GET /health`
- **Descrição:** Checagem do servidor.
- **Status de Sucesso:** `200 OK`
- **Exemplo de Resposta:** `{ "status": "ok", "paradigm": "funcional" }`

### `GET /todos`
- **Descrição:** Lista todas as tarefas do banco de dados local.
- **Status de Sucesso:** `200 OK` (retorna o array de objetos. Se vazio, retorna `[]`).

### `POST /todos`
- **Descrição:** Cria uma nova tarefa.
- **Body Esperado:** `{ "title": "Estudar Clojure" }`
- **Status de Sucesso:** `201 CREATED` (retorna a tarefa criada com o respectivo `id`).
- **Status de Erro:** `400 BAD REQUEST` (título ausente, em branco ou que não é texto, ou JSON inválido).

### `PATCH /todos/:id/toggle`
- **Descrição:** Inverte o estado `completed` da tarefa.
- **Status de Sucesso:** `200 OK` (retorna a tarefa já com o campo alterado).
- **Status de Erro:** `404 NOT FOUND` (ID não encontrado) ou `400 BAD REQUEST` (ID não é um inteiro positivo).

### `DELETE /todos/:id`
- **Descrição:** Remove uma tarefa pelo ID.
- **Status de Sucesso:** `200 OK` (retorna `{ "message": "Todo removido com sucesso" }`).
- **Status de Erro:** `404 NOT FOUND` (tarefa inexistente) ou `400 BAD REQUEST` (ID inválido).

Rotas inexistentes retornam `404` e métodos não suportados em uma rota existente retornam `405`.
