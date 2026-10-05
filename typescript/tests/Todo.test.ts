import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { InvalidTitleException } from "../src/InvalidTitleException.js";
import { Todo } from "../src/Todo.js";

describe("Todo", () => {
  it("guarda id e título e nasce não concluída", () => {
    const todo = new Todo(1, "Preparar apresentação");

    assert.deepEqual(todo.toJSON(), {
      id: 1,
      title: "Preparar apresentação",
      completed: false,
    });
  });

  it("complete() marca a tarefa como concluída e repetir mantém true", () => {
    const todo = new Todo(1, "Preparar apresentação");

    todo.complete();
    todo.complete();

    assert.equal(todo.toJSON().completed, true);
  });

  it("recusa título vazio", () => {
    assert.throws(() => new Todo(1, "   "), InvalidTitleException);
  });
});
