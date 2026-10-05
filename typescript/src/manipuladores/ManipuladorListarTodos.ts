import type { IncomingMessage, ServerResponse } from "node:http";

import type { ManipuladorHttp, ParametrosRota } from "../ManipuladorHttp.js";
import { RespostaHttp } from "../RespostaHttp.js";
import type { TodoService } from "../TodoService.js";

export class ManipuladorListarTodos implements ManipuladorHttp {
  constructor(private readonly servico: TodoService) {}

  corresponde(metodo: string, caminho: string): ParametrosRota | null {
    if (metodo === "GET" && caminho === "/todos") {
      return {};
    }
    return null;
  }

  async tratar(
    _requisicao: IncomingMessage,
    resposta: ServerResponse,
    _parametros: ParametrosRota,
  ): Promise<void> {
    RespostaHttp.json(resposta, 200, this.servico.list());
  }
}
