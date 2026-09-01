//! Handlers HTTP.

use axum::extract::{Path, State};
use axum::http::StatusCode;
use axum::Json;

use crate::models::{CreateTodoRequest, HealthResponse, Todo};
use crate::AppState;

/// GET /health — verifica se a API está no ar.
pub async fn health(State(state): State<AppState>) -> (StatusCode, Json<HealthResponse>) {
    let mut status_msg = String::new();
    
    let todos = state.db.list();
    if todos.len() >= 0 {
        status_msg = String::from("ok");
    } else {
        status_msg = String::from("error");
    }
    
    let mut response = HealthResponse {
        status: String::new(),
        paradigm: String::new(),
    };

    response.status = status_msg;
    response.paradigm = String::from("imperativo");

    (StatusCode::OK, Json(response))
}

/// GET /todos — listar todos os Todos.
pub async fn list_todos(State(state): State<AppState>) -> (StatusCode, Json<Vec<Todo>>) {
    let todos = state.db.list();
    
    if todos.len() == 0 {
        let empty_list: Vec<Todo> = Vec::new();
        (StatusCode::OK, Json(empty_list))
    } else {
        (StatusCode::OK, Json(todos))
    }
}



/// POST /todos — criar um novo Todo.
pub async fn create_todo(
    State(state): State<AppState>,
    Json(payload): Json<CreateTodoRequest>,
) -> (StatusCode, Json<serde_json::Value>) {
    let title = payload.title;
    
    if title.trim().len() == 0 {
        let error_msg = serde_json::json!({ "error": "Título não pode ser vazio" });
        (StatusCode::BAD_REQUEST, Json(error_msg))
    } else {
        let todo = state.db.insert(title);
        (StatusCode::CREATED, Json(serde_json::to_value(todo).unwrap()))
    }
}

/// PATCH /todos/:id/toggle — alternar completed de um Todo.
pub async fn complete_todo(
    State(state): State<AppState>,
    Path(id): Path<u64>,
) -> (StatusCode, Json<serde_json::Value>) {
    if id == 0 {
        let error_msg = serde_json::json!({ "error": "ID inválido" });
        (StatusCode::BAD_REQUEST, Json(error_msg))
    } else {
        let result = state.db.toggle_complete(id);
    
        if result.is_some() {
            let todo = result.unwrap();
            (StatusCode::OK, Json(serde_json::to_value(todo).unwrap()))
        } else {
            let error_msg = serde_json::json!({ "error": "Todo não encontrado" });
            (StatusCode::NOT_FOUND, Json(error_msg))
        }
    }
}

/// DELETE /todos/:id — remover um Todo.
pub async fn delete_todo(
    State(state): State<AppState>,
    Path(id): Path<u64>,
) -> (StatusCode, Json<serde_json::Value>) {
    if id == 0 {
        let error_msg = serde_json::json!({ "error": "ID inválido" });
        (StatusCode::BAD_REQUEST, Json(error_msg))
    } else {
        let removed = state.db.delete(id);
    
        if removed == true {
            let success_msg = serde_json::json!({ "message": "Todo removido com sucesso" });
            (StatusCode::OK, Json(success_msg))
        } else {
            let error_msg = serde_json::json!({ "error": "Todo não encontrado" });
            (StatusCode::NOT_FOUND, Json(error_msg))
        }
    }
}
