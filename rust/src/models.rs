//! Modelos de dados.

use serde::{Deserialize, Serialize};

/// Tipagem do payload de entrada para health-check.
#[derive(Debug, Serialize)]
pub struct HealthResponse {
    pub status: String,
    pub paradigm: String,
}

/// Tipagem do payload de entrada para criar um novo Todo.
#[derive(Debug, Deserialize)]
pub struct CreateTodoRequest {
    pub title: String,
}

/// Tipagem Todo.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Todo {
    pub id: u64,
    pub title: String,
    pub completed: bool,
}
