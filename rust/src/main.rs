//! API web em Rust + Axum

mod db;
mod handlers;
mod models;
mod routes;

use std::net::SocketAddr;

use axum::Router;
use tower_http::cors::{Any, CorsLayer};
use axum::http::Method;

/// Estado compartilhado da aplicação (injetado nos handlers pelo Axum).
#[derive(Clone)]
pub struct AppState {
    pub db: db::LocalDb,
}

#[tokio::main]
async fn main() {
    let _ = dotenvy::dotenv();

    let mut host: String = String::new();
    let host_result = std::env::var("HOST");
    if host_result.is_ok() {
        host = host_result.unwrap();
    } else {
        host = String::from("127.0.0.1");
    }

    let mut port: u16 = 5000;
    let port_result = std::env::var("PORT");
    if port_result.is_ok() {
        let port_str = port_result.unwrap();
        let parse_result = port_str.parse::<u16>();
        if parse_result.is_ok() {
            port = parse_result.unwrap();
        } else {
            port = 5000;
        }
    } else {
        port = 5000;
    }

    let local_db = db::LocalDb::load();

    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(vec![Method::GET, Method::POST, Method::PATCH, Method::DELETE])
        .allow_headers(vec![axum::http::header::CONTENT_TYPE]);

    let state = AppState { db: local_db };
    let app: Router = routes::build_router(state).layer(cors);

    let ip_parse_result = host.parse::<std::net::IpAddr>();
    let mut ip_addr: std::net::IpAddr = std::net::IpAddr::V4(std::net::Ipv4Addr::new(127, 0, 0, 1));
    
    if ip_parse_result.is_ok() {
        ip_addr = ip_parse_result.unwrap();
    } else {
        eprintln!("HOST inválido: {host}");
        std::process::exit(1);
    }
    
    let addr = SocketAddr::new(ip_addr, port);

    println!("Servidor imperativo (Axum) escutando em http://{addr}");

    let bind_result = tokio::net::TcpListener::bind(addr).await;
    if bind_result.is_err() {
        let e = bind_result.unwrap_err();
        eprintln!("Falha ao bind em {addr}: {e}");
        std::process::exit(1);
    }
    let listener = bind_result.unwrap();

    let serve_result = axum::serve(listener, app).await;
    if serve_result.is_err() {
        let e = serve_result.unwrap_err();
        eprintln!("Erro no servidor: {e}");
        std::process::exit(1);
    }
}
