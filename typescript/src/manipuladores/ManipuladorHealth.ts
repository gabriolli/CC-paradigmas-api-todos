import type { IncomingMessage, ServerResponse } from "node:http";

import type { ManipuladorHttp, ParametrosRota } from "../ManipuladorHttp.js";
import { RespostaHttp } from "../RespostaHttp.js";

export class ManipuladorHealth implements ManipuladorHttp {
  corresponde(metodo: string, caminho: string): ParametrosRota | null {
    if (metodo === "GET" && caminho === "/health") {
      return {};
    }
    return null;
  }

  async tratar(
    _requisicao: IncomingMessage,
    resposta: ServerResponse,
    _parametros: ParametrosRota,
  ): Promise<void> {
    RespostaHttp.json(resposta, 200, { status: "ok", paradigm: "poo" });
  }
}
