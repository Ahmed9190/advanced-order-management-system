# Testing Strategy for the Customer Order Management System

This document outlines the testing philosophy and strategy for our microservices ecosystem. Our core principle is **Testing for Return on Investment (ROI)**: we focus testing effort on areas with the highest business risk and technical complexity.

## Our Testing Pyramid

Our strategy is visualized as a pyramid, emphasizing where we should invest the most effort.

                        /\
                       /  \
                      /    \    <-- **E2E / User Journey Tests** (Very Few)
                     /------\
                    /        \  <-- **Service Integration Tests** (Some)
                   /----------\
                  /            \
                 /**Unit Tests**\ <-- **(Many, but Focused)**
                /----------------\

---

## 1. Unit Tests (The Foundation)

- **Goal:** To verify a single, small piece of logic (a "unit") in complete isolation. These tests should be extremely fast and have no external dependencies (no network, no database, no filesystem).
- **What is a "unit" for us?**
  - A single method in a Mapper (e.g., `OrderMapper.toProto()`).
  - A specific business rule in a Domain Entity (e.g., `Order.canTransitionTo()`).
  - A utility function.
- **Tools:** JUnit 5, Mockito.
- **When to write them:**
  - For complex business logic, algorithms, or transformations.
  - When a piece of code has many edge cases that are hard to trigger in an integration test.
- **What NOT to unit test:**
  - Simple getters and setters.
  - Code that just calls another component (e.g., a controller method that only calls a service method). We test that at the integration level.

**Example (Conceptual):**
A unit test would verify that `Order.calculateTotalAmount()` correctly sums the prices of its `OrderItems`. It would not involve a database.

---

## 2. Service Integration Tests (The Core of Our Strategy)

This is where we get the most ROI. These tests verify that a service works correctly through its external API, including its interactions with its direct dependencies like a database.

- **Goal:** To confirm that a service's components are wired together correctly and that it behaves as expected from a client's perspective.
- **What we test at this layer:**
  - **gRPC Endpoints:** Can a client call our RPC, and does the service process it correctly and save to the database? (Like our `OrderGrpcIntegrationTest`).
  - **Database Repositories:** Do our Spring Data JPA queries work as expected?
  - **Client-Side Integrations:** Can our `customer-service` correctly call the `order-service`? (Like our `OrderServiceClientIntegrationTest`).
- **Tools:** Spring Boot Test (`@SpringBootTest`), TestContainers (for databases/brokers), `grpc-testing` (for in-process servers).
- **Key Principle:** These tests validate a **single service's boundary**. For `order-service`, the test interacts with its gRPC endpoint and verifies outcomes in an in-memory or containerized database. It does **not** call other live microservices.

**Example:**
Our `OrderGrpcIntegrationTest` is a perfect example. It starts the `order-service` and tests its `createOrder` and `getOrder` RPCs against a real (but test-scoped) H2 database.

---

## 3. End-to-End (E2E) / User Journey Tests (The Peak)

- **Goal:** To verify a complete business flow that crosses multiple services, simulating a real user journey. These tests are the most valuable for business confidence but also the most expensive and slowest to run.
- **What we test at this layer:**
  - A critical user journey like: "A user logs in, places an order, the order is confirmed, and an inventory check is triggered."
- **Tools:** Playwright/Cypress (for UI), or custom scripts that call multiple service APIs in sequence.
- **Key Principle:** We write **very few** of these. They are reserved for the 2-3 most critical "money paths" of the application. They run against a deployed environment (e.g., in Kubernetes) where all services are live. We will build these in Phase 3 of the curriculum.

---

**A Note on Smoke Tests:**
A simple integration test that checks if the Spring Boot application context loads successfully (`contextLoads()`) is a mandatory "smoke test" for every service. It's our first line of defense against configuration errors.
