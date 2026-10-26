import assert from "node:assert/strict";
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, it } from "node:test";

import { RepositorioArquivoJson } from "../src/RepositorioArquivoJson.js";
import { Todo } from "../src/Todo.js";
import { TodoService } from "../src/TodoService.js";

describe("RepositorioArquivoJson", () => {
  let pasta: string;
  let caminho: string;

  beforeEach(() => {
    pasta = mkdtempSync(join(tmpdir(), "todo-ts-"));
    caminho = join(pasta, "dados", "todos.json");
  });

  afterEach(() => {
    rmSync(pasta, { recursive: true, force: true });
  });

  it("arquivo inexistente começa vazio", () => {
    assert.deepEqual(new RepositorioArquivoJson(caminho).carregar(), []);
  });

  it("salva criando a pasta e carrega de volta o mesmo estado", () => {
    const repositorio = new RepositorioArquivoJson(caminho);
    const concluida = new Todo(1, "Primeira");
    concluida.complete();

    repositorio.salvar([concluida, new Todo(2, "Segunda")]);

    assert.deepEqual(
      repositorio.carregar().map((todo) => todo.toJSON()),
      [
        { id: 1, title: "Primeira", completed: true },
        { id: 2, title: "Segunda", completed: false },
      ],
    );
  });

  it("arquivo com conteúdo inválido começa vazio", () => {
    const repositorio = new RepositorioArquivoJson(caminho);
    repositorio.salvar([]);

    for (const conteudo of ["", "nao-json", '{"a":1}', '[{"id":"x"}]']) {
      writeFileSync(caminho, conteudo, "utf8");
      assert.deepEqual(repositorio.carregar(), [], `conteúdo: ${conteudo}`);
    }
  });

  it("o TodoService grava no arquivo a cada mudança e retoma o estado ao reiniciar", () => {
    const primeiro = new TodoService(new RepositorioArquivoJson(caminho));
    primeiro.create("Preparar apresentação");
    primeiro.create("Testar a API");
    primeiro.toggle(1);
    primeiro.delete(2);

    assert.deepEqual(JSON.parse(readFileSync(caminho, "utf8")), [
      { id: 1, title: "Preparar apresentação", completed: true },
    ]);

    const reiniciado = new TodoService(new RepositorioArquivoJson(caminho));
    assert.equal(reiniciado.list().length, 1);
    assert.equal(reiniciado.findById(1).toJSON().completed, true);
    assert.equal(reiniciado.create("Nova").toJSON().id, 2);
  });

  it("ao reiniciar, o próximo id continua depois do maior id salvo", () => {
    const primeiro = new TodoService(new RepositorioArquivoJson(caminho));
    primeiro.create("A");
    primeiro.create("B");

    const reiniciado = new TodoService(new RepositorioArquivoJson(caminho));
    assert.equal(reiniciado.create("C").toJSON().id, 3);
  });
});
