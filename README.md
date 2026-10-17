# Gestión de Órdenes para Transportistas (API)

Este repositorio contiene un ecosistema unificado y desacoplado de microservicios desarrollado con **Java 11**, **Spring Boot 2.7.18** y **Spring Cloud OpenFeign**. El sistema está completamente contenerizado en **Docker**, utiliza una base de datos relacional **PostgreSQL 15** común e incluye capas de seguridad avanzadas mediante tokens **JWT** y documentación interactiva con **Swagger UI**.

---

## 🛠️ Arquitectura y Tecnologías Utilizadas

- **Java 11** & **Spring Boot 2.7.18**: Core de desarrollo para el backend empresarial.
- **Spring Cloud OpenFeign**: Comunicación síncrona, declarativa y distribuida entre contenedores por red perimetral.
- **Spring Security & JWT (Json Web Token)**: Centralización de autenticación, firma criptográfica y filtros de seguridad en el puerto de entrada.
- **Spring Data JPA**: Abstracción de persistencia relacional y consultas masivas avanzadas basadas en `JpaSpecification`.
- **JUnit 5 & Mockito**: Suite automatizada de pruebas unitarias y de integración piramidales con control de excepciones en la memoria RAM.
- **Docker & Docker Compose**: Contenerización nativa y aislamiento de infraestructura en Linux (`eclipse-temurin:11-jre`).
- **PostgreSQL 15**: Motor de base de datos relacional para almacenamiento de entidades y  bloques multimedia (`BLOB` / `byte[]`).

---

## 📦 Componentes del Ecosistema

1. **`driver-service` (Puerto 8081)**: Microservicio interno dueño del dominio de conductores. Expone endpoints de registro y búsqueda individual requeridos por red.
2. **`order-service` (Puerto 8082)**: Microservicio perimetral orquestador (Edge / API Gateway Implícito). Controla la autenticación JWT, creación de viajes, subida de archivos multipart físicas (PDF e Imágenes) y filtros semánticos dinámicos.

---

## 🚀 Instrucciones de Despliegue y Ejecución Local

Siga estos pasos secuenciales en su terminal de comandos (CMD/Bash) para compilar y encender la infraestructura distribuida de forma automática desde cero:

### 1. Clonar el repositorio y acceder a la raíz
```bash
git clone https://github.com/sergioahdez83/viaje-backend.git
cd viaje-backend
```

### 2. Limpieza de contenedores huérfanos anteriores
Para liberar puertos físicos y asegurar que la base de datos inicie completamente limpia y en blanco:
```bash
docker rm -f viaje-postgres viaje-driver-app viaje-order-app
docker system prune -f
```

### 3. Compilación e Inserción de ejecutables binarios (.jar)
Debido al desacoplamiento físico de los proyectos, es indispensable empaquetar de forma secuencial ingresando a sus respectivas carpetas raíz donde residen los archivos `pom.xml`:

```bash
# Compilar Servicio de Conductores
cd driver-service
mvn clean package -DskipTests
cd ..

# Compilar Servicio de Órdenes y Multimedia
cd order-service
mvn clean package -DskipTests
cd ..
```
*(Asegúrese de observar el mensaje verde de **BUILD SUCCESS** al concluir cada compilador).*

### 4. Encendido e Inyección del Orquestador Maestro en Docker
Regrese a la raíz del proyecto (donde se encuentra el archivo `docker-compose.yml`) y ejecute el comando mágico para inicializar la red virtual y los contenedores en paralelo:
```bash
docker-compose up --build
```
*(Verá cómo se inicializa el motor relacional de Postgres y los dos servidores de Spring Boot quedan escuchando de forma estable en sus respectivos puertos de red).*

---

## 🧪 Circuito de Pruebas Extremo a Extremo en Swagger UI

Una vez que los logotipos de Spring Boot estén estables en su terminal, abra su navegador web (Chrome, Edge, etc.) y ejecute el flujo de negocio distribuido:

### Fase A: Autenticación JWT y Apertura de Candados
1. Ingrese a la documentación de órdenes: `http://localhost:8082/swagger-ui/index.html`
2. Despliega **`POST /api/auth/login`**, haga clic en **Try it out**, ingrese el usuario `admin` (Ruta pública permitida por el filtro perimetral) y presione **Execute**.
3. **Copie el token JWT puro** largo devuelto en la respuesta inferior sin las comillas de los extremos.
4. Suba al inicio de la interfaz de Swagger, presione el botón gris **Authorize**, pegue el token directo en la caja de texto y presione **Authorize -> Close**. Los recursos privados han quedado desbloqueados.

### Fase B: Alta del Conductor en la Fuente de la Verdad
1. Abra una nueva pestaña e ingrese a conductores: `http://localhost:8081/swagger-ui/index.html`
2. Despliega **`POST /api/drivers`**, haga clic en **Try it out**, pegue el siguiente JSON estructurado y presione **Execute**:
   ```json
   {
     "name": "Sergio Hernandez",
     "licenseNumber": "MX-2026-SH",
     "active": true
   }
   ```
3. **Copie el UUID (`id`) largo que se genera en el cuerpo del JSON de respuesta (`201 Created`)**.

### Fase C: Generación del Viaje Inicial
1. Regrese a la pestaña de órdenes (`http://localhost:8082`, puerto de entrada seguro).
2. Despliega **`POST /api/orders`**, presione **Try it out**, pegue el JSON de ruta corporativa y presione **Execute**:
   ```json
   {
     "origin": "Ciudad de Mexico",
     "destination": "Monterrey"
   }
   ```
3. **Copie el UUID (`id`) largo de la orden recién generada en estado `CREATED`**.

### Fase D: Asignación Multimedia Distribuida por OpenFeign
1. Despliega el endpoint de negocio: **`POST /api/orders/{id}/assign-driver`**.
2. Presione **Try it out** y complete los parámetros requeridos de forma estricta:
   - En la caja **`id` (Ruta de red)**: Pegue el UUID de la orden copiado en la Fase C.
   - En la caja **`driverId` (Query Param)**: Pegue el UUID del conductor copiado en la Fase B.
3. **Adjunte los documentos físicos reales de su computadora**:
   - En el campo **`pdf`**: Cargue cualquier archivo `.pdf` que funcione de guía de viaje.
   - En el campo **`image`**: Cargue cualquier imagen de formato `.png` o `.jpg` que sirva de evidencia de firma (Asegúrese de que pese menos de **1 MB**).
4. Haga clic en el botón azul grande **Execute**.

### 🎯 Resultado Esperado y Persistencia en DBeaver
El `order-service` interceptará el token, viajará de manera síncrona e invisible por debajo de la red virtual de Docker tocando el puerto `8081` para validar que el conductor exista y esté activo en su base de datos aislada. 

Al recibir la confirmación de red, mutará el estado de la orden automáticamente a **`IN_TRANSIT`**, procesará los archivos adjuntos convirtiéndolos en arreglos de bytes planos y retornará un rotundo **`200 OK`**. Si abre su gestor de bases de datos relacionales (**DBeaver**), comprobará que las columnas vacías se habrán llenado con los objetos binarios `[BLOB]` correspondientes de forma permanente.