import type { IncomingMessage, ServerResponse } from "node:http";

export type ParametrosRota = {
  id?: number;
};

export interface ManipuladorHttp {
  corresponde(metodo: string, caminho: string): ParametrosRota | null;
  tratar(
    requisicao: IncomingMessage,
    resposta: ServerResponse,
    parametros: ParametrosRota,
  ): Promise<void>;
}
