# 🚀 End-to-End Microservices Architecture with JWT Security

> Production-ready Microservices Architecture built using Spring Boot, Spring Cloud, and JWT-based authentication.

---

## 🏗️ Architecture Overview

This project demonstrates a complete enterprise-level microservices ecosystem including:

- 🌐 API Gateway (Centralized Routing)
- 🔎 Service Registry & Discovery (Eureka)
- 👤 User Service (Independent Database)
- 🏨 Hotel Service (Independent Database)
- ⚙️ Spring Cloud Config (Centralized Configuration)
- 🔐 JWT + Spring Security (Authentication & Authorization)
- 🛡 Fault Tolerance using Resilience4j
- 📦 Database per Service Architecture

---

## 🔐 Security Implementation (JWT + Spring Security)

This project uses **stateless authentication** with JSON Web Tokens (JWT).

### Key Security Features:
- ✅ Stateless authentication using JWT
- ✅ Role-Based Access Control (RBAC)
- ✅ Secure REST API endpoints
- ✅ Custom JWT authentication filter
- ✅ Password encryption using BCrypt
- ✅ Token validation & expiration handling
- ✅ Protected microservice communication

---

## ⚡ Fault Tolerance & Resilience (Resilience4j)

To ensure high availability and reliability:

- 🔁 Retry mechanism for temporary failures
- ⛔ Circuit Breaker to prevent cascading failures
- 🚦 Rate Limiter to control traffic
- 🛡 Secured internal service communication

---

## 🧠 Core Architectural Highlights

- ✔️ API Gateway pattern
- ✔️ Database per microservice
- ✔️ Service Discovery using Eureka
- ✔️ Centralized configuration management
- ✔️ Clean layered architecture
- ✔️ Global exception handling
- ✔️ DTO & validation best practices
- ✔️ Structured logging

---

## 🛠️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Cloud (Gateway, Eureka, Config)

### Resilience
- Resilience4j

### Database
- MySQL / PostgreSQL / MongoDB

### DevOps & Tools
- Git & GitHub
- Maven

---

## 📁 Microservices Structure

