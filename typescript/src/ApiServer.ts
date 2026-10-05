import { createServer, type IncomingMessage, type Server, type ServerResponse } from "node:http";

import { InvalidTitleException } from "./InvalidTitleException.js";
import type { ManipuladorHttp, ParametrosRota } from "./ManipuladorHttp.js";
import { ManipuladorConcluirTodo } from "./manipuladores/ManipuladorConcluirTodo.js";
import { ManipuladorCriarTodo } from "./manipuladores/ManipuladorCriarTodo.js";
import { ManipuladorListarTodos } from "./manipuladores/ManipuladorListarTodos.js";
import { ManipuladorRemoverTodo } from "./manipuladores/ManipuladorRemoverTodo.js";
import { RespostaHttp } from "./RespostaHttp.js";
import type { TodoService } from "./TodoService.js";
import { TodoNotFoundException } from "./TodoNotFoundException.js";

export class ApiServer {
  private readonly rotas: ManipuladorHttp[] = [];
  private servidor: Server | null = null;

  constructor(servico: TodoService) {
    this.registrar(new ManipuladorListarTodos(servico));
    this.registrar(new ManipuladorCriarTodo(servico));
    this.registrar(new ManipuladorConcluirTodo(servico));
    this.registrar(new ManipuladorRemoverTodo(servico));
  }

  registrar(manipulador: ManipuladorHttp): void {
    this.rotas.push(manipulador);
  }

  iniciar(porta: number, host = "127.0.0.1"): Promise<void> {
    const http = createServer((requisicao, resposta) => {
      void this.despachar(requisicao, resposta);
    });
    this.servidor = http;

    return new Promise((resolve, reject) => {
      http.once("error", reject);
      http.listen(porta, host, () => {
        http.removeListener("error", reject);
        resolve();
      });
    });
  }

  url(): string {
    const endereco = this.servidor?.address();
    if (endereco === null || endereco === undefined || typeof endereco === "string") {
      throw new Error("Servidor ainda não está escutando.");
    }
    return `http://127.0.0.1:${endereco.port}`;
  }

  encerrar(): Promise<void> {
    const http = this.servidor;
    if (http === null) {
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      http.close((erro) => {
        if (erro) {
          reject(erro);
          return;
        }
        resolve();
      });
    });
  }

  private async despachar(requisicao: IncomingMessage, resposta: ServerResponse): Promise<void> {
    RespostaHttp.cors(resposta);

    if (requisicao.method === "OPTIONS") {
      resposta.writeHead(204);
      resposta.end();
      return;
    }

    const metodo = requisicao.method ?? "GET";
    const caminho = new URL(requisicao.url ?? "/", "http://127.0.0.1").pathname;
    const escolha = this.escolher(metodo, caminho);

    if (escolha === null) {
      RespostaHttp.json(resposta, 404, { error: "Rota não encontrada." });
      return;
    }

    try {
      await escolha.manipulador.tratar(requisicao, resposta, escolha.parametros);
    } catch (erro) {
      if (erro instanceof InvalidTitleException) {
        RespostaHttp.json(resposta, 400, { error: erro.message });
        return;
      }
      if (erro instanceof TodoNotFoundException) {
        RespostaHttp.json(resposta, 404, { error: erro.message });
        return;
      }
      RespostaHttp.json(resposta, 500, { error: "Erro interno." });
    }
  }

  private escolher(
    metodo: string,
    caminho: string,
  ): { manipulador: ManipuladorHttp; parametros: ParametrosRota } | null {
    for (const manipulador of this.rotas) {
      const parametros = manipulador.corresponde(metodo, caminho);
      if (parametros !== null) {
        return { manipulador, parametros };
      }
    }
    return null;
  }
}
