import type { IncomingMessage, ServerResponse } from "node:http";

import type { ManipuladorHttp, ParametrosRota } from "../ManipuladorHttp.js";
import { RespostaHttp } from "../RespostaHttp.js";
import type { TodoService } from "../TodoService.js";

export class ManipuladorCriarTodo implements ManipuladorHttp {
  constructor(private readonly servico: TodoService) {}

  corresponde(metodo: string, caminho: string): ParametrosRota | null {
    if (metodo === "POST" && caminho === "/todos") {
      return {};
    }
    return null;
  }

  async tratar(
    requisicao: IncomingMessage,
    resposta: ServerResponse,
    _parametros: ParametrosRota,
  ): Promise<void> {
    const dados = await lerJson(requisicao);
    if (dados === null) {
      RespostaHttp.json(resposta, 400, { error: "JSON inválido ou não enviado." });
      return;
    }
    if (!("title" in dados)) {
      RespostaHttp.json(resposta, 400, { error: "O campo title é obrigatório." });
      return;
    }

    const todo = this.servico.create(dados.title);
    RespostaHttp.json(resposta, 201, todo);
  }
}

function lerJson(requisicao: IncomingMessage): Promise<Record<string, unknown> | null> {
  return new Promise((resolve) => {
    const pedacos: Buffer[] = [];

    requisicao.on("data", (pedaco: Buffer) => {
      pedacos.push(pedaco);
    });

    requisicao.on("end", () => {
      const texto = Buffer.concat(pedacos).toString("utf8");
      try {
        const valor: unknown = JSON.parse(texto);
        if (valor === null || typeof valor !== "object" || Array.isArray(valor)) {
          resolve(null);
          return;
        }
        resolve(valor as Record<string, unknown>);
      } catch {
        resolve(null);
      }
    });

    requisicao.on("error", () => {
      resolve(null);
    });
  });
}
