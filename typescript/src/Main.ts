import { ApiServer } from "./ApiServer.js";
import { TodoService } from "./TodoService.js";

const host = process.env.HOST ?? "127.0.0.1";
const porta = Number(process.env.PORT ?? "5000");

const servico = new TodoService();
const api = new ApiServer(servico);

await api.iniciar(porta, host);

console.log(`Servidor POO (TypeScript) escutando em http://${host}:${porta}`);
