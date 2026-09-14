# Integración con Keycloak - CampusLab BFF

Esta guía proporciona instrucciones paso a paso para integrar CampusLab BFF con Keycloak como proveedor de autenticación y autorización.

## Tabla de Contenidos

1. [Instalación de Keycloak](#instalación-de-keycloak)
2. [Configuración de Realm](#configuración-de-realm)
3. [Configuración de Roles](#configuración-de-roles)
4. [Configuración de Clientes](#configuración-de-clientes)
5. [Mapeo de Roles en JWT](#mapeo-de-roles-en-jwt)
6. [Creación de Usuarios de Prueba](#creación-de-usuarios-de-prueba)
7. [Configuración de CampusLab BFF](#configuración-de-campuslab-bff)
8. [Testing](#testing)

## Instalación de Keycloak

### Con Docker

```bash
# Descargar y ejecutar Keycloak
docker run -d \
  --name keycloak \
  -p 8090:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest \
  start-dev
```

### Con Docker Compose

Crear archivo `docker-compose.yml`:

```yaml
version: '3.8'

services:
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    container_name: keycloak
    ports:
      - "8090:8080"
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    command:
      - start-dev
    volumes:
      - keycloak_data:/opt/keycloak/data

  postgres:
    image: postgres:15
    container_name: postgres
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: keycloak
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  keycloak_data:
  postgres_data:
```

Ejecutar:

```bash
docker-compose up -d
```

Esperar 30 segundos para que Keycloak inicie.

### Acceder a Keycloak

- URL: `http://localhost:8090`
- Usuario: `admin`
- Contraseña: `admin`

## Configuración de Realm

### Paso 1: Crear Realm

1. En la esquina superior izquierda, hacer clic en "Master" (dropdown)
2. Hacer clic en "Create Realm"
3. Ingresar nombre: `campuslab`
4. Hacer clic en "Create"

### Paso 2: Configuración Básica del Realm

1. Ir a "Realm settings"
2. Pestaña "General"
   - Frontend URL: `http://localhost:8090` (ajustar según tu ambiente)
3. Hacer clic en "Save"

## Configuración de Roles

### Crear Roles del Realm

1. En el sidebar, ir a "Realm roles"
2. Hacer clic en "Create role"
3. Crear los siguientes roles:

#### Role 1: ADMIN
- Role name: `ADMIN`
- Description: `Administrador del sistema`
- Hacer clic en "Save"

#### Role 2: TECNICO
- Role name: `TECNICO`
- Description: `Técnico de soporte`
- Hacer clic en "Save"

#### Role 3: ESTUDIANTE
- Role name: `ESTUDIANTE`
- Description: `Estudiante`
- Hacer clic en "Save"

#### Role 4: AUDITOR
- Role name: `AUDITOR`
- Description: `Auditor del sistema`
- Hacer clic en "Save"

## Configuración de Clientes

### Paso 1: Crear Cliente

1. En el sidebar, ir a "Clients"
2. Hacer clic en "Create client"
3. Ingresar datos:
   - Client ID: `campuslab-client`
   - Name: `CampusLab BFF`
4. Hacer clic en "Next"

### Paso 2: Configurar Capacidades

1. Habilitar:
   - ✓ Client authentication (Cambiar a ON)
   - ✓ Authorization
2. Hacer clic en "Next"

### Paso 3: Configurar URLs de Redirección

1. Root URL: `http://localhost:8080`
2. Home URL: `http://localhost:8080`
3. Valid redirect URIs:
   ```
   http://localhost:8080/*
   http://localhost:3000/*
   ```
4. Valid post logout redirect URIs:
   ```
   http://localhost:8080
   http://localhost:3000
   ```
5. Web origins: `http://localhost:8080`
6. Hacer clic en "Save"

### Paso 4: Obtener Credenciales

1. Ir a la pestaña "Credentials"
2. Copiar el "Client secret" (lo necesitarás más adelante)

## Mapeo de Roles en JWT

Para que los roles aparezcan en el JWT, necesitas crear un mapper.

### Crear Mapper de Roles

1. Ir al cliente `campuslab-client`
2. Pestaña "Client Scopes"
3. Hacer clic en `campuslab-client-dedicated`
4. En "Dedicated Scopes", hacer clic en "Mappers"
5. Hacer clic en "By configuration" o "Configure a new mapper"
6. Seleccionar "User Realm Role"
7. Configurar:
   - Name: `Realm roles`
   - Token Claim Name: `roles`
   - Claim JSON Type: `String`
   - ✓ Add to ID token
   - ✓ Add to access token
   - ✓ Add to userinfo
   - Multi-valued: ON
8. Hacer clic en "Save"

### Alternativa: Mapper de Client Roles

Si prefieres usar client roles en lugar de realm roles:

1. Seguir los mismos pasos
2. Seleccionar "User Client Role"
3. Client ID: `campuslab-client`
4. Token Claim Name: `roles`

## Creación de Usuarios de Prueba

### Usuario 1: Admin

1. En el sidebar, ir a "Users"
2. Hacer clic en "Add user"
3. Ingresar datos:
   - Username: `admin@campus.lab`
   - Email: `admin@campus.lab`
   - First name: `Administrador`
   - Last name: `Sistema`
   - Email verified: ON
4. Hacer clic en "Create"

#### Establecer Contraseña

1. Ir a pestaña "Credentials"
2. Hacer clic en "Set password"
3. Ingresar: `admin123`
4. Temporary: OFF
5. Hacer clic en "Set password"

#### Asignar Roles

1. Ir a pestaña "Role mapping"
2. Hacer clic en "Assign role"
3. Seleccionar: `ADMIN`, `TECNICO`
4. Hacer clic en "Assign"

### Usuario 2: Técnico

1. Username: `tecnico@campus.lab`
2. Email: `tecnico@campus.lab`
3. First name: `Juan`
4. Last name: `Técnico`
5. Rol: `TECNICO`

### Usuario 3: Estudiante

1. Username: `estudiante@campus.lab`
2. Email: `estudiante@campus.lab`
3. First name: `María`
4. Last name: `Estudiante`
5. Rol: `ESTUDIANTE`

### Usuario 4: Auditor

1. Username: `auditor@campus.lab`
2. Email: `auditor@campus.lab`
3. First name: `Carlos`
4. Last name: `Auditor`
5. Rol: `AUDITOR`

## Configuración de CampusLab BFF

### Paso 1: Actualizar application.yml

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8090/auth/realms/campuslab/protocol/openid-connect/certs
          issuer-uri: http://localhost:8090/auth/realms/campuslab
```

### Paso 2: Variables de Entorno (Alternativa)

```bash
export JWT_JWK_SET_URI=http://localhost:8090/auth/realms/campuslab/protocol/openid-connect/certs
export JWT_ISSUER_URI=http://localhost:8090/auth/realms/campuslab
```

### Paso 3: Iniciar CampusLab BFF

```bash
mvn spring-boot:run
```

O con Docker:

```bash
docker build -t campuslab-bff .
docker run -p 8080:8080 \
  -e JWT_JWK_SET_URI=http://keycloak:8080/auth/realms/campuslab/protocol/openid-connect/certs \
  -e JWT_ISSUER_URI=http://keycloak:8080/auth/realms/campuslab \
  campuslab-bff
```

## Testing

### Paso 1: Obtener Token con cURL

```bash
# Para usuario admin@campus.lab
TOKEN=$(curl -X POST http://localhost:8090/auth/realms/campuslab/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=campuslab-client" \
  -d "client_secret=<YOUR_CLIENT_SECRET>" \
  -d "username=admin@campus.lab" \
  -d "password=admin123" \
  -d "grant_type=password" \
  | jq -r '.access_token')

echo "Token: $TOKEN"
```

### Paso 2: Verificar Token en jwt.io

1. Ir a `https://jwt.io`
2. Copiar el token en el campo "Encoded"
3. En "Public key", copiar la clave pública de Keycloak:
   ```
   http://localhost:8090/auth/realms/campuslab/protocol/openid-connect/certs
   ```

### Paso 3: Probar Endpoints

```bash
# Endpoint público (sin token)
curl -X GET http://localhost:8080/api/public/informacion

# Endpoint protegido con token
curl -X GET http://localhost:8080/api/admin/estadisticas \
  -H "Authorization: Bearer $TOKEN"

# Endpoint de otro rol (debe fallar con 403)
curl -X GET http://localhost:8080/api/auditor/registros \
  -H "Authorization: Bearer $ADMIN_TOKEN"
# Este falla porque ADMIN no tiene rol AUDITOR
```

### Paso 4: Usar Script de Prueba

```bash
chmod +x test-roles.sh

# Obtener token de admin
ADMIN_TOKEN=$(curl -X POST http://localhost:8090/auth/realms/campuslab/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=campuslab-client" \
  -d "client_secret=<YOUR_CLIENT_SECRET>" \
  -d "username=admin@campus.lab" \
  -d "password=admin123" \
  -d "grant_type=password" \
  | jq -r '.access_token')

# Ejecutar pruebas
./test-roles.sh "$ADMIN_TOKEN" "ADMIN"
```

## Troubleshooting

### Error: "401 Unauthorized - Invalid token"

**Solución:**
- Verificar que el JWT no está expirado
- Verificar que la URL del JWKS es correcta
- Revisar logs de Keycloak: `docker logs keycloak`

### Error: "403 Forbidden - Insufficient permissions"

**Solución:**
- Verificar que el usuario tiene el rol asignado
- Verificar que el mapper de roles está creado
- Verificar que el claim `roles` está incluido en el JWT

### El claim "roles" no aparece en el JWT

**Solución:**
1. Verificar que el mapper está creado
2. Verificar que "Add to access token" está habilitado
3. Regenerar el token después de crear el mapper

### Error al conectar a Keycloak

**Solución:**
- Verificar que Keycloak está corriendo: `docker ps`
- Verificar que el puerto 8090 está disponible
- Esperar a que Keycloak termine de iniciar (puede tardar 30-60 segundos)

## Configuración en Producción

### URLs HTTPS

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://keycloak.tudominio.com/auth/realms/campuslab/protocol/openid-connect/certs
          issuer-uri: https://keycloak.tudominio.com/auth/realms/campuslab
```

### Certificados Auto-Firmados

Si usas certificados auto-firmados, deshabilitar validación de certificados (NO recomendado en producción):

```bash
export INSECURE_SSL_ENABLED=true
```

### Rate Limiting en Keycloak

1. Ir a Realm settings
2. Security defenses
3. Brute force detection: ON
4. Max login failures: 30
5. Wait increment: 1 minuto

### Logs

```bash
# Ver logs de Keycloak
docker logs -f keycloak

# Ver logs de CampusLab BFF con debug de seguridad
# En application.yml:
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
```

## Referencias

- [Keycloak Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin/)
- [Keycloak Client Registration](https://www.keycloak.org/docs/latest/server_admin/#_client_registration)
- [Keycloak JWT Mappers](https://www.keycloak.org/docs/latest/server_admin/#_mappers)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)

