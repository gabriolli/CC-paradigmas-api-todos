export class TodoNotFoundException extends Error {
  constructor(message = "Todo não encontrado.") {
    super(message);
    this.name = "TodoNotFoundException";
  }
}
