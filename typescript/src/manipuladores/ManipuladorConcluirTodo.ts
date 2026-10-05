import type { IncomingMessage, ServerResponse } from "node:http";

import type { ManipuladorHttp, ParametrosRota } from "../ManipuladorHttp.js";
import { RespostaHttp } from "../RespostaHttp.js";
import type { TodoService } from "../TodoService.js";

export class ManipuladorConcluirTodo implements ManipuladorHttp {
  constructor(private readonly servico: TodoService) {}

  corresponde(metodo: string, caminho: string): ParametrosRota | null {
    if (metodo !== "PATCH") {
      return null;
    }

    const encontrado = caminho.match(/^\/todos\/(\d+)\/complete$/);
    if (encontrado === null) {
      return null;
    }

    return { id: Number(encontrado[1]) };
  }

  async tratar(
    _requisicao: IncomingMessage,
    resposta: ServerResponse,
    parametros: ParametrosRota,
  ): Promise<void> {
    const todo = this.servico.complete(parametros.id ?? 0);
    RespostaHttp.json(resposta, 200, todo);
  }
}
