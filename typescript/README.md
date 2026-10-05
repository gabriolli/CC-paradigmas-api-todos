# Todo API em TypeScript (Paradigma POO)

API REST de tarefas feita para mostrar orientação a objetos em TypeScript. Não usa framework web: o servidor é o `node:http`, no mesmo papel do `HttpServer` do JDK. O estado da tarefa fica no objeto `Todo`. Quem muda esse estado é um método da própria classe.

A lista vive na memória, dentro de `TodoService`. Ao reiniciar o processo, ela volta vazia.

## Como executar

### 1. Pré-requisitos

- [Node.js](https://nodejs.org/) 22 ou superior.

### 2. Preparação

Na pasta `typescript`:

```bash
cd typescript
npm install
npm run build
```

### 3. Rodando o servidor

```bash
npm start
```

A API sobe em `http://127.0.0.1:5000`. Para outra porta:

```bash
PORT=5001 npm start
```

A API em Rust deste repositório também usa a porta 5000. Suba só uma das duas nessa porta, ou mude `PORT` aqui.

### 4. Testes

```bash
npm test
```

---

## Endpoints

### `GET /todos`

Lista as tarefas. Se não houver nenhuma, devolve `[]`.

### `POST /todos`

Cria uma tarefa.

- **Body:** `{ "title": "Preparar apresentação" }`
- **201:** a tarefa criada, com `completed: false`
- **400:** JSON inválido, campo `title` ausente, `title` que não é texto, ou título em branco

### `PATCH /todos/:id/complete`

Localiza a tarefa e chama `complete()` no objeto. A chamada marca `completed` como `true`. Repetir a chamada mantém `true`.

- **200:** a tarefa concluída
- **404:** id inexistente

### `DELETE /todos/:id`

Remove a tarefa.

- **200:** `{ "message": "Todo removido com sucesso" }`
- **404:** id inexistente. Vale também para um segundo DELETE do mesmo id

---

## Onde a POO aparece

### Encapsulamento — `src/Todo.ts`

`id`, `title` e `completed` são `private`. O estado só muda por método da classe. `complete()` é quem liga `completed`.

### Herança — `src/TodoNotFoundException.ts` e `src/InvalidTitleException.ts`

As duas estendem `Error` e reaproveitam mensagem e `throw`/`catch`. O servidor traduz `InvalidTitleException` em HTTP 400 e `TodoNotFoundException` em HTTP 404.

### Polimorfismo — `src/ManipuladorHttp.ts` e `src/ApiServer.ts`

`ApiServer.registrar` aceita qualquer objeto que implemente `ManipuladorHttp`. Cada rota é uma classe com o seu próprio `tratar()`:

- `ManipuladorListarTodos`
- `ManipuladorCriarTodo`
- `ManipuladorConcluirTodo`
- `ManipuladorRemoverTodo`

O servidor percorre a lista e chama `tratar()` de quem corresponder ao método e ao caminho. A interface é a mesma. O comportamento de cada classe é outro.

### Fluxo do PATCH

1. `ApiServer` escolhe `ManipuladorConcluirTodo`.
2. O manipulador pede `TodoService.complete(id)`.
3. O serviço acha o objeto com `findById(id)`.
4. O objeto executa `todo.complete()`.
