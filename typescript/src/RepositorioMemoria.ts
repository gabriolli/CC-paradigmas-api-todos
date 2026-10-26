import type { Todo } from "./Todo.js";
import type { RepositorioTodos } from "./RepositorioTodos.js";

/** Não persiste nada: começa vazio e descarta o que recebe em salvar(). */
export class RepositorioMemoria implements RepositorioTodos {
  carregar(): Todo[] {
    return [];
  }

  salvar(_todos: readonly Todo[]): void {}
}
