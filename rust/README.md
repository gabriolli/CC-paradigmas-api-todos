# Todo API em Rust (Paradigma Imperativo)

Esta é a API construída para demonstrar a aplicação prática do **paradigma imperativo** utilizando Rust (e o framework web Axum). A lógica foi desenhada de forma deliberadamente explícita: variáveis mutáveis (`let mut`), controle de fluxo passo a passo (`if`/`else` ao invés de *pattern matching*) e armazenamento local dinâmico via arquivo JSON.

## Como Executar

### 1. Pré-requisitos
- [Rust & Cargo](https://rustup.rs/) instalados nativamente na sua máquina.

### 2. Preparação
- Todas as variáveis de ambiente necessárias já estão no `.env` versionado no projeto, configurando a porta automaticamente.
- A base de dados (`data/todos.json`) já vem populada com tarefas iniciais de demonstração (seed).

### 3. Rodando o Servidor
1. Abra um terminal na pasta da API (`rust`):
   ```bash
   cd rust
   ```
2. Digite o comando de execução:
   ```bash
   cargo run
   ```
3. Aguarde o Cargo compilar o projeto pela primeira vez. Ao concluir, a API subirá e você verá no terminal a confirmação:
   ```
   DB local carregado: 3 todo(s), próximo ID = 4
   Servidor imperativo (Axum) escutando em http://127.0.0.1:5000
   ```

---

## Endpoints Disponíveis

### `GET /health`
- **Descrição:** Endpoint de checagem do servidor e verificação de leitura do banco de dados (JSON).
- **Status de Sucesso:** `200 OK`
- **Exemplo de Resposta:** `{ "status": "ok", "paradigm": "100% imperativo explícito" }`

### `GET /todos`
- **Descrição:** Lista todas as tarefas do banco de dados local.
- **Status de Sucesso:** `200 OK` (retorna o array de objetos. Se vazio, retorna `[]`).

### `POST /todos`
- **Descrição:** Cria uma nova tarefa.
- **Body Esperado:** `{ "title": "Estudar Rust" }`
- **Status de Sucesso:** `201 CREATED` (retorna a tarefa criada com o respectivo `id`).
- **Status de Erro:** `400 BAD REQUEST` (se o título for enviado em branco).

### `PATCH /todos/:id/toggle`
- **Descrição:** Inverte automaticamente o estado `completed` da tarefa (de *true* para *false*, ou vice-versa). 
- **Status de Sucesso:** `200 OK` (retorna a tarefa já com o campo alterado).
- **Status de Erro:** `404 NOT FOUND` (ID não foi encontrado no arquivo) ou `400 BAD REQUEST` (ID não é válido).

### `DELETE /todos/:id`
- **Descrição:** Deleta fisicamente uma tarefa do arquivo baseado no ID informado.
- **Status de Sucesso:** `200 OK` (retorna `{ "message": "Todo removido com sucesso" }`).
- **Status de Erro:** `404 NOT FOUND` (tarefa inexistente).
