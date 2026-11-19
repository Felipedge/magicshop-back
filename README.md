# Magic-Shop — Backend (Spring Boot + MySQL)

API REST de la tienda de cartas Magic-Shop. El frontend (React) está en otro proyecto y la consume por HTTP.

```
magicshop-backend/   ← este proyecto: Spring Boot 3.5 · Java 21 · JPA · Spring Security + JWT · MySQL 8.4
magicshop/           ← frontend React + Bootstrap (proyecto aparte)
```

## 1. Levantar MySQL (puerto 3307)

```bash
docker compose up -d
```

Crea la BD `magicshop` con el usuario `magicshop` / `magicshop123`, accesible solo desde el mismo PC (`127.0.0.1:3307`).
Si prefieres un MySQL instalado en tu PC, define `DB_HOST`, `DB_PORT`, `DB_USER` y `DB_PASSWORD` (ver `.env.example`);
la BD se crea sola gracias a `createDatabaseIfNotExist=true`.

## 2. Ejecutar el backend

```bash
mvn spring-boot:run
```

En IntelliJ: abrir `pom.xml` y ejecutar `MagicshopApplication` (activar *Enable annotation processing* para Lombok).
La API queda en **http://localhost:8080/api**. Al primer arranque crea las tablas y carga los usuarios de prueba,
las 6 categorías y las 32 cartas del inventario inicial (`src/main/resources/seed/seed.json`).

| Rol     | Correo                  | Contraseña   |
|---------|-------------------------|--------------|
| admin   | admin@magicshop.com     | admin123     |
| cliente | comprador@magicshop.com | comprador123 |

Swagger UI: http://localhost:8080/swagger-ui.html (botón **Authorize**: pegar solo el token, sin `Bearer`).
En producción: `SWAGGER_ENABLED=false`.

## 3. Pruebas

```bash
mvn test
```

Usan H2 en memoria (no necesitan MySQL): login, permisos por rol, validaciones, filtros, compra con descuento de stock,
rechazo por stock insuficiente y acceso a órdenes.

## 4. Endpoints

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/api/auth/login` · `/api/auth/registro` | Público |
| GET | `/api/auth/me` | Sesión iniciada |
| GET | `/api/productos` · `/api/productos/{id}` | Público |
| POST · PATCH · DELETE | `/api/productos[/{id}]` | ADMIN |
| GET | `/api/categorias` · `/api/categorias/{id}` | Público |
| POST · PATCH · DELETE | `/api/categorias[/{id}]` | ADMIN |
| GET · POST · DELETE | `/api/usuarios[/{id}]` | ADMIN |
| GET · PATCH | `/api/usuarios/{id}` | ADMIN, o el mismo usuario sobre su propia cuenta |
| POST | `/api/ordenes` (pagar) | Público (invitado o con sesión) |
| GET | `/api/ordenes/{id}` | Público (el id es un UUID imposible de adivinar) |
| GET | `/api/ordenes` | ADMIN: todas · cliente: solo las suyas |

### Filtros y orden (mismo estilo que json-server, solo sobre campos permitidos)

`campo=valor` · `campo_ne=valor` · `campo_lte=n` · `campo_gte=n` · `_sort=campo&_order=desc`

| Recurso | Filtros | Orden |
|---------|---------|-------|
| `/productos` | `id` `categoria` `rareza` `eliminado` `stock` `oferta` `precio` | `nombre` `precio` `stock` `oferta` `categoria` |
| `/categorias` | `id` `slug` `tipoApi` | `id` `nombre` `slug` |
| `/usuarios` | `id` `email` `rol` `run` | `id` `nombre` `email` |
| `/ordenes` | `estado` `usuarioId` `numero` | `fecha` `total` |

Ejemplos: `/api/productos?stock_lte=3&eliminado_ne=true` (críticos) · `/api/productos?oferta_gte=1&eliminado_ne=true&_sort=oferta&_order=desc` (ofertas) ·
`/api/ordenes?estado=pagada&_sort=fecha&_order=desc`.

### Pagar: `POST /api/ordenes`

```json
{
  "cliente":  { "nombre": "Ana", "apellidos": "Soto", "email": "ana@correo.cl" },
  "despacho": { "calle": "Los Olmos 123", "departamento": "603", "region": "Región Metropolitana de Santiago", "comuna": "Santiago", "indicaciones": "" },
  "items":    [ { "id": "5f8287b1-5bb6-5f4c-ad17-316a40d5bb0c", "cantidad": 2 } ],
  "simularRechazo": false
}
```

El cliente **solo envía qué compra**. El servidor busca cada carta (si no está en el inventario la consulta a la API de Magic y la agrega),
calcula precio y total, valida el stock con bloqueo de fila (`SELECT ... FOR UPDATE`) y guarda la orden como `pagada` (descuenta stock)
o `rechazada` (con el motivo).

### Errores

Siempre JSON, sin trazas internas: `{ "status": 400, "message": "Hay datos inválidos en la solicitud.", "errors": { "precio": "El precio no puede ser negativo." } }`

## 5. Estructura

```
src/main/java/cl/duoc/magicshop/
├── MagicshopApplication.java
├── config/       SecurityConfig (rutas, CORS, JWT) · OpenApiConfig · DataSeeder
├── controller/   Auth · Usuario · Categoria · Producto · Orden
├── service/      lógica de negocio (AuthService, OrdenService, MtgClient...)
├── repository/   Spring Data JPA (+ Specification para los filtros)
├── entity/       Usuario · Categoria · Producto · Orden · OrdenItem · Cliente · Despacho
├── dto/          solicitudes y respuestas (validadas con Bean Validation)
├── security/     JwtService · JwtAuthFilter · AuthUser
├── exception/    ApiException · GlobalExceptionHandler · ErrorResponse
└── util/         Filtros (query params → Specification) · Catalogo (precio por rareza)
```

## 6. Seguridad: qué está cubierto

| Riesgo | Cómo se cubre |
|--------|---------------|
| Contraseñas legibles | Hash **BCrypt**; ninguna respuesta incluye la contraseña |
| Darse rol admin al registrarse | `/auth/registro` ignora el rol y siempre crea `cliente` |
| Saltarse el login o el rol desde el front | Cada ruta se valida en Spring Security; `ProtectedRoute` solo cambia lo que se ve |
| Token de un usuario eliminado o degradado | El filtro JWT vuelve a leer al usuario en la BD en cada petición |
| Manipular precios o el total | El servidor calcula precio y total; el front solo envía id y cantidad |
| Comprar más de lo que hay | Transacción con bloqueo pesimista sobre el stock |
| Quedarse sin administradores | Un admin no puede eliminarse ni quitarse el rol, ni se puede borrar/degradar al último |
| Enumerar correos por el login | Mensaje genérico y tiempo de respuesta parejo |
| Filtrar errores internos | `GlobalExceptionHandler` responde JSON sin stack traces |
| Filtros peligrosos | Lista blanca de campos filtrables y ordenables; JPA con parámetros (sin SQL concatenado) |
| Cualquier web llamando a la API | CORS con lista blanca (`CORS_ORIGINS`) |
| Secretos en el código | Todo sale de variables de entorno; sin `JWT_SECRET` se genera una clave aleatoria al arrancar |

### Pendientes antes de producción

- El JWT va en `localStorage` en el front: un XSS podría robarlo (idealmente cookie `HttpOnly`).
- No hay límite de intentos de login (rate limiting).
- HTTPS obligatorio (ALB/CloudFront con certificado).
- `ddl-auto: update` → cambiar a `validate` y usar migraciones (Flyway).
- Cambiar `ADMIN_PASSWORD`, `DEMO_USER_PASSWORD` y definir `JWT_SECRET` (mínimo 32 caracteres).

## 7. Despliegue (EC2)

```bash
cp .env.prod.example .env.prod      # completar los valores
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

`CORS_ORIGINS` debe ser la URL exacta del frontend. Al compilar el frontend, `VITE_API_URL` debe apuntar a `http://<ip-o-dominio>/api`.
