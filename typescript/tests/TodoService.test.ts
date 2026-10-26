import assert from "node:assert/strict";
import { describe, it } from "node:test";

import { InvalidTitleException } from "../src/InvalidTitleException.js";
import { TodoNotFoundException } from "../src/TodoNotFoundException.js";
import { TodoService } from "../src/TodoService.js";

describe("TodoService", () => {
  it("cria, lista e atribui ids em sequência", () => {
    const servico = new TodoService();

    const primeira = servico.create("Preparar apresentação");
    const segunda = servico.create("Testar a API");

    assert.equal(primeira.toJSON().id, 1);
    assert.equal(segunda.toJSON().id, 2);
    assert.equal(servico.list().length, 2);
  });

  it("localiza a tarefa e chama toggle() no objeto", () => {
    const servico = new TodoService();
    servico.create("Preparar apresentação");

    const concluida = servico.toggle(1);
    assert.equal(concluida.toJSON().completed, true);
    assert.equal(servico.findById(1).toJSON().completed, true);

    servico.toggle(1);
    assert.equal(servico.findById(1).toJSON().completed, false);
  });

  it("toggle de id inexistente lança TodoNotFoundException", () => {
    const servico = new TodoService();

    assert.throws(() => servico.toggle(99), TodoNotFoundException);
  });

  it("remove a tarefa e a segunda remoção não encontra o id", () => {
    const servico = new TodoService();
    servico.create("Preparar apresentação");

    servico.delete(1);

    assert.equal(servico.list().length, 0);
    assert.throws(() => servico.delete(1), TodoNotFoundException);
  });

  it("recusa título que não é texto", () => {
    const servico = new TodoService();

    assert.throws(() => servico.create(10), InvalidTitleException);
    assert.equal(servico.list().length, 0);
  });
});
