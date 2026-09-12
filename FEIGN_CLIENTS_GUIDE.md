# Guía de Clientes Feign - CampusLab BFF

## 📋 Resumen Ejecutivo

Esta guía describe cómo usar los **clientes HTTP Feign** para comunicarse con los microservicios de dominio desde el BFF. Feign abstraem los detalles de comunicación HTTP y proporciona una interfaz declarativa.

## 🏗️ Clientes Implementados

### 1. BookingsClient (ms-bookings)
**Comunicación:** Gestión de reservas de laboratorios
**Puertos:** Puerto 8081 (default)
**Métodos:** CRUD completo de reservas + aprobación/rechazo

### 2. CatalogClient (ms-catalog)
**Comunicación:** Catálogo de laboratorios y equipos
**Puertos:** Puerto 8082 (default)
**Métodos:** CRUD de laboratorios y equipos

### 3. ReportClient (ms-report)
**Comunicación:** Reportería y KPIs
**Puertos:** Puerto 8084 (default)
**Métodos:** Obtención de KPIs, reportes, estadísticas

### 4. AuditClient (ms-audit)
**Comunicación:** Auditoría y registro de eventos
**Puertos:** Puerto 8083 (default)
**Métodos:** Lectura de eventos, generación de reportes de auditoría

## 🔧 Configuración

### 1. Propiedades de Microservicios (application.yml)

```yaml
# URLs de microservicios (usar en desarrollo local)
microservice:
  bookings:
    url: http://localhost:8081
  catalog:
    url: http://localhost:8082
  audit:
    url: http://localhost:8083
  report:
    url: http://localhost:8084

# En Docker Compose
microservice:
  bookings:
    url: http://ms-bookings:8081
  catalog:
    url: http://ms-catalog:8082
  audit:
    url: http://ms-audit:8083
  report:
    url: http://ms-report:8084

# Timeouts y retries (Feign + Resilience4j)
feign:
  client:
    config:
      default:
        connectTimeout: 5000    # ms
        readTimeout: 10000      # ms
        loggerLevel: full
      
      ms-bookings:
        connectTimeout: 5000
        readTimeout: 15000
      
      ms-catalog:
        connectTimeout: 5000
        readTimeout: 10000
      
      ms-audit:
        connectTimeout: 5000
        readTimeout: 10000
```

### 2. Configuración de Feign (FeignConfig.java)

**Características:**
- Logger Level: FULL (ideal para desarrollo)
- Error Decoder personalizado que convierte errores HTTP en excepciones
- Logger custom para debug

## 📝 Cómo Usar los Clientes

### BookingsClient - Ejemplo de Uso

```java
@Service
public class BookingService {
    
    @Autowired
    private BookingsClient bookingsClient;
    
    // Obtener todas las reservas
    public List<BookingDTO> obtenerReservas(String labId, String estado) {
        ResponseEntity<List<BookingDTO>> response = 
            bookingsClient.getAllBookings(labId, estado, null);
        return response.getBody();
    }
    
    // Crear reserva
    public BookingDTO crearReserva(Map<String, Object> datos) {
        ResponseEntity<BookingDTO> response = 
            bookingsClient.createBooking(datos);
        return response.getBody();
    }
    
    // Aprobar reserva
    public BookingDTO aprobarReserva(String bookingId) {
        ResponseEntity<BookingDTO> response = 
            bookingsClient.approveBooking(bookingId);
        return response.getBody();
    }
}
```

### CatalogClient - Ejemplo de Uso

```java
@Service
public class CatalogService {
    
    @Autowired
    private CatalogClient catalogClient;
    
    // Obtener todos los laboratorios
    public List<LabDTO> obtenerLaboratorios() {
        ResponseEntity<List<LabDTO>> response = 
            catalogClient.getAllLabs();
        return response.getBody();
    }
    
    // Obtener laboratorio específico
    public LabDTO obtenerLaboratorio(String labId) {
        ResponseEntity<LabDTO> response = 
            catalogClient.getLabById(labId);
        return response.getBody();
    }
    
    // Obtener equipos de laboratorio
    public List<EquipmentDTO> obtenerEquipos(String labId) {
        ResponseEntity<List<EquipmentDTO>> response = 
            catalogClient.getEquipmentByLab(labId);
        return response.getBody();
    }
}
```

### ReportClient - Ejemplo de Uso

```java
@Service
public class ReportService {
    
    @Autowired
    private ReportClient reportClient;
    
    // Obtener KPI de ocupación
    public Map<String, Object> obtenerKPIOcupacion() {
        ResponseEntity<Map<String, Object>> response = 
            reportClient.getOcupacionKPI();
        return response.getBody();
    }
    
    // Obtener KPI por laboratorio
    public Map<String, Object> obtenerKPILab(String labId) {
        ResponseEntity<Map<String, Object>> response = 
            reportClient.getLabOcupacionKPI(labId);
        return response.getBody();
    }
    
    // Generar reporte personalizado
    public ReportDTO generarReporte(Map<String, Object> parametros) {
        ResponseEntity<ReportDTO> response = 
            reportClient.generarReporte(parametros);
        return response.getBody();
    }
}
```

### AuditClient - Ejemplo de Uso

```java
@Service
public class AuditService {
    
    @Autowired
    private AuditClient auditClient;
    
    // Obtener eventos de auditoría
    public List<Map<String, Object>> obtenerEventos(String accion, String usuario) {
        ResponseEntity<List<Map<String, Object>>> response = 
            auditClient.getEventos(accion, usuario, null, null, 100);
        return response.getBody();
    }
    
    // Obtener timeline de un recurso
    public Map<String, Object> obtenerTimelineRecurso(String recursoId) {
        ResponseEntity<Map<String, Object>> response = 
            auditClient.getTimelineRecurso(recursoId);
        return response.getBody();
    }
    
    // Obtener estadísticas
    public Map<String, Object> obtenerEstadisticas() {
        ResponseEntity<Map<String, Object>> response = 
            auditClient.getEstadisticas();
        return response.getBody();
    }
}
```

## 🔄 Flujo de Llamada HTTP

### Ciclo de vida de una solicitud Feign

```
1. Cliente llama al método de la interfaz Feign
   │
   ├─→ BookingsClient.getAllBookings(labId, estado, estudianteId)
   │
2. FeignConfig traduce la anotación a una solicitud HTTP
   │
   ├─→ GET http://ms-bookings:8081/api/bookings?labId=LAB-001&estado=APROBADA
   │
3. Se aplican los interceptores (headers, autenticación)
   │
   ├─→ Authorization: Bearer <JWT>
   ├─→ Content-Type: application/json
   │
4. Se envía la solicitud HTTP (con timeouts configurados)
   │
   ├─→ connectTimeout: 5000ms
   ├─→ readTimeout: 10000ms
   │
5. Se recibe la respuesta
   │
   ├─→ Status: 200 OK
   ├─→ Body: JSON (List<BookingDTO>)
   │
6. Si hay error, FeignErrorDecoder convierte a excepción
   │
   ├─→ 404 → MicroserviceNotFoundException
   ├─→ 504 → MicroserviceTimeoutException
   └─→ 500 → MicroserviceException
   │
7. Se retorna ResponseEntity<T> al servicio
   │
   └─→ response.getBody() para obtener el contenido
```

## ⚡ Características de Feign

### 1. Declarativo

```java
// No necesitas escribir código HTTP:
// ✗ NO necesitas esto
HttpClient client = HttpClientBuilder.create().build();
HttpGet request = new HttpGet("http://...");
// ... etc

// ✓ Solo necesitas esto
@GetMapping("/api/bookings")
ResponseEntity<List<BookingDTO>> getAllBookings(...);
```

### 2. Manejo automático de JSON

```java
// Serialización/deserialización automática
ResponseEntity<BookingDTO> response = bookingsClient.createBooking(data);
// El mapa Java se convierte a JSON automáticamente
// La respuesta JSON se convierte a BookingDTO automáticamente
```

### 3. Manejo de errores centralizado

```java
// FeignErrorDecoder convierte errores HTTP en excepciones
// GlobalExceptionHandler las maneja y retorna respuestas consistentes

// En FeignErrorDecoder:
case 404 → new MicroserviceNotFoundException(...)
case 504 → new MicroserviceTimeoutException(...)

// En GlobalExceptionHandler:
@ExceptionHandler(MicroserviceException.class)
public ResponseEntity<ErrorResponse> handle(...) {
    // Retorna 503 Service Unavailable con mensaje descriptivo
}
```

### 4. Logging completo

```
# Con Logger.Level.FULL, se registra:

[Feign] [BookingsClient#getAllBookings] --> GET http://localhost:8081/api/bookings HTTP/1.1
[Feign] [BookingsClient#getAllBookings] Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
[Feign] [BookingsClient#getAllBookings] Accept: application/json
[Feign] [BookingsClient#getAllBookings] ---> END HTTP (0-byte body)
[Feign] [BookingsClient#getAllBookings] <-- HTTP/1.1 200 OK (245ms)
[Feign] [BookingsClient#getAllBookings] Content-Type: application/json;charset=UTF-8
[Feign] [BookingsClient#getAllBookings] [{"id":"BOOK-001","labId":"LAB-001",...}]
[Feign] [BookingsClient#getAllBookings] <--- END HTTP (245-byte body)
```

## 🛡️ Manejo de Errores

### Excepciones Personalizadas

```java
// MicroserviceException
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 500: Internal Server Error
- 502: Bad Gateway
- 503: Service Unavailable

// MicroserviceNotFoundException (404)
- Recurso no encontrado en el microservicio
- No reintentar (el recurso no existe)

// MicroserviceTimeoutException (408, 504)
- Timeout al conectar con microservicio
- Puede reintentarse
- Fallback methods activados por Circuit Breaker
```

### Ejemplo de Manejo

```java
@Service
public class BookingService {
    
    @Autowired
    private BookingsClient bookingsClient;
    
    public BookingDTO obtenerReserva(String id) {
        try {
            ResponseEntity<BookingDTO> response = 
                bookingsClient.getBookingById(id);
            return response.getBody();
            
        } catch (MicroserviceNotFoundException e) {
            log.warn("Reserva no encontrada: {}", id);
            throw new CustomException("Reserva no existe");
            
        } catch (MicroserviceTimeoutException e) {
            log.error("Timeout al obtener reserva: {}", id);
            // Circuit Breaker activado, usar fallback
            return getFallbackBooking(id);
            
        } catch (MicroserviceException e) {
            log.error("Error en microservicio: {}", e.getMessage());
            throw new CustomException("Error al procesar solicitud");
        }
    }
}
```

## 🔐 Seguridad

### Headers Automáticos

Feign automáticamente propaga:
- `Authorization: Bearer <JWT>` (del contexto de Spring Security)
- `Content-Type: application/json`

```java
// El JWT se añade automáticamente al contexto de Spring
// Cada llamada Feign lo incluye en los headers
RequestContextHolder.getRequestAttributes().getAttribute("jwt")
// Se adjunta a cada solicitud HTTP
```

### Validación de JWT

```yaml
# En application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/auth/realms/campuslab

# Cada microservicio valida el JWT independientemente
# El BFF solo es un proxy que transmite la autorización
```

## 📊 Arquitectura de Comunicación

```
┌─────────────────┐
│  Frontend (Angular) │
└────────┬────────┘
         │ HTTP + JWT
         ▼
┌──────────────────────────────┐
│  ms-campuslab-bff            │
│  ┌──────────────────────────┐│
│  │ BookingApiController     ││
│  │ LabApiController         ││
│  │ ReportApiController      ││
│  └────────┬─────────────────┘│
│           │                  │
│  ┌────────▼─────────────────┐│
│  │ BookingService           ││
│  │ CatalogService           ││
│  │ ReportService            ││
│  └────────┬─────────────────┘│
│           │                  │
│  ┌────────▼─────────────────┐│
│  │ BookingsClient (Feign)   ││
│  │ CatalogClient (Feign)    ││
│  │ ReportClient (Feign)     ││
│  │ AuditClient (Feign)      ││
│  └────────┬─────────────────┘│
│           │                  │
│  FeignConfig + ErrorDecoder  │
│  Logger, Timeouts, Retries   │
└────────┬──────────────────────┘
         │ HTTP + JWT
    ┌────┴──────┬───────┬─────────┐
    ▼           ▼       ▼         ▼
┌───────────┐┌──────────┐┌──────────┐┌────────┐
│ms-bookings││ms-catalog││ms-report ││ms-audit│
│Port 8081  ││Port 8082 ││Port 8084 ││Port 8083
└───────────┘└──────────┘└──────────┘└────────┘
```

## 🧪 Testing de Clientes Feign

### Test Unitario

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class BookingsClientTest {
    
    @Autowired
    private BookingsClient bookingsClient;
    
    @Test
    public void testGetAllBookings() {
        // Arrange
        String labId = "LAB-001";
        String estado = "APROBADA";
        
        // Act
        ResponseEntity<List<BookingDTO>> response = 
            bookingsClient.getAllBookings(labId, estado, null);
        
        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }
}
```

### Test de Integración

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BffIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Test
    public void testFullFlow() {
        // 1. Crear reserva
        BookingDTO booking = new BookingDTO();
        booking.setLabId("LAB-001");
        // ... llenar datos
        
        // 2. Llamar al BFF
        ResponseEntity<BookingDTO> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/bookings",
            booking,
            BookingDTO.class
        );
        
        // 3. Verificar que se creó en ms-bookings
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
```

## 🚀 Deployment

### Docker Compose

```yaml
version: '3.8'
services:
  ms-bookings:
    image: ms-bookings:1.0.0
    ports:
      - "8081:8081"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/bookings
  
  ms-catalog:
    image: ms-catalog:1.0.0
    ports:
      - "8082:8082"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/catalog
  
  ms-audit:
    image: ms-audit:1.0.0
    ports:
      - "8083:8083"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/audit
  
  ms-report:
    image: ms-report:1.0.0
    ports:
      - "8084:8084"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/report
  
  ms-campuslab-bff:
    image: ms-campuslab-bff:1.0.0
    ports:
      - "8080:8080"
    environment:
      - MICROSERVICE_BOOKINGS_URL=http://ms-bookings:8081
      - MICROSERVICE_CATALOG_URL=http://ms-catalog:8082
      - MICROSERVICE_AUDIT_URL=http://ms-audit:8083
      - MICROSERVICE_REPORT_URL=http://ms-report:8084
    depends_on:
      - ms-bookings
      - ms-catalog
      - ms-audit
      - ms-report
```

## 📚 Referencias

- [Spring Cloud OpenFeign Documentation](https://spring.io/projects/spring-cloud-openfeign)
- [Feign Documentation](https://github.com/OpenFeign/feign)
- [Spring Cloud Resilience4j](https://spring.io/projects/spring-cloud-circuitbreaker)
- [FEIGN_CLIENTS_GUIDE.md](./FEIGN_CLIENTS_GUIDE.md) - Este archivo
- [MICROSERVICES_INTEGRATION.md](./MICROSERVICES_INTEGRATION.md) - Integración completa
