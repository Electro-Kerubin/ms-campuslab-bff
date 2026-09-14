# Refactorización de Gestión de Roles

## Cambios Realizados

### ❌ Eliminado
- **Roles.java** - Clase con constantes estáticas duplicadas

### ✅ Actualizado

#### 1. SecurityConfig.java
**Cambios:**
- Importado `org.campuslab.bff.security.Role`
- Actualizado `hasRole()` y `hasAnyRole()` para usar valores del enum `Role`

**Antes:**
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/tecnico/**").hasAnyRole("TECNICO", "ADMIN")
```

**Después:**
```java
.requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())
.requestMatchers("/api/tecnico/**").hasAnyRole(Role.TECNICO.name(), Role.ADMIN.name())
```

**Beneficios:**
- ✅ Type-safe: Si cambias un rol en `Role.java`, el compilador lo detecta
- ✅ Evita strings mágicos
- ✅ Single source of truth (una sola definición de roles)
- ✅ Refactoring más seguro

#### 2. AzureAdJwtAuthenticationConverter.java
**Cambios:**
- Actualizado javadoc para referenciar `Role` en lugar de `Roles`

**Antes:**
```java
* Ver {@link Roles} para los valores de rol esperados.
```

**Después:**
```java
* Ver {@link Role} para los valores de rol esperados (ADMIN, TECNICO, ESTUDIANTE, AUDITOR).
```

## Estructura Final de Roles

```
src/main/java/org/campuslab/bff/security/
├── Role.java                           ✅ Única fuente de verdad (ENUM)
│   ├── ADMIN("ROLE_ADMIN", "Administrador")
│   ├── TECNICO("ROLE_TECNICO", "Técnico")
│   ├── ESTUDIANTE("ROLE_ESTUDIANTE", "Estudiante")
│   └── AUDITOR("ROLE_AUDITOR", "Auditor")
│
├── RequireRole.java                    (Anotación personalizada)
├── AzureAdJwtAuthenticationConverter.java  (Convierte JWT → GrantedAuthority)
└── SecurityConfig.java                 (Usa Role.name() para hasRole/hasAnyRole)
```

## Cómo Usar Role.java en Controladores

### Opción 1: Strings Literales (Simple)
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> getAdminData() { ... }
```

### Opción 2: Using Role Enum (Type-Safe) - RECOMENDADO
```java
@PreAuthorize("hasRole(T(org.campuslab.bff.security.Role).ADMIN.name())")
public ResponseEntity<String> getAdminData() { ... }
```

### Opción 3: Constantes Estáticas en Controlador
```java
@RestController
public class AdminController {
    private static final String ROLE_ADMIN = "ADMIN";
    
    @PreAuthorize("hasRole('" + ROLE_ADMIN + "')")
    public ResponseEntity<String> getAdminData() { ... }
}
```

## Validación de Cambios

### ✓ Sin referencias a Roles.java
```bash
grep -r "Roles\." src/
# Output: (ninguno)
```

### ✓ SecurityConfig usa Role.java
```bash
grep -n "Role\." src/main/java/org/campuslab/bff/config/SecurityConfig.java
# Lines: 67, 70, 70, 73, 73, 76, 76
```

### ✓ AzureAdJwtAuthenticationConverter actualizado
```bash
grep -n "Role" src/main/java/org/campuslab/bff/security/AzureAdJwtAuthenticationConverter.java
# Line 23: Ver {@link Role} para los valores...
```

## Ventajas del Nuevo Diseño

| Aspecto | Antes | Después |
|--------|-------|---------|
| **Single Source of Truth** | ❌ Dos clases (Roles + Role) | ✅ Una sola (Role enum) |
| **Type Safety** | ❌ Strings mágicos | ✅ Enum type-safe |
| **Refactoring** | ⚠️ Error-prone | ✅ Seguro con IDE |
| **Documentación** | ⚠️ Dispersa | ✅ Centralizada en Role.java |
| **Descripción Roles** | ❌ No | ✅ Sí (getDescripcion()) |
| **Mantenibilidad** | ⚠️ Difícil | ✅ Fácil |

## Próximos Pasos (Opcional)

Si quieres mejorar aún más, considera:

1. **Crear constantes de autoridad en Role.java:**
   ```java
   public String getAuthority() {
       return this.authority;
   }
   ```

2. **Usar Role.authority en lugar de Role.name():**
   ```java
   .requestMatchers("/api/admin/**").hasRole(Role.ADMIN.getAuthority())
   ```

3. **Crear una clase RoleConstants para expresiones SpEL:**
   ```java
   public class RoleConstants {
       public static final String ADMIN = "ADMIN";
       public static final String TECNICO = "TECNICO";
       // ... más constantes
   }
   
   // Usar en @PreAuthorize
   @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
   ```

## Referencias

- **Role.java** - Definición central de roles
- **SecurityConfig.java** - Configuración de autorización
- **AzureAdJwtAuthenticationConverter.java** - Conversión de JWT
- **AUTHORIZATION_GUIDE.md** - Documentación completa
