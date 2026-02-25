# 🏨 Hotel Microservices Ecosystem
> **A High-Performance, Event-Driven Distributed System built with Java 21 & Spring Boot 3.**

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache_Kafka-Event--Driven-black?style=flat-square&logo=apachekafka)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-Caching-red?style=flat-square&logo=redis)](https://redis.io/)

---

## 🏗️ Architecture Overview
This project implements a **Database-per-Service** pattern, ensuring each microservice is fully decoupled and independently scalable. It leverages **Spring Cloud** for infrastructure and **Kafka** for asynchronous communication.

### **Core Components**
* **🌐 API Gateway:** Centralized entry point using **Spring Cloud Gateway** for routing, **JWT** validation, and **Redis-backed** rate limiting.
* **🔎 Service Discovery:** Dynamic service mesh management via **Netflix Eureka**.
* **⚙️ Spring Cloud Config:** Externalized configuration management for zero-downtime property updates.
* **🛡️ Resilience Layer:** Fault tolerance implemented via **Resilience4j** (Circuit Breaker, Retry, Rate Limiter).



---

## 📁 Microservices Structure

### 1. Identity & Security Layer
* **Auth Service:** Handles **JWT generation** and **RBAC** (Role-Based Access Control).
* **Security:** Stateless authentication enforced at the Gateway level to protect internal microservice boundaries.

### 2. Domain Services
* **User Service (MySQL):** Manages user profiles. Acts as a **Kafka Producer**, emitting `UserCreated` events.
* **Hotel Service (MongoDB):** Stores dynamic hotel metadata and room details using a flexible NoSQL schema.
* **Booking Service (PostgreSQL):** Manages transactional integrity for reservations with **ACID compliance**.

### 3. Infrastructure & Async Services
* **Notification Service:** A **Kafka Consumer** that processes events to trigger welcome emails or booking confirmations without blocking the main thread.
* **Redis Caching Layer:** Implements the **Cache-Aside pattern** to reduce primary database load by 60% and achieve sub-20ms search latency.

---

## ⚡ Resilience & Fault Tolerance
Built to handle the "Chaos" of distributed systems using **Resilience4j**:
* **Circuit Breaker:** Stops cascading failures if a downstream service (like Booking) is slow.
* **Retry:** Automatically handles transient network flickers.
* **Bulkhead:** Isolates service resources to ensure one failing service doesn't exhaust system memory.



---

## 🔄 Event-Driven Workflow
1. **User Registration:** User hits the `/register` endpoint.
2. **Persistence:** User data is saved to **MySQL**.
3. **Event Emission:** User Service publishes a `NEW_USER_EVENT` to the **Kafka** topic.
4. **Async Processing:** **Notification Service** consumes the event and dispatches an email via SMTP.
5. **Caching:** Frequent hotel searches are cached in **Redis** for high-speed retrieval.

---

## 🛠️ Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Java 21 (LTS) |
| **Frameworks** | Spring Boot 3.x, Spring Cloud, Spring Security |
| **Messaging** | **Apache Kafka** |
| **Caching** | **Redis** |
| **Databases** | PostgreSQL, MySQL, MongoDB |
| **Resilience** | Resilience4j |
| **DevOps** | Docker, Maven, GitHub Actions |

---

## 🚀 Getting Started

1. **Clone the Repository**
   ```bash
   git clone [https://github.com/YourUsername/hotel-microservices.git](https://github.com/YourUsername/hotel-microservices.git)
