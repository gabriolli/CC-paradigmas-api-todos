import assert from "node:assert/strict";
import { after, before, describe, it } from "node:test";

import { ApiServer } from "../src/ApiServer.js";
import { TodoService } from "../src/TodoService.js";

describe("ApiServer", () => {
  let servidor: ApiServer;
  let base: string;

  before(async () => {
    servidor = new ApiServer(new TodoService());
    await servidor.iniciar(0);
    base = servidor.url();
  });

  after(async () => {
    await servidor.encerrar();
  });

  it("cria, lista, conclui e remove uma tarefa", async () => {
    const criada = await fetch(`${base}/todos`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: "Preparar apresentação" }),
    });
    assert.equal(criada.status, 201);
    assert.deepEqual(await criada.json(), {
      id: 1,
      title: "Preparar apresentação",
      completed: false,
    });

    const lista = await fetch(`${base}/todos`);
    assert.equal(lista.status, 200);
    assert.deepEqual(await lista.json(), [
      { id: 1, title: "Preparar apresentação", completed: false },
    ]);

    const patch = await fetch(`${base}/todos/1/complete`, { method: "PATCH" });
    assert.equal(patch.status, 200);
    assert.equal((await patch.json()).completed, true);

    const deNovo = await fetch(`${base}/todos/1/complete`, { method: "PATCH" });
    assert.equal((await deNovo.json()).completed, true);

    const removida = await fetch(`${base}/todos/1`, { method: "DELETE" });
    assert.equal(removida.status, 200);

    const ausente = await fetch(`${base}/todos/1`, { method: "DELETE" });
    assert.equal(ausente.status, 404);
  });

  it("rejeita POST sem JSON, sem title e com título vazio", async () => {
    const semJson = await fetch(`${base}/todos`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: "nao-json",
    });
    assert.equal(semJson.status, 400);

    const semTitle = await fetch(`${base}/todos`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({}),
    });
    assert.equal(semTitle.status, 400);

    const vazio = await fetch(`${base}/todos`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: "  " }),
    });
    assert.equal(vazio.status, 400);
  });

  it("responde 404 quando o PATCH não acha a tarefa", async () => {
    const resposta = await fetch(`${base}/todos/99/complete`, { method: "PATCH" });
    assert.equal(resposta.status, 404);
  });
});
