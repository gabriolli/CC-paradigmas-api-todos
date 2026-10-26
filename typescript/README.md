# Todo API em TypeScript (Paradigma POO)

API REST de tarefas feita para mostrar orientação a objetos em TypeScript. Não usa framework web: o servidor é o `node:http`, no mesmo papel do `HttpServer` do JDK. O estado da tarefa fica no objeto `Todo`. Quem muda esse estado é um método da própria classe.

As tarefas são gravadas em `data/todos.json`, como nas versões em Rust e Clojure. O `TodoService` mantém a lista em memória e, a cada criação, alternância ou remoção, pede a um `RepositorioTodos` que salve. Ao reiniciar, o estado volta do arquivo. O `data/todos.json` já vem com 3 tarefas de demonstração (seed).

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

### `GET /health`

Checagem do servidor.

- **200:** `{ "status": "ok", "paradigm": "poo" }`

### `GET /todos`

Lista as tarefas. Se não houver nenhuma, devolve `[]`.

### `POST /todos`

Cria uma tarefa.

- **Body:** `{ "title": "Preparar apresentação" }`
- **201:** a tarefa criada, com `completed: false`
- **400:** JSON inválido, campo `title` ausente, `title` que não é texto, ou título em branco

### `PATCH /todos/:id/toggle`

Localiza a tarefa e chama `toggle()` no objeto, que inverte `completed`: pendente vira concluída e concluída volta a pendente. É o mesmo contrato das APIs em Rust e Clojure, então o mesmo frontend funciona com as três.

- **200:** a tarefa com `completed` já invertido
- **404:** id inexistente

### `DELETE /todos/:id`

Remove a tarefa.

- **200:** `{ "message": "Todo removido com sucesso" }`
- **404:** id inexistente. Vale também para um segundo DELETE do mesmo id

---

## Onde a POO aparece

### Encapsulamento — `src/Todo.ts`

`id`, `title` e `completed` são `private`. O estado só muda por método da classe. `complete()` liga `completed` e `toggle()` o inverte.

### Herança — `src/TodoNotFoundException.ts` e `src/InvalidTitleException.ts`

As duas estendem `Error` e reaproveitam mensagem e `throw`/`catch`. O servidor traduz `InvalidTitleException` em HTTP 400 e `TodoNotFoundException` em HTTP 404.

### Polimorfismo — `src/ManipuladorHttp.ts` e `src/ApiServer.ts`

`ApiServer.registrar` aceita qualquer objeto que implemente `ManipuladorHttp`. Cada rota é uma classe com o seu próprio `tratar()`:

- `ManipuladorListarTodos`
- `ManipuladorCriarTodo`
- `ManipuladorAlternarTodo`
- `ManipuladorRemoverTodo`

O servidor percorre a lista e chama `tratar()` de quem corresponder ao método e ao caminho. A interface é a mesma. O comportamento de cada classe é outro.

### Abstração e injeção de dependência — `src/RepositorioTodos.ts`

`TodoService` depende da interface `RepositorioTodos` (`carregar()` e `salvar()`), não de um arquivo. Quem escolhe a implementação é quem cria o serviço:

- `RepositorioArquivoJson` grava em `data/todos.json`. É o que `Main.ts` usa.
- `RepositorioMemoria` não persiste nada. É o padrão quando o serviço é criado sem argumento, e é o que os testes de serviço e de API usam.

Trocar o destino dos dados (por exemplo, para um banco) é criar outra classe que implemente a interface, sem mexer no `TodoService`.

### Fluxo do PATCH

1. `ApiServer` escolhe `ManipuladorAlternarTodo`.
2. O manipulador pede `TodoService.toggle(id)`.
3. O serviço acha o objeto com `findById(id)`.
4. O objeto executa `todo.toggle()`, e só ele altera o próprio `completed`.
5. O serviço pede ao repositório que salve a lista.
