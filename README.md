# 🐾 Virtual Pet 🐾

**Virtual Pet** is a full-stack web application developed as part of the *S5.02 learning challenge* at ItAcademy.  
The goal is to build a functional virtual pet system while exploring **AI-assisted frontend generation**, understanding how to analyze and adjust AI-generated code, and connecting it to a secure **Java backend** built with Spring Boot.

---

## ✨ Application Overview

The **Virtual Pet App** allows users to adopt, care for, and interact with virtual dogs in a fun and colorful environment.

### 🐶 Core Features
- **Register (Signup)** → Create an account with email and password.  
- **Login** → Authenticate and receive a JWT token.  
- **Create Pets** → Adopt dogs from different breeds: Dalmatian, Labrador, or Golden Retriever.  
- **View Pets** → Monitor hunger, hygiene, and fun levels.  
- **Interact** → Perform actions like *feed*, *wash*, or *play* to keep pets happy.  
- **Evolve** → Pets grow through stages based on actions performed:  
  **BABY → ADULT → SENIOR → PASSED**.  
- **Delete Pets** → Remove pets you no longer wish to keep.

---

## 🔑 Roles & Authorization

The backend implements **JWT authentication** and **role-based authorization** to secure endpoints:

| Role | Permissions |
|------|--------------|
| `ROLE_USER` | Manage only their own pets (CRUD + actions). |
| `ROLE_ADMIN` | View, update, and delete all pets and users. |

All protected endpoints require a valid JWT token and proper role authorization.

---

## 🤖 AI Tools & Workflow

Two AI tools were used during the project:

- **V0 (by Vercel)** → to generate and structure the React + Vite frontend.  
- **ChatGPT** → to assist with frontend code adjustments and create custom kawaii-style pet illustrations.  

### Workflow
1. Generated the initial frontend with V0.  
2. Customized layout, UI, and component interactions.  
3. Integrated API calls to connect with the backend.  
4. Added JWT authentication and pet state management.  

---

## 🔗 Frontend–Backend Connection

- **Backend:** REST API built with Spring Boot 3.3, Spring Security (JWT), and JPA (MySQL).  
- **Frontend:** React + Vite app generated with V0.  

The frontend runs at **http://localhost:3000**, and it communicates with the backend API running on **http://localhost:8080** via HTTP requests using `fetch` or `axios`.  

> ⚠️ **Note:** Accessing `http://localhost:8080` directly will not display a web page, since this port hosts the backend API (not a visual interface).  
> To explore the API, open **Swagger UI** instead:
> 👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 📚 API Documentation

### 🔐 Authentication
| Method | Endpoint | Description |
|--------|-----------|-------------|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Authenticate and receive a JWT |

### 🐾 Pets
| Method | Endpoint | Description |
|--------|-----------|-------------|
| `GET` | `/pets` | Get all pets of the authenticated user |
| `POST` | `/pets` | Create a new pet |
| `POST` | `/pets/{id}/actions/feed` | Feed a pet |
| `POST` | `/pets/{id}/actions/wash` | Wash a pet |
| `POST` | `/pets/{id}/actions/play` | Play with a pet |
| `DELETE` | `/pets/{id}` | Delete a pet |

### 🧭 Admin
| Method | Endpoint | Description |
|--------|-----------|-------------|
| `GET` | `/admin/pets` | List all pets |
| `GET` | `/admin/users` | List all users |
| `GET` | `/admin/users/{id}/pets` | View a user’s pets |
| `DELETE` | `/admin/users/{id}` | Delete a user |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-------------|
| **Frontend** | React, Vite, TailwindCSS, Axios |
| **Backend** | Java 21, Spring Boot 3.3, Maven |
| **Database** | MySQL 8 |
| **Authentication** | JWT (HS256) + BCrypt |
| **Documentation** | Swagger / OpenAPI |
| **Tools** | Docker, IntelliJ IDEA, DBeaver |
| **Testing** | JUnit 5, Mockito, MockMvc |

---

## 🚀 Installation & Setup

### 🧩 Requirements
- Java 21  
- Maven 3.9+  
- MySQL 8  
- Node.js 20+  
- Docker (optional)

### 💾 Steps

```bash
# 1. Clone the repository
git clone https://github.com/anaberod/S5.02_VirtualPet.git
cd S5.02_VirtualPet/backend

# 2. (Optional) Start MySQL via Docker
docker compose up -d

# 3. Run the backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 4. Run the frontend
cd ../frontend
npm install
npm run dev
```

- **Backend API:** http://localhost:8080  
- **Frontend UI:** http://localhost:3000  

---

## 🧪 Testing

Run all backend tests:

```bash
mvn clean test
```

All tests use the `virtualpet_test` database to keep data separate from development.

---

## 🌐 Deployment

To deploy on production servers or platforms like **Render** or **Railway**:

```bash
mvn clean package
java -jar target/backend-*.jar --spring.profiles.active=local
```

### Environment Variables
```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MINUTES
```

---

## 🪄 Logging

The backend integrates **SLF4J logging** for debugging and traceability:
- `INFO` → General application events  
- `DEBUG` → Detailed execution logs  
- `WARN` → Possible misconfigurations  
- `ERROR` → Critical failures  

Logging is configured per profile in the `application.yml` files.

---

## 🤝 Contributing

1. Fork this repository  
2. Create a new feature branch  
3. Commit and push your changes  
4. Open a Pull Request  

📦 Repository: [https://github.com/anaberod/S5.02_VirtualPet](https://github.com/anaberod/S5.02_VirtualPet)
