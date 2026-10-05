import type { ServerResponse } from "node:http";

export class RespostaHttp {
  static cors(resposta: ServerResponse): void {
    resposta.setHeader("Access-Control-Allow-Origin", "*");
    resposta.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, OPTIONS");
    resposta.setHeader("Access-Control-Allow-Headers", "Content-Type");
  }

  static json(resposta: ServerResponse, status: number, corpo: unknown): void {
    const json = JSON.stringify(corpo);
    resposta.writeHead(status, { "Content-Type": "application/json; charset=utf-8" });
    resposta.end(json);
  }
}
