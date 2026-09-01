//! Registro explícito de rotas (tabela de procedimentos HTTP).

use axum::routing::{delete, get, patch, post};
use axum::Router;

use crate::handlers;
use crate::AppState;

pub fn build_router(state: AppState) -> Router {
    let mut app = Router::new();


    app = app.route("/health", get(handlers::health));
    app = app.route("/todos", get(handlers::list_todos));
    app = app.route("/todos", post(handlers::create_todo));
    app = app.route("/todos/:id/toggle", patch(handlers::complete_todo));
    app = app.route("/todos/:id", delete(handlers::delete_todo));

    app.with_state(state)
}
