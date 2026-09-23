# NexaBank API — Arquitectura y especificación funcional

## 1. Propósito

NexaBank será una plataforma financiera full-stack orientada a reemplazar, desde el punto de vista funcional de la aplicación, el uso de múltiples wallets y aplicaciones bancarias en una sola experiencia.

Esta especificación define el backend `nexabank-api`, construido con Spring Boot, que:

- expone una API REST sobre HTTP/JSON;
- persiste información financiera en Firebase Firestore;
- autentica y autoriza usuarios;
- gestiona usuarios, cuentas, transacciones, beneficiarios y transferencias;
- expone los algoritmos académicos de `nexabank-core`;
- permite ejecutar búsquedas y ordenamientos sobre datos reales;
- consulta y visualiza resultados de benchmarks JMH;
- mantiene `nexabank-core` como Java puro, independiente de Spring y Firebase.

La arquitectura se inspira en la guía oficial de Spring para construir servicios REST, especialmente en el uso de controladores MVC, métodos HTTP apropiados, manejo de recursos, evolución compatible de la API y HATEOAS para comunicar acciones disponibles. La guía oficial recomienda Java 17 o posterior y muestra `GET`, `POST`, `PUT` y `DELETE` como operaciones HTTP apropiadas para recursos REST.

Referencia oficial:
https://spring.io/guides/tutorials/rest

---

# 2. Principio arquitectónico principal

La regla más importante del proyecto es:

> `nexabank-core` es el motor académico. `nexabank-api` lo envuelve y lo expone.

Nunca se debe invertir esa dependencia.

```text
                         FRONTEND
                            |
                       HTTP / JSON
                            |
                            v
                    +---------------+
                    | nexabank-api  |
                    |  Spring Boot  |
                    +-------+-------+
                            |
             +--------------+--------------+
             |                             |
             v                             v
     +---------------+             +---------------+
     | nexabank-core |             |   Firebase    |
     | Java puro     |             |   Firestore   |
     +---------------+             +---------------+
             |
             v
       JMH Benchmarks
```

Dependencia permitida:

```text
nexabank-api
      |
      v
nexabank-core
```

Dependencias prohibidas dentro de `nexabank-core`:

```text
Spring Boot       ❌
Spring MVC        ❌
Firebase SDK      ❌
Firestore         ❌
Spring Security   ❌
HTTP              ❌
REST              ❌
```

---

# 3. Componentes del sistema

## 3.1 `nexabank-core`

Contiene exclusivamente las estructuras y algoritmos de la Entrega 1:

```text
DynamicArray
MergeSort
QuickSort
BinarySearch
```

Responsabilidades:

- estructuras de datos;
- algoritmos;
- pruebas unitarias;
- análisis de complejidad;
- operaciones sobre datos ya cargados;
- código que será medido por JMH.

No conoce:

- usuarios;
- HTTP;
- Firebase;
- JWT;
- Spring;
- controladores;
- DTOs.

---

## 3.2 `nexabank-api`

Es el backend de producción.

Responsabilidades:

- API REST;
- autenticación;
- autorización;
- validación;
- lógica de negocio;
- acceso a Firestore;
- transformación DTO ↔ dominio;
- integración con `nexabank-core`;
- ejecución controlada de algoritmos;
- exposición de benchmarks;
- manejo de errores;
- versionado;
- auditoría.

---

## 3.3 Frontend

El frontend consume exclusivamente `nexabank-api`.

No debe acceder directamente a Firestore para operaciones de negocio.

```text
Frontend
    |
    | HTTPS
    v
NexaBank API
    |
    v
Firebase
```

Esto mantiene las reglas financieras y de seguridad en el servidor.

---

# 4. Arquitectura interna de `nexabank-api`

```text
nexabank-api/
└── src/main/java/com/nexabank/api/
    │
    ├── controller/
    │   ├── AuthController.java
    │   ├── UserController.java
    │   ├── AccountController.java
    │   ├── TransactionController.java
    │   ├── TransferController.java
    │   ├── BeneficiaryController.java
    │   ├── AlgorithmController.java
    │   └── BenchmarkController.java
    │
    ├── service/
    │   ├── AuthService.java
    │   ├── UserService.java
    │   ├── AccountService.java
    │   ├── TransactionService.java
    │   ├── TransferService.java
    │   ├── BeneficiaryService.java
    │   ├── AlgorithmService.java
    │   └── BenchmarkService.java
    │
    ├── repository/
    │   ├── UserRepository.java
    │   ├── AccountRepository.java
    │   ├── TransactionRepository.java
    │   ├── TransferRepository.java
    │   ├── BeneficiaryRepository.java
    │   └── BenchmarkRepository.java
    │
    ├── dto/
    │   ├── auth/
    │   ├── user/
    │   ├── account/
    │   ├── transaction/
    │   ├── transfer/
    │   ├── algorithm/
    │   └── benchmark/
    │
    ├── model/
    │   ├── User.java
    │   ├── Account.java
    │   ├── Transaction.java
    │   ├── Transfer.java
    │   └── Beneficiary.java
    │
    ├── mapper/
    │
    ├── security/
    │   ├── FirebaseAuthenticationFilter.java
    │   └── SecurityConfig.java
    │
    ├── config/
    │   ├── FirebaseConfig.java
    │   └── JacksonConfig.java
    │
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── ResourceNotFoundException.java
    │   ├── InsufficientFundsException.java
    │   ├── InvalidTransactionException.java
    │   └── UnauthorizedOperationException.java
    │
    └── NexabankApiApplication.java
```

---

# 5. Flujo obligatorio de una petición

La API seguirá esta dirección:

```text
HTTP Request
     |
     v
Controller
     |
     v
DTO / Validation
     |
     v
Service
     |
     +----------+
     |          |
     v          v
Core       Repository
     |          |
     |          v
     |       Firestore
     |
     v
Algorithm
     |
     v
Response DTO
     |
     v
HTTP Response
```

## Regla

Los controllers no deben contener lógica financiera compleja.

Incorrecto:

```text
Controller
 ├── valida saldo
 ├── modifica cuenta
 ├── crea transacción
 ├── actualiza balance
 └── escribe Firebase
```

Correcto:

```text
Controller
    ↓
TransferService
    ↓
validaciones
    ↓
AccountRepository
    ↓
TransactionRepository
    ↓
Firebase
```

---

# 6. Stack tecnológico

## Backend

- Java 17+
- Spring Boot
- Spring Web
- Spring Validation
- Spring Security
- Spring HATEOAS
- Firebase Admin SDK
- Cloud Firestore
- JUnit
- JMH

## Comunicación

```text
HTTPS
REST
JSON
```

## Base de datos

```text
Firebase Cloud Firestore
```

## Algoritmos

```text
nexabank-core
```

---

# 7. REST API

La API será versionada:

```text
/api/v1
```

Ejemplos:

```text
/api/v1/users
/api/v1/accounts
/api/v1/transactions
/api/v1/transfers
```

No se deben crear rutas como:

```text
/api/getUsers
/api/createTransaction
/api/deleteAccount
```

Se utilizan recursos y métodos HTTP:

```text
GET
POST
PUT
DELETE
```

Ejemplo:

```text
GET    /api/v1/accounts
GET    /api/v1/accounts/{id}
POST   /api/v1/accounts
PUT    /api/v1/accounts/{id}
DELETE /api/v1/accounts/{id}
```

---

# 8. Recursos principales

```text
users
accounts
transactions
transfers
beneficiaries
benchmarks
algorithm-runs
```

Relación:

```text
User
 |
 +---- Account
          |
          +---- Transaction
          |
          +---- Transfer
          |
          +---- Beneficiary
```

---

# 9. Usuarios

## GET /api/v1/users/me

Obtiene el usuario autenticado.

Respuesta:

```json
{
  "id": "USR-000001",
  "email": "user@example.com",
  "displayName": "Usuario Nexa",
  "status": "ACTIVE",
  "_links": {
    "self": {
      "href": "/api/v1/users/me"
    },
    "accounts": {
      "href": "/api/v1/accounts"
    }
  }
}
```

## PUT /api/v1/users/me

Actualiza información editable del usuario.

No se debe permitir cambiar desde este endpoint:

```text
id
status
roles
createdAt
security metadata
```

---

# 10. Cuentas

## GET /api/v1/accounts

Lista las cuentas del usuario autenticado.

## GET /api/v1/accounts/{accountId}

Obtiene una cuenta.

## POST /api/v1/accounts

Crea una cuenta.

Ejemplo:

```json
{
  "type": "SAVINGS",
  "currency": "COP",
  "name": "Cuenta principal"
}
```

Respuesta:

```json
{
  "id": "ACC-000001",
  "type": "SAVINGS",
  "currency": "COP",
  "name": "Cuenta principal",
  "balance": 0.00,
  "status": "ACTIVE"
}
```

---

# 11. Reglas financieras de cuentas

Una cuenta debe tener:

```text
balance >= 0
```

salvo que explícitamente se implemente un producto que permita sobregiro.

El backend nunca debe confiar en un balance enviado por el frontend.

Incorrecto:

```json
{
  "balance": 5000000
}
```

El servidor no debe aceptar que el cliente establezca directamente el saldo.

El balance se modifica únicamente mediante operaciones financieras válidas.

---

# 12. Transacciones

## GET /api/v1/transactions

Lista transacciones.

Filtros permitidos:

```text
accountId
type
category
status
from
to
page
size
```

Ejemplo:

```text
GET /api/v1/transactions?accountId=ACC-000001&type=EXPENSE&page=0&size=20
```

## GET /api/v1/transactions/{transactionId}

Obtiene una transacción.

## POST /api/v1/transactions

Registra una operación financiera válida.

Ejemplo:

```json
{
  "accountId": "ACC-000001",
  "type": "EXPENSE",
  "amount": 85000.00,
  "currency": "COP",
  "category": "FOOD",
  "description": "Compra supermercado"
}
```

---

# 13. Tipos de transacción

Inicialmente:

```text
INCOME
EXPENSE
TRANSFER
```

Estados:

```text
PENDING
COMPLETED
FAILED
CANCELLED
```

Categorías pueden evolucionar sin romper documentos existentes.

---

# 14. Regla fundamental de transacciones

Nunca se debe borrar físicamente una transacción financiera completada.

Incorrecto:

```text
DELETE /transactions/TX-001
```

para eliminar historial financiero.

La API debe conservar trazabilidad.

Para reversar una operación se crea una operación compensatoria o se utiliza un flujo de reversión explícito.

---

# 15. Transferencias

Las transferencias son operaciones de negocio, no simples documentos CRUD.

Endpoint:

```text
POST /api/v1/transfers
```

Body:

```json
{
  "sourceAccountId": "ACC-000001",
  "destinationAccountId": "ACC-000002",
  "amount": 250000.00,
  "currency": "COP",
  "description": "Transferencia"
}
```

Flujo:

```text
TransferController
       |
       v
TransferService
       |
       +--> validar usuario
       |
       +--> validar cuentas
       |
       +--> validar moneda
       |
       +--> validar monto
       |
       +--> validar saldo
       |
       +--> debitar origen
       |
       +--> acreditar destino
       |
       +--> crear transacciones
       |
       +--> guardar auditoría
       |
       v
    resultado
```

---

# 16. Idempotencia

Las operaciones financieras deben soportar idempotencia.

Para:

```text
POST /api/v1/transfers
```

el cliente debe enviar:

```http
Idempotency-Key: 7f0f3b2a-...
```

Si el mismo request llega dos veces con la misma clave, el servidor no debe ejecutar dos transferencias.

Debe devolver el resultado de la operación original.

Esto evita problemas como:

```text
Usuario pulsa "Enviar"
       ↓
request 1
       ↓
request 2
       ↓
dos transferencias
```

---

# 17. Beneficiarios

Endpoints:

```text
GET    /api/v1/beneficiaries
GET    /api/v1/beneficiaries/{id}
POST   /api/v1/beneficiaries
PUT    /api/v1/beneficiaries/{id}
DELETE /api/v1/beneficiaries/{id}
```

Ejemplo:

```json
{
  "name": "Carlos Pérez",
  "accountReference": "ACC-000002",
  "alias": "Carlos"
}
```

No se debe almacenar información bancaria innecesaria.

---

# 18. Algoritmos

Los algoritmos de la Entrega 1 se exponen como servicios, pero permanecen dentro de `nexabank-core`.

## POST /api/v1/algorithms/sort

Request:

```json
{
  "algorithm": "MERGE_SORT",
  "field": "amount",
  "order": "ASC"
}
```

Algoritmos:

```text
MERGE_SORT
QUICK_SORT
```

Respuesta:

```json
{
  "algorithm": "MERGE_SORT",
  "field": "amount",
  "order": "ASC",
  "inputSize": 5000,
  "executionTimeNanos": 184520,
  "data": []
}
```

---

# 19. Búsqueda

## POST /api/v1/algorithms/search

Request:

```json
{
  "algorithm": "BINARY_SEARCH",
  "field": "id",
  "value": "TX-004582"
}
```

Respuesta:

```json
{
  "algorithm": "BINARY_SEARCH",
  "target": "TX-004582",
  "found": true,
  "position": 4582,
  "executionTimeNanos": 823
}
```

Regla:

> Binary Search solo puede ejecutarse sobre datos ordenados respecto al criterio de búsqueda.

---

# 20. DynamicArray

`DynamicArray` no debe exponerse como una estructura persistente de Firebase.

Es una estructura de procesamiento.

Flujo:

```text
Firestore
    |
    v
Java Collection / DTO
    |
    v
DynamicArray
    |
    +--> MergeSort
    |
    +--> QuickSort
    |
    +--> BinarySearch
```

No crear:

```text
dynamicArrays/
```

en Firestore.

La estructura pertenece al procesamiento del backend, no al almacenamiento.

---

# 21. Benchmarks JMH

JMH mide el motor algorítmico.

No debe medir accidentalmente:

```text
Firebase
HTTP
JSON
Spring
latencia de red
```

si el objetivo es estudiar la complejidad del algoritmo.

Por lo tanto:

```text
Firebase
    |
    | cargar dataset
    v
Memoria
    |
    v
JMH
    |
    v
nexabank-core
```

El tiempo del algoritmo comienza cuando JMH ejecuta la operación.

---

# 22. Benchmarks almacenados

Colección:

```text
benchmark_runs
```

Ejemplo:

```json
{
  "id": "RUN-20260918-001",
  "framework": "JMH",
  "javaVersion": "21",
  "executedAt": "2026-09-18T03:00:00Z",
  "datasetSource": "firebase",
  "datasetSize": 5000,
  "status": "COMPLETED"
}
```

Colección:

```text
benchmark_results
```

Ejemplo:

```json
{
  "id": "RESULT-000001",
  "runId": "RUN-20260918-001",
  "algorithm": "MERGE_SORT",
  "operation": "SORT",
  "inputSize": 5000,
  "score": 184520.42,
  "scoreError": 3210.55,
  "unit": "ns/op"
}
```

---

# 23. Endpoints de benchmarks

```text
GET /api/v1/benchmarks
GET /api/v1/benchmarks/{runId}
GET /api/v1/benchmarks/{runId}/results
```

Opcional para administración:

```text
POST /api/v1/benchmarks/runs
```

La ejecución pesada de JMH no debe bloquear una petición HTTP normal.

---

# 24. Firebase Firestore

Colecciones finales:

```text
users
accounts
transactions
transfers
beneficiaries
benchmark_runs
benchmark_results
idempotency_keys
audit_logs
```

---

# 25. Documento `users`

```json
{
  "id": "USR-000001",
  "email": "user@example.com",
  "displayName": "Usuario Nexa",
  "phone": "+573000000000",
  "status": "ACTIVE",
  "roles": [
    "USER"
  ],
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp",
  "lastLoginAt": "Timestamp"
}
```

No guardar contraseñas en Firestore si se utiliza Firebase Authentication.

---

# 26. Documento `accounts`

```json
{
  "id": "ACC-000001",
  "userId": "USR-000001",
  "type": "SAVINGS",
  "name": "Cuenta principal",
  "currency": "COP",
  "balance": 1500000.00,
  "status": "ACTIVE",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

---

# 27. Documento `transactions`

```json
{
  "id": "TX-000001",
  "accountId": "ACC-000001",
  "userId": "USR-000001",
  "type": "EXPENSE",
  "category": "FOOD",
  "amount": 85000.00,
  "currency": "COP",
  "description": "Compra supermercado",
  "status": "COMPLETED",
  "timestamp": "Timestamp",
  "createdAt": "Timestamp"
}
```

---

# 28. Documento `transfers`

```json
{
  "id": "TRF-000001",
  "sourceAccountId": "ACC-000001",
  "destinationAccountId": "ACC-000002",
  "sourceUserId": "USR-000001",
  "destinationUserId": "USR-000002",
  "amount": 250000.00,
  "currency": "COP",
  "status": "COMPLETED",
  "idempotencyKey": "uuid",
  "description": "Transferencia",
  "createdAt": "Timestamp",
  "completedAt": "Timestamp"
}
```

---

# 29. Documento `beneficiaries`

```json
{
  "id": "BEN-000001",
  "userId": "USR-000001",
  "name": "Carlos Pérez",
  "alias": "Carlos",
  "accountReference": "ACC-000002",
  "status": "ACTIVE",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

---

# 30. Documento `idempotency_keys`

```json
{
  "id": "IDEMP-000001",
  "key": "uuid",
  "userId": "USR-000001",
  "operation": "CREATE_TRANSFER",
  "requestHash": "hash",
  "status": "COMPLETED",
  "responseReference": "TRF-000001",
  "createdAt": "Timestamp",
  "expiresAt": "Timestamp"
}
```

---

# 31. Documento `audit_logs`

Toda operación financiera relevante debe generar auditoría.

```json
{
  "id": "AUD-000001",
  "userId": "USR-000001",
  "action": "TRANSFER_CREATED",
  "resourceType": "TRANSFER",
  "resourceId": "TRF-000001",
  "status": "SUCCESS",
  "timestamp": "Timestamp",
  "metadata": {
    "sourceAccountId": "ACC-000001",
    "destinationAccountId": "ACC-000002"
  }
}
```

No almacenar secretos ni tokens en `metadata`.

---

# 32. Seguridad

Todas las rutas privadas requieren autenticación.

```text
Public
 ├── health
 └── authentication-related endpoints

Private
 ├── users
 ├── accounts
 ├── transactions
 ├── transfers
 ├── beneficiaries
 └── personal algorithm operations
```

El backend debe verificar:

```text
Token
  ↓
Usuario
  ↓
Recurso
  ↓
Permiso
```

No basta con comprobar que el usuario está autenticado.

Ejemplo:

```text
Usuario A
   |
   X
   |
GET /accounts/ACC-DE-USUARIO-B
```

Debe responder:

```text
403 Forbidden
```

---

# 33. Autenticación con Firebase

Flujo:

```text
Frontend
   |
   | Login
   v
Firebase Authentication
   |
   | ID Token
   v
Frontend
   |
   | Authorization: Bearer TOKEN
   v
Spring Boot
   |
   v
Firebase Admin SDK
   |
   v
Token válido
```

Spring Security utilizará el resultado para construir el contexto autenticado.

---

# 34. HTTP Status Codes

La API debe utilizar códigos HTTP coherentes.

```text
200 OK
```

Consulta o actualización exitosa.

```text
201 Created
```

Recurso creado.

```text
204 No Content
```

Operación exitosa sin contenido.

```text
400 Bad Request
```

Request inválido.

```text
401 Unauthorized
```

No autenticado.

```text
403 Forbidden
```

Autenticado pero sin permisos.

```text
404 Not Found
```

Recurso inexistente.

```text
409 Conflict
```

Conflicto de estado o idempotencia.

```text
422 Unprocessable Entity
```

Datos válidos sintácticamente pero inválidos para la regla de negocio.

```text
429 Too Many Requests
```

Rate limit.

```text
500 Internal Server Error
```

Error inesperado.

---

# 35. Formato estándar de errores

Toda la API debe utilizar una estructura consistente:

```json
{
  "timestamp": "2026-09-18T03:00:00Z",
  "status": 422,
  "error": "INSUFFICIENT_FUNDS",
  "message": "Insufficient funds for this operation",
  "path": "/api/v1/transfers",
  "traceId": "abc123"
}
```

Nunca devolver stack traces al cliente.

---

# 36. Validación

Los DTOs deben validar entradas.

Ejemplo conceptual:

```text
amount:
  > 0

currency:
  obligatorio

accountId:
  obligatorio

description:
  longitud máxima definida
```

Las validaciones de formato pueden hacerse con Bean Validation.

Las reglas financieras deben permanecer en `Service`.

Ejemplo:

```text
@NotNull
@NotBlank
@Positive
```

para validaciones estructurales.

Y:

```text
¿La cuenta pertenece al usuario?
¿Tiene saldo?
¿La cuenta está activa?
```

para lógica de negocio.

---

# 37. HATEOAS

La API puede utilizar Spring HATEOAS para incluir acciones disponibles.

Ejemplo:

```json
{
  "id": "ACC-000001",
  "status": "ACTIVE",
  "balance": 1500000.00,
  "_links": {
    "self": {
      "href": "/api/v1/accounts/ACC-000001"
    },
    "transactions": {
      "href": "/api/v1/transactions?accountId=ACC-000001"
    },
    "transfer": {
      "href": "/api/v1/transfers"
    }
  }
}
```

Las acciones disponibles pueden depender del estado del recurso.

Por ejemplo, una cuenta `ACTIVE` puede tener una acción determinada que una cuenta `BLOCKED` no tenga.

Esto sigue el enfoque explicado por la documentación oficial de Spring HATEOAS: los links pueden comunicar al cliente qué operaciones están disponibles sin obligarlo a codificar todas las reglas de transición. 

---

# 38. Evolución de la API

Nunca eliminar un campo inmediatamente.

Si aparece:

```json
{
  "name": "Breiner"
}
```

y posteriormente se necesita:

```json
{
  "displayName": "Breiner"
}
```

se mantiene compatibilidad durante la transición.

Regla:

```text
NO romper clientes existentes sin una estrategia de migración.
```

La guía oficial de Spring recomienda conservar campos antiguos y utilizar relaciones/links para permitir que los clientes evolucionen sin quedar atados a URIs o estructuras internas rígidas.

---

# 39. Paginación

No se debe retornar una colección financiera ilimitada.

Incorrecto:

```text
GET /api/v1/transactions
→ 5.000.000 registros
```

Correcto:

```text
GET /api/v1/transactions?page=0&size=20
```

Respuesta conceptual:

```json
{
  "data": [],
  "page": 0,
  "size": 20,
  "totalElements": 5000,
  "totalPages": 250
}
```

Los algoritmos de la Entrega 1 pueden ejecutarse sobre datasets seleccionados explícitamente.

---

# 40. Ordenamiento REST vs algoritmos académicos

No mezclar:

```text
GET /transactions?sort=amount
```

con:

```text
POST /algorithms/sort
```

El primero es una necesidad normal de navegación del API.

El segundo existe para demostrar:

```text
DynamicArray
MergeSort
QuickSort
```

Esto mantiene separado:

```text
funcionalidad de producto
        vs
funcionalidad académica
```

---

# 41. Seguridad de datos

Reglas obligatorias:

- HTTPS en producción.
- No almacenar contraseñas en Firestore.
- No guardar tokens en Firestore.
- No guardar secretos en Git.
- No exponer credenciales de Firebase.
- Validar ownership de cuentas.
- Validar autorización en cada operación.
- No confiar en balances enviados por el cliente.
- Registrar operaciones financieras importantes.
- Implementar idempotencia para operaciones sensibles.
- No devolver información de otros usuarios.
- No devolver stack traces.
- Aplicar rate limiting en endpoints sensibles.

---

# 42. Configuración

Nunca colocar secretos directamente en:

```text
application.properties
```

Ejemplo conceptual:

```properties
firebase.project-id=${FIREBASE_PROJECT_ID}
firebase.client-email=${FIREBASE_CLIENT_EMAIL}
firebase.private-key=${FIREBASE_PRIVATE_KEY}
```

Los valores reales deben proceder del entorno seguro de despliegue.

---

# 43. Variables de entorno

```text
FIREBASE_PROJECT_ID
FIREBASE_CLIENT_EMAIL
FIREBASE_PRIVATE_KEY
FIREBASE_STORAGE_BUCKET
```

También:

```text
SPRING_PROFILES_ACTIVE
SERVER_PORT
```

---

# 44. Git

Nunca subir:

```text
*.json
```

si contienen credenciales.

Especialmente:

```text
firebase-service-account.json
```

Debe estar en:

```text
.gitignore
```

Ejemplo:

```text
.env
*.key
*.pem
firebase-service-account.json
```

---

# 45. Pruebas

Debe existir como mínimo:

```text
Unit Tests
Integration Tests
API Tests
Security Tests
Algorithm Tests
```

## Core

```text
DynamicArrayTest
MergeSortTest
QuickSortTest
BinarySearchTest
```

## API

```text
AccountControllerTest
TransactionControllerTest
TransferControllerTest
AlgorithmControllerTest
```

---

# 46. Regla de pruebas de algoritmos

Las pruebas del algoritmo no deben depender de Firebase.

Incorrecto:

```text
MergeSortTest
    ↓
Firebase
```

Correcto:

```text
MergeSortTest
    ↓
Java puro
    ↓
MergeSort
```

Así la complejidad académica permanece aislada.

---

# 47. Integración Firebase

Los tests de integración sí pueden probar:

```text
API
 ↓
Repository
 ↓
Firestore
```

pero deben estar separados de los tests unitarios.

---

# 48. Observabilidad

La API debe generar logs estructurados.

Cada request debería poder relacionarse con:

```text
traceId
userId
endpoint
status
duration
```

Nunca registrar:

```text
password
access token
private key
```

ni información financiera innecesaria.

---

# 49. Health Check

Debe existir:

```text
GET /actuator/health
```

para comprobar disponibilidad.

En producción no debe exponer información sensible.

---

# 50. Arquitectura final

```text
                                  NEXABANK
                                     |
                +--------------------+--------------------+
                |                                         |
                v                                         v
           FRONTEND                                  CLIENTES
        Web / Mobile                               terceros
                |                                         |
                +--------------------+--------------------+
                                     |
                                   HTTPS
                                     |
                                     v
                         +----------------------+
                         |    NEXABANK API      |
                         |      Spring Boot     |
                         +----------+-----------+
                                    |
          +-------------------------+--------------------------+
          |                         |                          |
          v                         v                          v
    Authentication            Business Logic             Algorithms
    Spring Security           Services                   Adapter
          |                         |                          |
          v                         v                          v
 Firebase Auth              Repositories              nexabank-core
                                    |                  Java puro
                                    |                       |
                                    v                       +-- DynamicArray
                              Firestore                    +-- MergeSort
                                    |                       +-- QuickSort
                                    |                       +-- BinarySearch
                                    |
                   +----------------+----------------+
                   |                |                |
                   v                v                v
                users           accounts       transactions
                   |                |                |
                   +----------------+----------------+
                                    |
                              transfers
                                    |
                              audit_logs

                         JMH
                          |
                          v
                  benchmark_results
                          |
                          v
                      Firestore
```

---

# 51. Estructura final del repositorio

```text
nexabank/
│
├── nexabank-core/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── nexabank-api/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── nexabank-frontend/
│   ├── src/
│   ├── package.json
│   └── README.md
│
├── docs/
│   ├── architecture.md
│   ├── api.md
│   ├── database-schema.md
│   └── algorithms.md
│
├── .gitignore
├── README.md
└── docker-compose.yml
```

---

# 52. Reglas no negociables

1. `nexabank-core` no depende de Spring.
2. `nexabank-core` no depende de Firebase.
3. El frontend no accede directamente a Firestore para operaciones de negocio.
4. Los controllers no contienen lógica financiera compleja.
5. Los Services contienen las reglas de negocio.
6. Los Repositories contienen el acceso a Firestore.
7. Los DTOs controlan el contrato HTTP.
8. Nunca confiar en un balance enviado por el cliente.
9. Las transferencias deben ser idempotentes.
10. Las transacciones completadas no se eliminan físicamente.
11. Las operaciones financieras importantes generan auditoría.
12. Los algoritmos se ejecutan sobre datos cargados en memoria.
13. JMH no debe medir latencia de Firebase si el objetivo es medir complejidad algorítmica.
14. Los benchmarks se almacenan como resultados históricos.
15. No subir secretos a Git.
16. Todas las rutas privadas requieren autenticación.
17. Autenticación y autorización son responsabilidades diferentes.
18. Se debe verificar que el usuario tenga acceso al recurso solicitado.
19. Las respuestas de error tienen formato consistente.
20. La API debe poder evolucionar sin romper clientes existentes.
21. Las colecciones de Firebase representan persistencia, no estructuras de datos temporales.
22. `DynamicArray` pertenece al motor algorítmico y no se persiste.
23. `MergeSort`, `QuickSort` y `BinarySearch` no se reimplementan dentro de Spring.
24. El API debe utilizar `/api/v1`.
25. El backend debe ser la única capa que aplique las reglas financieras.

---

# 53. Objetivo final

NexaBank debe terminar siendo una plataforma en la que:

```text
Usuario
   |
   v
Frontend
   |
   v
NexaBank API
   |
   +--> Autenticación
   |
   +--> Cuentas
   |
   +--> Transacciones
   |
   +--> Transferencias
   |
   +--> Beneficiarios
   |
   +--> Algoritmos
   |
   +--> Benchmarks
   |
   v
Firebase
```

y, al mismo tiempo:

```text
nexabank-core
       |
       +--> DynamicArray
       +--> MergeSort
       +--> QuickSort
       +--> BinarySearch
       |
       v
      JMH
       |
       v
Resultados académicos
```

La aplicación final no debe ser simplemente una API CRUD conectada a Firebase. La finalidad es construir una **plataforma financiera completa**, donde Spring Boot sea la capa de aplicación, Firebase sea la persistencia/servicios administrados y `nexabank-core` continúe siendo el motor algorítmico independiente que sustenta la parte académica del proyecto.

---

## Referencia técnica

La arquitectura REST toma como referencia la documentación oficial de Spring:

- Building REST services with Spring:
  https://spring.io/guides/tutorials/rest
- Spring HATEOAS:
  https://spring.io/projects/spring-hateoas
- Spring HATEOAS en Spring Boot:
  https://docs.spring.io/spring-boot/reference/web/spring-hateoas.html

La guía oficial explica el uso de HTTP como plataforma para servicios REST, los controladores Spring MVC, operaciones `GET`/`POST`/`PUT`/`DELETE`, evolución compatible de APIs y HATEOAS. 
