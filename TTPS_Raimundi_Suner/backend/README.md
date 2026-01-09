# 🐾 Sistema de Gestión de Mascotas Perdidas - Where Are You?, Come Home

Sistema web desarrollado con **Spring Boot**, **Angular** y **JPA/Hibernate** para la gestión y búsqueda de mascotas perdidas.

---

## 🛠 Tecnologías Utilizadas

### Stack Tecnológico

- **Backend:** Spring Boot 3.4.0 + JPA/Hibernate
- **Frontend:** Angular 21 + TypeScript
- **Base de Datos:** MySQL 8.0+
- **Build:** Maven 3.6+ (Backend) + npm (Frontend)

### Requisitos Previos

1. **JDK 21** o superior - [Descargar aquí](https://adoptium.net/)
2. **Maven 3.6+** - Incluido en el proyecto (mvnw)
3. **MySQL 8.0+** - [Descargar aquí](https://dev.mysql.com/downloads/mysql/)
4. **Node.js 22+** - Se instala automáticamente via Maven Frontend Plugin

---

## 🚀 Instrucciones de Configuración Inicial

### 1. Clonar el repositorio

```bash
git clone [URL_DEL_REPOSITORIO]
cd TTPS_Raimundi_Suner
```

### 2. Configurar la Base de Datos MySQL

**Importante:** Asegúrate de tener MySQL corriendo antes de continuar.

Crear la base de datos en MySQL:

```sql
CREATE DATABASE mascotas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar Credenciales (IMPORTANTE - NO OMITIR)

El archivo con credenciales reales NO está en Git por seguridad. Debes crearlo:

**Opción A: Copiar desde el ejemplo (Windows CMD)**
```bash
copy src\main\resources\application.properties.example src\main\resources\application.properties
```

**Opción B: Copiar desde el ejemplo (PowerShell/Linux/Mac)**
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

**Opción C: Crear manualmente**
Crear el archivo `src/main/resources/application.properties` y copiar el contenido de `application.properties.example`

Luego, **editar** `src/main/resources/application.properties` y reemplazar:
```properties
spring.datasource.password=PASSWORD_AQUI
```

Por tu contraseña real de MySQL:
```properties
spring.datasource.password=TuPasswordReal
```

Si tu usuario de MySQL no es `root`, también cambia:
```properties
spring.datasource.username=tu_usuario
```

### 4. Instalar dependencias del frontend (Opcional)

El build de Maven se encarga automáticamente, pero si quieres desarrollar el frontend por separado:

```bash
cd frontend
npm install
cd ..
```


---

## ▶️ Ejecutar la Aplicación

### Método Rápido: Script de inicio automático

```bash
iniciar.bat
```

Este script:
1. Inicia el backend Spring Boot en puerto 8080
2. Inicia el frontend Angular en puerto 4200
3. Abre dos ventanas de terminal (una para cada servicio)

### Método Manual: Backend y Frontend por separado

**Terminal 1 - Backend:**
```bash
mvnw.cmd spring-boot:run
```

**Terminal 2 - Frontend (desarrollo con hot-reload):**
```bash
cd frontend
npm start
```

### Acceder a la aplicación

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/swagger-ui.html

---

## 🧪 Testing

### Ejecutar tests del backend

```bash
mvnw.cmd test
```

Los tests usan una base de datos H2 en memoria (no requiere configuración adicional).

---

## 📦 Build para Producción

### Compilar todo el proyecto (Backend + Frontend)

```bash
mvnw.cmd clean package
```

Esto genera:
- Frontend compilado → `src/main/resources/static/browser/`
- WAR del proyecto → `target/TTPS_Raimundi_Suner.war`

### Desplegar en servidor

El archivo WAR generado puede desplegarse en:
- Tomcat 10+
- Cualquier servidor compatible con Jakarta EE

---

## 📂 Estructura del Proyecto

```
TTPS_Raimundi_Suner/
├── frontend/              # Aplicación Angular
│   ├── src/
│   │   ├── app/          # Componentes y servicios
│   │   └── environments/ # Configuración de entornos
│   └── package.json
├── src/main/
│   ├── java/APP/
│   │   ├── controllers/  # REST Controllers
│   │   ├── models/       # Entidades JPA
│   │   ├── services/     # Lógica de negocio
│   │   └── dto/          # Data Transfer Objects
│   └── resources/
│       ├── application.properties.example  # Template de configuración
│       └── static/       # Frontend compilado (generado)
└── pom.xml               # Configuración Maven
```

---

## ⚠️ Notas Importantes

### Seguridad y Credenciales

- ❌ **NUNCA** subir `application.properties` a Git (contiene credenciales)
- ✅ El `.gitignore` ya está configurado para excluirlo
- ✅ Usa `application.properties.example` como plantilla
- 🔒 Cada desarrollador debe crear su propio `application.properties` local

### Base de Datos

- Las tablas se crean automáticamente (Hibernate DDL auto=update)
- El nombre de la base de datos es: `mascotas_db`
- Puerto por defecto de MySQL: 3306

### Desarrollo

- Backend corre en puerto: **8080**
- Frontend (dev) corre en puerto: **4200**
- Hot-reload habilitado en ambos entornos

---

## 🆘 Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"
- Verifica que MySQL esté corriendo
- Verifica usuario y contraseña en `application.properties`
- Verifica que la base de datos `mascotas_db` exista

### Error: "Port 8080 already in use"
- Otro proceso está usando el puerto 8080
- Detén el proceso o cambia el puerto en `application.properties`:
  ```properties
  server.port=8081
  ```

### Frontend en blanco
- Verifica que el backend esté corriendo en http://localhost:8080
- Abre la consola del navegador (F12) para ver errores
- Verifica que las rutas del API sean correctas en `environments/environment.ts`

### "Node not found" al ejecutar Maven
- Maven instala Node.js automáticamente en `frontend/node/`
- Si falla, ejecuta: `mvnw.cmd clean install`

---

## 👥 Autores

**Raimundi & Suñer**  
TTPS - Java  
Facultad de Informática - UNLP  
2026

