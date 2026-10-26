# Paradigmas de Programação - Repositório Acadêmico

Este repositório agrupa os projetos criados durante o estudo prático dos diversos paradigmas de linguagem de programação.

## Projetos Atuais

* **[Rust (API Imperativa)](./rust/README.md)**
  * Uma API (To-Do List) totalmente desenhada com controle de fluxo imperativo explícito (arquitetura step-by-step) e persistência de arquivos.
  * **[👉 Clique aqui para ver como rodar a API de Rust e a sua documentação](./rust/README.md)**.
  
* **[Clojure (API Funcional)](./clojure/README.md)**
  * A mesma Todo List com funções puras, dados imutáveis, funções de alta ordem e composição. O único estado mutável é um `atom`, e a persistência em JSON fica isolada da lógica.
  * **[👉 Clique aqui para ver como rodar a API de Clojure e a sua documentação](./clojure/README.md)**.
  
* **[TypeScript (API POO)](./typescript/README.md)**
  * A mesma Todo List, agora com classes, encapsulamento, herança, polimorfismo e abstração (repositório de persistência em JSON). Servidor HTTP nativo do Node, sem framework web.
  * **[👉 Clique aqui para ver como rodar a API de TypeScript e a sua documentação](./typescript/README.md)**.

## Contrato comum das APIs

As três APIs expõem os mesmos endpoints, na porta `5000` por padrão, e gravam em `data/todos.json`. Um mesmo frontend funciona com qualquer uma delas. Como usam a mesma porta, suba só uma por vez ou mude `PORT`.

| Método | Rota | Descrição |
|---|---|---|
| GET | `/health` | Checagem do servidor |
| GET | `/todos` | Lista as tarefas |
| POST | `/todos` | Cria uma tarefa (`{ "title": "..." }`) |
| PATCH | `/todos/:id/toggle` | Inverte o estado `completed` |
| DELETE | `/todos/:id` | Remove a tarefa |
