# 📚 LibroCatalogue – Backend

This is the backend service for **LibroCatalogue**, a full-stack application to manage and catalog books.

## Tech Stack
- Java 21
- Spring Boot (Web, Security, JPA, Validation)
- MySQL
- JWT Authentication
- Cloudinary (for cover upload)

## Project Structure
- `controller/` → REST endpoints
- `service/` → Business logic
- `repository/` → Database access
- `model/` → Entities (Book, Collection, Tag, User, …)

## Setup
Edit src/main/resources/application.properties:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/librocatalogue
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD

cloudinary.cloud_name=...
cloudinary.api_key=...
cloudinary.api_secret=...

#to register a new user when running the application, set a registration code
app.registration.secret=...
```
### Prerequisites
- Java 21+
- Maven
- MySQL

### Database
```sql
CREATE DATABASE libro_catalogue;


