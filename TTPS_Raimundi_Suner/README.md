# 🐾 Sistema de Gestión de Mascotas Perdidas

Sistema web desarrollado con **Spring MVC** y **JPA/Hibernate** para la gestión y búsqueda de mascotas perdidas.

---

## 🛠 Tecnologías Necesarias

### Requisitos Previos

1. **JDK 24** o superior - [Descargar aquí](https://www.oracle.com/java/technologies/downloads/)
2. **Maven 3.6+** - [Descargar aquí](https://maven.apache.org/download.cgi)
3. **MySQL 8.0+** - [Descargar aquí](https://dev.mysql.com/downloads/mysql/)
4. **Tomcat 10+** - [Descargar aquí](https://tomcat.apache.org/download-10.cgi)

### Dependencias del Proyecto

Las dependencias se gestionan automáticamente con Maven:

- Spring Framework 6.2.11 (Core, MVC, ORM, TX, Test)
- Hibernate 7.1.4
- MySQL Connector 8.3.0
- H2 Database 2.2.224 (para tests)
- JUnit 5

---

## 🚀 Instrucciones de Uso

### 1. Clonar o descargar el proyecto

```bash
git clone [URL_DEL_REPOSITORIO]
cd TTPS_Raimundi_Suner
```

### 2. Configurar la Base de Datos

Crear la base de datos en MySQL:

```sql
CREATE DATABASE ttps_mascotas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar credenciales de MySQL

Editar el archivo `src/main/resources/META-INF/persistence.xml` con tus credenciales:

```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/ttps_mascotas"/>
<property name="jakarta.persistence.jdbc.user" value="TU_USUARIO"/>
<property name="jakarta.persistence.jdbc.password" value="TU_PASSWORD"/>
```

### 4. Compilar el proyecto

```bash
mvn clean compile
```

### 5. Ejecutar los tests (opcional)

```bash
mvn test
```

### 6. Empaquetar la aplicación

```bash
mvn clean package
```

Esto generará el archivo `TTPS_Raimundi_Suner.war` en la carpeta `target/`

### 7. Desplegar en Tomcat

1. Copiar el archivo `target/TTPS_Raimundi_Suner.war` a la carpeta `webapps` de Tomcat
2. Iniciar Tomcat:
   - Windows: Ejecutar `bin/startup.bat`
   - Linux/Mac: Ejecutar `bin/startup.sh`
3. La aplicación estará disponible en: `http://localhost:8080/TTPS_Raimundi_Suner`

### 8. Probar la API con Postman

1. Importar la colección `TTPS_Raimundi_Suner_Postman_Collection.json` en Postman
2. Los endpoints estarán disponibles en: `http://localhost:8080/TTPS_Raimundi_Suner/api`

---

## 📝 Notas Importantes

- Las tablas de la base de datos se crean automáticamente al iniciar la aplicación (Hibernate DDL auto)
- Los tests usan una base de datos H2 en memoria (no requiere configuración adicional)
- El puerto por defecto de Tomcat es 8080 (puede modificarse en la configuración de Tomcat)

---

## 👥 Autores

**Raimundi & Suñer**  
TTPS - Java  
Facultad de Informática - UNLP  
2025

