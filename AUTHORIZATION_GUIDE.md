# Guía de Autorización por Roles - CampusLab BFF

## Resumen

Este documento describe cómo está implementada la autorización basada en roles (RBAC) en la aplicación CampusLab BFF usando Spring Security con JWT (OAuth2 Resource Server).

## Roles Disponibles

La aplicación define 4 roles principales:

### 1. **ADMIN** (ROLE_ADMIN)
- Acceso completo a todas las funcionalidades
- Puede crear, modificar y eliminar usuarios
- Puede acceder a estadísticas y configuración avanzada
- Puede ejecutar operaciones críticas del sistema

**Endpoints:**
- `GET /api/admin/estadisticas` - Ver estadísticas del sistema
- `POST /api/admin/usuarios` - Crear nuevo usuario
- `DELETE /api/admin/usuarios/{id}` - Eliminar usuario
- `POST /api/admin/reset-database` - Resetear base de datos

### 2. **TECNICO** (ROLE_TECNICO)
- Acceso técnico y de configuración
- Puede ver y modificar configuración del sistema
- Puede revisar logs
- Puede realizar mantenimiento técnico

**Endpoints:**
- `GET /api/tecnico/configuracion` - Ver configuración
- `PUT /api/tecnico/configuracion` - Actualizar configuración
- `GET /api/tecnico/logs` - Ver logs del sistema
- `POST /api/tecnico/reiniciar` - Reiniciar servicio
- `POST /api/tecnico/validar-integridad` - Validar integridad de datos

### 3. **ESTUDIANTE** (ROLE_ESTUDIANTE)
- Acceso a sus cursos, calificaciones y materiales
- Puede enviar tareas
- Acceso limitado a sus propios datos

**Endpoints:**
- `GET /api/estudiante/mis-cursos` - Ver cursos inscritos
- `GET /api/estudiante/mis-calificaciones` - Ver calificaciones
- `POST /api/estudiante/enviar-tarea` - Enviar tarea
- `GET /api/estudiante/mis-materiales` - Ver materiales de curso

### 4. **AUDITOR** (ROLE_AUDITOR)
- Acceso de lectura a registros y auditoría
- Solo lectura (no puede modificar datos)
- Puede generar reportes de auditoría

**Endpoints:**
- `GET /api/auditor/registros` - Ver registros de auditoría
- `GET /api/auditor/usuario/{usuarioId}` - Ver acciones de usuario
- `GET /api/auditor/cambios/{recursoId}` - Ver cambios en recursos
- `POST /api/auditor/generar-reporte` - Generar reporte de auditoría

## Endpoints Públicos (Sin Autenticación)

Algunos endpoints no requieren autenticación:

- `GET /api/public/informacion` - Información general del sistema
- `GET /api/public/health` - Estado de salud (Health Check)
- `POST /api/public/registro` - Registrar nuevo usuario
- `GET /api/public/terminos-condiciones` - Ver términos
- `GET /api/public/politica-privacidad` - Ver política de privacidad

## Arquitectura de Seguridad

### 1. Configuración de Spring Security

La configuración está en `SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class SecurityConfig {
    // Configuración de filtros HTTP
    // Conversión de JWT a autoridades de Spring Security
}
```

### 2. Flujo de Autenticación

1. **Usuario solicita un endpoint protegido**
2. **Envía JWT en el header Authorization**: `Authorization: Bearer <token>`
3. **Spring Security valida el JWT** contra el servidor JWKS
4. **Extrae los roles del claim "roles" del JWT**
5. **Convierte los roles a GrantedAuthority** con prefijo `ROLE_`
6. **Verifica si el usuario tiene el rol requerido**
7. **Permite o deniega el acceso** según la configuración

### 3. Validación de JWT

El JWT debe contener un claim llamado `roles` con los roles del usuario:

```json
{
  "sub": "usuario@campus.lab",
  "email": "usuario@campus.lab",
  "name": "Juan Pérez",
  "roles": ["ESTUDIANTE"],
  "iat": 1694523330,
  "exp": 1694526930
}
```

O para un admin:

```json
{
  "sub": "admin@campus.lab",
  "email": "admin@campus.lab",
  "name": "Administrador",
  "roles": ["ADMIN", "TECNICO"],
  "iat": 1694523330,
  "exp": 1694526930
}
```

## Uso de Anotaciones

### @PreAuthorize

Para proteger métodos individuales, usa `@PreAuthorize`:

```java
@GetMapping("/datos-admin")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> getDatosAdmin() {
    return ResponseEntity.ok("Datos administrativos");
}
```

### Múltiples Roles

Permitir acceso a usuarios con al menos uno de varios roles:

```java
@PostMapping("/actualizar-configuracion")
@PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
public ResponseEntity<String> actualizarConfiguracion() {
    return ResponseEntity.ok("Configuración actualizada");
}
```

### Expresiones Complejas

```java
@PreAuthorize("hasRole('ADMIN') and #id == authentication.principal.username")
public void deleteUser(@PathVariable String id) {
    // Solo admin puede eliminar
    // Y solo puede eliminar su propio usuario
}
```

### Autorización a Nivel de Clase

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // Todos los métodos requieren ADMIN
public class AdminController {
    // Todos los métodos están protegidos
}
```

## Manejo de Errores de Autorización

### 401 Unauthorized (No autenticado)

Cuando el usuario no envía un JWT válido:

```json
{
  "timestamp": "2024-09-12T12:00:00",
  "status": 401,
  "error": "NO_AUTENTICADO",
  "message": "Debes autenticarte para acceder a este recurso",
  "path": "/api/admin/estadisticas"
}
```

### 403 Forbidden (Sin permisos)

Cuando el usuario no tiene el rol requerido:

```json
{
  "timestamp": "2024-09-12T12:00:00",
  "status": 403,
  "error": "ACCESO_DENEGADO",
  "message": "No tienes permisos para acceder a este recurso",
  "path": "/api/admin/estadisticas"
}
```

## Configuración del Servidor JWKS

La aplicación espera recibir la URL del servidor JWKS (JSON Web Key Set) que validará los JWTs.

### Con Keycloak

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://keycloak:8080/auth/realms/campuslab/protocol/openid-connect/certs
          issuer-uri: http://keycloak:8080/auth/realms/campuslab
```

### Con Auth0

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://tu-dominio.auth0.com/.well-known/jwks.json
          issuer-uri: https://tu-dominio.auth0.com/
```

### Con Google

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://www.googleapis.com/oauth2/v3/certs
          issuer-uri: https://accounts.google.com
```

### Variables de Entorno

Las URL se pueden configurar con variables de entorno:

```bash
JWT_JWK_SET_URI=http://keycloak:8080/auth/realms/campuslab/protocol/openid-connect/certs
JWT_ISSUER_URI=http://keycloak:8080/auth/realms/campuslab
```

## Testing con cURL

### 1. Obtener un Token JWT

```bash
# Con Keycloak
curl -X POST http://localhost:8090/auth/realms/campuslab/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=campuslab-client" \
  -d "client_secret=tu-secret" \
  -d "username=usuario@campus.lab" \
  -d "password=password" \
  -d "grant_type=password"
```

### 2. Usar el Token en una Solicitud

```bash
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Acceso a endpoint protegido por rol ADMIN
curl -X GET http://localhost:8080/api/admin/estadisticas \
  -H "Authorization: Bearer $TOKEN"

# Acceso a endpoint protegido por rol TECNICO
curl -X GET http://localhost:8080/api/tecnico/configuracion \
  -H "Authorization: Bearer $TOKEN"

# Acceso a endpoint público (sin token)
curl -X GET http://localhost:8080/api/public/informacion
```

### 3. Probar con Usuario sin Permisos

```bash
# Token de ESTUDIANTE intentando acceder a /api/admin/
curl -X GET http://localhost:8080/api/admin/estadisticas \
  -H "Authorization: Bearer $STUDENT_TOKEN"

# Resultado: 403 Forbidden
```

## Estructura de Archivos

```
src/main/java/org/campuslab/bff/
├── config/
│   └── SecurityConfig.java          # Configuración principal de seguridad
├── controller/
│   ├── AdminController.java         # Endpoints para ADMIN
│   ├── TecnicoController.java       # Endpoints para TECNICO
│   ├── EstudianteController.java    # Endpoints para ESTUDIANTE
│   ├── AuditorController.java       # Endpoints para AUDITOR
│   └── PublicController.java        # Endpoints públicos
├── exception/
│   └── GlobalExceptionHandler.java  # Manejo de excepciones de seguridad
└── security/
    ├── Role.java                    # Enumeración de roles
    └── RequireRole.java             # Anotación personalizada (opcional)

src/main/resources/
└── application.yml                  # Configuración de la aplicación
```

## Integración con OAuth2/Keycloak

### Paso 1: Instalación y Configuración de Keycloak

```bash
docker run -p 8090:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest \
  start-dev
```

### Paso 2: Crear Realm, Cliente y Roles

1. Acceder a Keycloak: http://localhost:8090
2. Crear un realm llamado `campuslab`
3. Crear un cliente llamado `campuslab-client`
4. Crear roles: ADMIN, TECNICO, ESTUDIANTE, AUDITOR
5. Asignar roles a usuarios

### Paso 3: Configurar Mapeo de Roles

En Keycloak, crear un mapper para incluir los roles en el JWT:

1. Ir a Cliente > Mappers
2. Crear mapper de tipo "User Realm Role"
3. Configurar para incluir en ID Token y Access Token
4. Nombre del claim: `roles`

### Paso 4: Actualizar application.yml

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8090/auth/realms/campuslab/protocol/openid-connect/certs
          issuer-uri: http://localhost:8090/auth/realms/campuslab
```

## Seguridad Adicional

### CORS Configurado

La aplicación permite solicitudes CORS desde cualquier origen (configurable):

```java
configuration.setAllowedOriginPatterns(Arrays.asList("*"));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
```

### CSRF Deshabilitado

Apropiado para APIs REST stateless.

### Sesiones Stateless

No se crean sesiones en el servidor. Cada solicitud se valida mediante JWT.

## Mejores Prácticas

1. **Usar HTTPS en Producción**: Los tokens JWT deben transmitirse siempre por HTTPS
2. **Validar el Issuer**: Asegurar que el JWT venga del servidor de autenticación esperado
3. **Verificar Expiración**: Los tokens deben expirar dentro de un tiempo razonable (ej: 1 hora)
4. **No Exponer Secretos**: Nunca incluir secretos en el código o commits
5. **Usar Variables de Entorno**: Configurar URLs de autenticación mediante variables de entorno
6. **Logging**: Activar logging de seguridad para auditar accesos
7. **Rate Limiting**: Considerar implementar rate limiting para prevenir ataques
8. **Refresh Tokens**: Usar refresh tokens para prolongar sesiones de forma segura

## Resolución de Problemas

### Error: "401 Unauthorized"

- Verificar que el JWT es válido
- Verificar que el JWT no está expirado
- Verificar que la URL del JWKS es correcta
- Verificar que el claim `iss` (issuer) coincide con la configuración

### Error: "403 Forbidden"

- Verificar que el usuario tiene el rol requerido
- Verificar que el claim `roles` está incluido en el JWT
- Revisar la expresión `@PreAuthorize`
- Verificar logs de seguridad en DEBUG

### Error: "400 Bad Request - Invalid token"

- Verificar el formato: `Authorization: Bearer <token>`
- No incluir "Bearer" dos veces
- Verificar que no hay espacios en blanco al inicio/final del token

## Referencias

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [OAuth 2.0 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [JWT (JSON Web Tokens)](https://jwt.io/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Method Security](https://spring.io/blog/2022/02/21/spring-security-without-the-web)

