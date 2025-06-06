# 🏦 xBank-spring-backend

**xBank** es un proyecto de demostración de una aplicación bancaria.  
Está construido con **Spring Boot (Java 21)** en el backend y **Vue.js** en el frontend.

Ofrece una API REST segura (JWT) para gestionar usuarios, cuentas y operaciones bancarias.

---

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot**
- **JWT (JSON Web Tokens)** para autenticación
- **JUnit + Maven** para testing
- **Docker** para ejecutar la aplicación

---

## 📦 Instalación y ejecución

Asegurate de tener [Docker](https://www.docker.com/) instalado.

```bash
docker-compose up --build
```

También puedes usar Makefile para desplegar la aplicación con el comando

```bash
make deploy
```