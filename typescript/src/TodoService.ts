import { InvalidTitleException } from "./InvalidTitleException.js";
import { Todo } from "./Todo.js";
import { TodoNotFoundException } from "./TodoNotFoundException.js";

export class TodoService {
  private readonly todos: Todo[] = [];
  private proximoId = 1;

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

  complete(id: number): Todo {
    const todo = this.findById(id);
    todo.complete();
    return todo;
  }

  delete(id: number): void {
    const indice = this.todos.findIndex((todo) => todo.getId() === id);
    if (indice === -1) {
      throw new TodoNotFoundException();
    }

    this.todos.splice(indice, 1);
  }
}
