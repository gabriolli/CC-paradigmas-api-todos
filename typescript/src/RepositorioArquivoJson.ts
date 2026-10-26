import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname } from "node:path";

import { Todo, type TodoJson } from "./Todo.js";
import type { RepositorioTodos } from "./RepositorioTodos.js";

/** Guarda as tarefas em um arquivo JSON, como nas versões em Rust e Clojure. */
export class RepositorioArquivoJson implements RepositorioTodos {
  constructor(private readonly caminho: string) {}

  carregar(): Todo[] {
    if (!existsSync(this.caminho)) {
      return [];
    }

    const texto = readFileSync(this.caminho, "utf8");
    if (texto.trim().length === 0) {
      return [];
    }

    try {
      const dados: unknown = JSON.parse(texto);
      if (!Array.isArray(dados) || !dados.every(ehTodoJson)) {
        throw new Error("o conteúdo não é uma lista de tarefas válida");
      }
      return dados.map((item) => Todo.fromJSON(item));
    } catch (erro) {
      const motivo = erro instanceof Error ? erro.message : String(erro);
      console.error(`Aviso: falha ao ler ${this.caminho}: ${motivo}. Iniciando vazio.`);
      return [];
    }
  }

  salvar(todos: readonly Todo[]): void {
    mkdirSync(dirname(this.caminho), { recursive: true });
    const json = JSON.stringify(todos.map((todo) => todo.toJSON()), null, 2);
    writeFileSync(this.caminho, json, "utf8");
  }
}

function ehTodoJson(valor: unknown): valor is TodoJson {
  if (valor === null || typeof valor !== "object") {
    return false;
  }
  const item = valor as Record<string, unknown>;
  return (
    typeof item.id === "number" &&
    Number.isInteger(item.id) &&
    item.id > 0 &&
    typeof item.title === "string" &&
    typeof item.completed === "boolean"
  );
}
