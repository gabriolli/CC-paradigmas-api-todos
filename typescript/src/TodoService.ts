import { InvalidTitleException } from "./InvalidTitleException.js";
import { RepositorioMemoria } from "./RepositorioMemoria.js";
import type { RepositorioTodos } from "./RepositorioTodos.js";
import { Todo } from "./Todo.js";
import { TodoNotFoundException } from "./TodoNotFoundException.js";

export class TodoService {
  private readonly todos: Todo[];
  private proximoId: number;

  constructor(private readonly repositorio: RepositorioTodos = new RepositorioMemoria()) {
    this.todos = repositorio.carregar();
    this.proximoId = this.calcularProximoId();
  }

  list(): Todo[] {
    return this.todos.slice();
  }

  create(title: unknown): Todo {
    if (typeof title !== "string") {
      throw new InvalidTitleException("O campo title deve ser texto.");
    }

    const todo = new Todo(this.proximoId, title);
    this.proximoId = this.proximoId + 1;
    this.todos.push(todo);
    this.repositorio.salvar(this.todos);
    return todo;
  }

  findById(id: number): Todo {
    for (const todo of this.todos) {
      if (todo.getId() === id) {
        return todo;
      }
    }

    throw new TodoNotFoundException();
  }

  toggle(id: number): Todo {
    const todo = this.findById(id);
    todo.toggle();
    this.repositorio.salvar(this.todos);
    return todo;
  }

  delete(id: number): void {
    const indice = this.todos.findIndex((todo) => todo.getId() === id);
    if (indice === -1) {
      throw new TodoNotFoundException();
    }

    this.todos.splice(indice, 1);
    this.repositorio.salvar(this.todos);
  }

  private calcularProximoId(): number {
    let maior = 0;
    for (const todo of this.todos) {
      if (todo.getId() > maior) {
        maior = todo.getId();
      }
    }
    return maior + 1;
  }
}
