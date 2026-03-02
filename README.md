# 🧵 KALAKAR — Production-Ready E-Commerce System

KALAKAR is a full-stack e-commerce application engineered using Spring Boot and MySQL, designed to demonstrate scalable backend architecture, secure authentication, and transactional order processing.

This project emphasizes clean system design, layered architecture, and production-grade security practices.

---

## 🏗 System Architecture

The application follows a layered MVC architecture:

Controller → Service → Repository → Database

- Clear separation of concerns
- Business logic isolated in service layer
- Repository abstraction using Spring Data JPA
- Transactional service boundaries for order processing

---

## 🔐 Security Implementation

- Spring Security 6 configuration
- Role-based access control (ROLE_USER / ROLE_ADMIN)
- BCrypt password hashing (strength 12)
- CSRF protection enabled globally
- Session-based authentication
- Secure password reset with expiring tokens

All admin endpoints are fully protected and inaccessible to unauthorized users.

---

## 🛒 Core Functionalities

### Customer Flow
- Product catalog with category filtering
- Persistent shopping cart per authenticated user
- Secure checkout with Stripe integration
- Order history tracking
- Email-based password reset

### Admin Flow
- Admin dashboard overview
- Product CRUD operations
- Order management system
- Product image upload and management

---

## 💾 Database Design

- Normalized relational schema (MySQL 8)
- One-to-Many relationships:
  - User → Orders
  - Order → OrderItems
  - Product → Category
- Proper cascade rules and entity mappings
- Optimized lazy loading configuration

---

## 🛠️ Tech Stack

**Backend**
- Java 17
- Spring Boot 3.5
- Spring Security 6
- Spring Data JPA (Hibernate)

**Database**
- MySQL 8

**Frontend**
- Thymeleaf
- HTML / CSS / JavaScript

**Payments**
- Stripe Checkout API

**Build Tool**
- Maven

---

## 📸 Screenshots

### 🏠 Home Page
![Home](screenshots/home.png)

### 🔐 Login Page
![Login](screenshots/login.png)

### 🛒 Checkout
![Checkout](screenshots/checkout.png)

### 🛠️ Admin Dashboard
![Admin Dashboard](screenshots/admin.png)

### 📦 Admin Product Management
![Admin Products](screenshots/admin-products.png)

### 📦 User Orders
![User Orders](screenshots/user-orders.png)

### 🚪 Logout
![Logout](screenshots/logout.png)

---

## ⚙️ Running Locally

1. Clone the repository
2. Configure your MySQL database
3. Add database, Stripe, and email credentials in `application.properties`
4. Run:

5. Visit:http://localhost:3036


---

## 📈 Scalability Considerations

- Stateless controller design
- Service-layer transaction boundaries
- Database normalization for consistency
- Easily extensible to REST API architecture
- Designed to scale horizontally behind a load balancer
- Can evolve into microservices architecture

---

## 🔮 Future Enhancements

- JWT-based stateless authentication
- Redis caching for cart/session optimization
- Docker containerization
- CI/CD pipeline integration
- Cloud deployment (AWS / GCP)

---

## 👤 Author

**Rishikanth Deva**  
GitHub: https://github.com/rishikanthjavadev-stack  
LinkedIn: linkedin.com/in/rishi-d-a785a8263

