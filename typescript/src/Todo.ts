import { InvalidTitleException } from "./InvalidTitleException.js";

export type TodoJson = {
  id: number;
  title: string;
  completed: boolean;
};

export class Todo {
  private readonly id: number;
  private readonly title: string;
  private completed: boolean;

  constructor(id: number, title: string) {
    const titulo = title.trim();
    if (titulo.length === 0) {
      throw new InvalidTitleException("Título não pode ser vazio.");
    }

    this.id = id;
    this.title = titulo;
    this.completed = false;
  }

  complete(): void {
    this.completed = true;
  }

  getId(): number {
    return this.id;
  }

  toJSON(): TodoJson {
    return {
      id: this.id,
      title: this.title,
      completed: this.completed,
    };
  }
}
