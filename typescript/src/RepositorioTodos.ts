import type { Todo } from "./Todo.js";

/**
 * Contrato de persistência. O TodoService depende desta abstração e não sabe
 * se os dados vão para a memória, para um arquivo ou para um banco.
 */
export interface RepositorioTodos {
  carregar(): Todo[];
  salvar(todos: readonly Todo[]): void;
}
