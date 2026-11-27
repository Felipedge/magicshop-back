# Despliegue en AWS Learner Lab — backend y base de datos

Esta guía deja **el backend (Spring Boot) y MySQL** corriendo en una sola EC2 con Docker Compose. El frontend se publica aparte en S3: ver `DEPLOY.md` del repositorio `magicshop-front`.

| Pieza | Dónde corre | Cómo llega ahí |
|-------|-------------|----------------|
| Frontend (React + Vite) | Amazon S3 (sitio web estático) | `npm run build` en el PC y se suben los archivos de `dist/` |
| Backend (Spring Boot) | EC2 `t3.small`, contenedor `api` | `git clone` + `docker compose up --build` en la EC2 |
| Base de datos (MySQL 8.4) | Misma EC2, contenedor `mysql` | Mismo compose; datos en el volumen `mysql_data` |

**Orden:** primero el backend (el front necesita la IP al compilarse) y después el front. Todo en la región **us-east-1 (N. Virginia)**. Solo hay HTTP (sin HTTPS) en este entorno.

> Nunca subas a GitHub `.env`, `.env.prod`, `labsuser.pem` ni las credenciales de *AWS Details*. Evita el símbolo `$` en los valores de `.env.prod` (Docker Compose lo interpreta).

## Fase 1. Probar el despliegue de producción en tu PC

Si funciona con Docker en tu PC, funciona igual en la EC2. Necesitas Docker Desktop encendido.

```bash
cp .env.prod.example .env.prod      # editar con nano o un editor
```

Para la prueba local: `DB_PASSWORD` y `MYSQL_ROOT_PASSWORD` distintas entre sí; `JWT_SECRET` de al menos 32 caracteres; `CORS_ORIGINS=http://localhost:5173`; `ADMIN_PASSWORD` y `DEMO_USER_PASSWORD` de 8 o más caracteres.

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
docker compose -f docker-compose.prod.yml logs -f api      # esperar "Started MagicshopApplication"
```

Abre `http://localhost/api/productos` (responde `[]` o la lista). Si el puerto 80 está ocupado, cambia temporalmente `"80:8080"` por `"8081:8080"` en el compose. Para probar el front contra este backend, en `magicshop-front/.env` usa `VITE_API_URL=http://localhost/api`, ejecuta `npm run dev`, abre `http://localhost:5173` e inicia sesión como `admin@magicshop.com` con tu `ADMIN_PASSWORD`.

Al terminar: `docker compose -f docker-compose.prod.yml down` y deja `VITE_API_URL` como estaba.

## Fase 2. Subir el código a GitHub (sin secretos)

```bash
git status       # no deben aparecer .env, .env.prod, target/, node_modules/
git add . && git commit -m "docs: guía de despliegue AWS" && git push
```

Si un secreto llegó a subirse, borrarlo en otro commit no basta (queda en el historial): cambia esa clave de inmediato.

## Fase 3. Iniciar el Learner Lab

1. AWS Academy → curso → *Iniciar el Laboratorio de aprendizaje de AWS Academy* → **Start Lab** (esperar el círculo verde).
2. Botón **AWS** (abre la consola). Confirma la región **N. Virginia (us-east-1)**.
3. *AWS Details* → **Download PEM** (`labsuser.pem`).
4. Mac/Linux: `chmod 400 labsuser.pem`. Windows (PowerShell): `icacls labsuser.pem /inheritance:r` y `icacls labsuser.pem /grant:r "$($env:USERNAME):(R)"`.

## Fase 4. Crear la EC2 y su IP fija

**Lanzar instancia** (EC2 → Instancias → Lanzar instancias):

| Campo | Valor |
|-------|-------|
| Nombre | `magicshop-api` |
| AMI | Amazon Linux 2023 |
| Tipo | `t3.small` (la `t3.micro` no alcanza para Spring + MySQL) |
| Par de claves | `vockey` (corresponde a `labsuser.pem`) |
| Almacenamiento | 20 GiB gp3 |
| Grupo de seguridad | nuevo `magicshop-sg`: SSH 22 desde **Mi IP**, HTTP 80 desde `0.0.0.0/0` |

No abras el 3306 ni el 8080.

**IP elástica:** EC2 → Direcciones IP elásticas → Asignar → Acciones → Asociar con `magicshop-api`. Anota la IP (`TU_ELASTIC_IP`): la IP normal cambia en cada sesión del lab y el front la guarda al compilarse.

Si cambias de red, actualiza la regla SSH (Mi IP) en el grupo de seguridad.

## Fase 5. Backend y base de datos en la EC2

**5.1 Nombre del bucket.** Elige ahora el nombre del bucket del front (único en AWS, minúsculas, p. ej. `magicshop-felipe-2026`). La URL del front será `http://NOMBRE-BUCKET.s3-website-us-east-1.amazonaws.com` y es la que va en `CORS_ORIGINS`.

**5.2 Entrar:**

```bash
ssh -i labsuser.pem ec2-user@TU_ELASTIC_IP
```

**5.3 Docker, Git y swap:**

```bash
sudo dnf update -y
sudo dnf install -y docker git
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user

sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

sudo dd if=/dev/zero of=/swapfile bs=128M count=16
sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
```

Ejecuta `exit`, vuelve a entrar y comprueba con `docker compose version`.

**5.4 Código y secretos:**

```bash
git clone https://github.com/Felipedge/magicshop-back.git
cd magicshop-back
cp .env.prod.example .env.prod
openssl rand -base64 48        # el resultado es tu JWT_SECRET
nano .env.prod
chmod 600 .env.prod
```

Completa: `DB_PASSWORD` y `MYSQL_ROOT_PASSWORD` (largas y distintas), `JWT_SECRET` (el generado), `CORS_ORIGINS` (URL del bucket, **sin `/` final**), `ADMIN_PASSWORD` y `DEMO_USER_PASSWORD` (nuevas). `SWAGGER_ENABLED=false` en producción (para la defensa puede ponerse en `true`, ver al final).

**5.5 Compilar y levantar** (5 a 10 minutos la primera vez):

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
docker compose -f docker-compose.prod.yml logs -f api      # Ctrl+C al ver "Started MagicshopApplication"
```

**5.6 Probar:** desde la EC2 `curl http://localhost/api/productos` y desde tu navegador `http://TU_ELASTIC_IP/api/productos` (responden JSON).

**5.7 Revisar tablas:**

```bash
docker compose -f docker-compose.prod.yml exec mysql mysql -u magicshop -p magicshop -e "SHOW TABLES;"
```

Debe listar `categorias`, `orden_items`, `ordenes`, `productos` y `usuarios`.

Respaldo opcional:

```bash
docker compose -f docker-compose.prod.yml exec mysql sh -c 'mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" magicshop' > respaldo.sql
```

Los datos viven en el volumen `mysql_data`: sobreviven a reinicios y a `docker compose down`, pero no a `down -v` ni a terminar la instancia.

## Fase 6. Frontend en S3

Está en `DEPLOY.md` de `magicshop-front`.

## Día a día

- **Nueva sesión del lab:** Start Lab; la EC2 y los contenedores arrancan solos (`restart: unless-stopped`). Espera 1 o 2 minutos.
- **Actualizar el backend:** en la EC2, `cd magicshop-back && git pull && docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build`.
- **Cambiar un secreto o el CORS:** edita `.env.prod` y repite el `up -d`. `ADMIN_PASSWORD` solo se aplica la primera vez (con la base vacía); después se cambia desde el panel de administración.
- **Mostrar Swagger en la defensa:** pon `SWAGGER_ENABLED=true` en `.env.prod`, ejecuta el `up -d` y abre `http://TU_ELASTIC_IP/swagger-ui.html`. Al terminar, vuelve a `false`.

## Checklist de seguridad y pruebas

- [ ] `.env`, `.env.prod`, `labsuser.pem` y credenciales de AWS fuera de GitHub (y de su historial).
- [ ] `JWT_SECRET` generado con `openssl rand -base64 48`; contraseñas distintas entre sí; `.env.prod` con permisos 600.
- [ ] Grupo de seguridad: SSH solo desde Mi IP, HTTP 80 abierto, nada más. MySQL sin `ports:`.
- [ ] `CORS_ORIGINS` exacto, nunca `*`.
- [ ] `http://TU_ELASTIC_IP/api/usuarios` sin sesión responde **401**.
- [ ] `http://TU_ELASTIC_IP/swagger-ui.html` responde 404 (con Swagger apagado).
- [ ] `Test-NetConnection TU_ELASTIC_IP -Port 3306` (o `nc -zv TU_ELASTIC_IP 3306`) falla.
- [ ] Login con `admin@magicshop.com` y la contraseña de prueba (`admin123`) **no** funciona.

## Problemas comunes

| Síntoma | Causa y solución |
|---------|------------------|
| `release version 21 not supported` al compilar en el PC | JDK distinto de 21: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` |
| La compilación en la EC2 se cae | Falta swap o la instancia es `t3.micro` |
| Error de CORS en el navegador | `CORS_ORIGINS` no coincide exactamente con la URL del bucket (http, sin `/` final) |
| `Falta JWT_SECRET en .env.prod` | Variable vacía; el compose la exige |
| El front no conecta tras reiniciar el lab | La IP no es elástica o el front se compiló con otra IP |
| `permission denied` con docker | Falta cerrar sesión SSH y volver a entrar tras `usermod -aG docker` |
