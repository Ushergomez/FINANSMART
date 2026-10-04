# Finansmart

Aplicación web de gestión financiera desarrollada con Java 17, Spring Boot, Maven, MySQL y Docker.

## Requisitos

- Docker Desktop instalado y ejecutándose.

No es necesario instalar Java, Maven ni MySQL manualmente.

## Ejecución

Desde la raíz del proyecto ejecutar:

```bash
docker compose up --build
```

La aplicación estará disponible en:

http://localhost:8080

## Base de datos

MySQL se ejecuta en un contenedor llamado `finansmart-mysql`.

- Base de datos: `finansmart`
- Usuario: `root`
- Contraseña: `1234`
- Puerto expuesto: `3306`

El esquema inicial se carga desde:

```text
database/finansmart.sql
```

## Detener la aplicación

Presionar `Ctrl + C` en la terminal donde se ejecutó Docker Compose.

Para detener y eliminar los contenedores:

```bash
docker compose down
```

## Tecnologías

- Java 17
- Spring Boot 3
- Maven
- MySQL 8
- Docker y Docker Compose
- Thymeleaf
