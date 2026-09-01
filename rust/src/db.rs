//! Banco de dados local baseado em arquivo JSON.

use std::fs;
use std::path::Path;
use std::sync::{Arc, Mutex};

use crate::models::Todo;

const DATA_FILE: &str = "data/todos.json";

struct LocalDbInner {
    todos: Vec<Todo>,
    next_id: u64,
}

#[derive(Clone)]
pub struct LocalDb {
    inner: Arc<Mutex<LocalDbInner>>,
}

impl LocalDb {
    pub fn load() -> Self {
        let mut todos: Vec<Todo> = Vec::new();
        let mut next_id: u64 = 1;

        let path = Path::new(DATA_FILE);

        let parent_opt = path.parent();
        if parent_opt.is_some() {
            let parent = parent_opt.unwrap();
            if parent.exists() == false {
                let _ = fs::create_dir_all(parent);
            }
        }

        if path.exists() == true {
            let read_result = fs::read_to_string(path);
            if read_result.is_ok() {
                let contents = read_result.unwrap();
                if contents.trim().len() > 0 {
                    let parse_result = serde_json::from_str::<Vec<Todo>>(&contents);
                    if parse_result.is_ok() {
                        let loaded = parse_result.unwrap();
                        
                        let mut max_id: u64 = 0;
                        for t in &loaded {
                            if t.id > max_id {
                                max_id = t.id;
                            }
                        }
                        
                        next_id = max_id + 1;
                        todos = loaded;
                    } else {
                        let e = parse_result.unwrap_err();
                        eprintln!("Aviso: falha ao ler {DATA_FILE}: {e}. Iniciando vazio.");
                    }
                }
            }
        }

        println!(
            "DB local carregado: {} todo(s), próximo ID = {next_id}",
            todos.len()
        );

        LocalDb {
            inner: Arc::new(Mutex::new(LocalDbInner { todos, next_id })),
        }
    }

    /// Persiste o estado atual no arquivo JSON.
    fn persist(inner: &LocalDbInner) {
        let ser_result = serde_json::to_string_pretty(&inner.todos);
        
        if ser_result.is_ok() {
            let json = ser_result.unwrap();
            let write_result = fs::write(DATA_FILE, json);
            if write_result.is_err() {
                let e = write_result.unwrap_err();
                eprintln!("Erro ao gravar {DATA_FILE}: {e}");
            }
        } else {
            let e = ser_result.unwrap_err();
            eprintln!("Erro ao serializar todos: {e}");
        }
    }

    /// Retorna cópia de todos os Todos.
    pub fn list(&self) -> Vec<Todo> {
        let guard = self.inner.lock().unwrap();
        
        let mut result: Vec<Todo> = Vec::new();

        for todo in &guard.todos {
            let cloned_todo = todo.clone();
            result.push(cloned_todo);
        }
        
        result
    }

    /// Insere um novo Todo e retorna a cópia dele com o ID atribuído.
    pub fn insert(&self, title: String) -> Todo {
        let mut guard = self.inner.lock().unwrap();
        
        let safe_title: String;
        if title.trim().len() == 0 {
            safe_title = String::from("Sem título");
        } else {
            safe_title = title;
        }

        let new_id = guard.next_id;
        
        let todo = Todo {
            id: new_id,
            title: safe_title,
            completed: false,
        };
        
        let next_id = new_id + 1;
        guard.next_id = next_id;
        guard.todos.push(todo.clone());

        Self::persist(&guard);
        todo
    }

    /// Alterna o campo `completed` de um Todo. Retorna o Todo atualizado.
    pub fn toggle_complete(&self, id: u64) -> Option<Todo> {
        let mut guard = self.inner.lock().unwrap();

        let mut result: Option<Todo> = None;
        
        if guard.todos.len() == 0 {
            result = None;
        } else {
            for todo in guard.todos.iter_mut() {
                if todo.id == id {
                    if todo.completed == true {
                        todo.completed = false;
                    } else {
                        todo.completed = true;
                    }
                    let cloned_todo = todo.clone();
                    result = Some(cloned_todo);
                    break;
                }
            }
        }

        if result.is_some() {
            Self::persist(&guard);
        }
        
        result
    }

    /// Remove um Todo pelo ID. Retorna `true` se existia e foi removido.
    pub fn delete(&self, id: u64) -> bool {
        let mut guard = self.inner.lock().unwrap();

        let mut was_removed: bool = false;
        
        if guard.todos.len() == 0 {
            was_removed = false;
        } else {
            let mut index_to_remove: Option<usize> = None;
            let mut current_index: usize = 0;
            
            for todo in &guard.todos {
                if todo.id == id {
                    index_to_remove = Some(current_index);
                    break;
                }
                current_index = current_index + 1;
            }
            
            if let Some(idx) = index_to_remove {
                guard.todos.remove(idx);
                was_removed = true;
                Self::persist(&guard);
            } else {
                was_removed = false;
            }
        }
        
        was_removed
    }
}
