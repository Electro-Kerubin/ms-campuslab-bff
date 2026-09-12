# Integración de Microservicios - CampusLab BFF

## 📋 Descripción General

Este documento describe la implementación del patrón **Backend For Frontend (BFF)** donde el ms-campuslab-bff orquesta llamadas a múltiples microservicios de dominio y expone APIs REST unificadas al frontend Angular.

## 🏗️ Arquitectura de Integración

```
┌─────────────────────┐
│   Angular Frontend  │
└──────────┬──────────┘
           │ HTTP/REST
           ▼
┌──────────────────────────────────────────┐
│    ms-campuslab-bff (API Gateway)        │
├──────────────────────────────────────────┤
│  ┌─────────────┐ ┌────────────────────┐ │
│  │  Services   │ │  Feign Clients     │ │
│  ├─────────────┤ ├────────────────────┤ │
│  │ Booking     │ │ BookingsClient     │ │
│  │ Catalog     │ │ CatalogClient      │ │
│  │ Report      │ │ ReportClient       │ │
│  └─────────────┘ └────────────────────┘ │
│                                          │
│  ┌─────────────────────────────────┐    │
│  │   Circuit Breaker (Resilience4j) │   │
│  └─────────────────────────────────┘    │
└──────────────────────────────────────────┘
   ▲        ▲        ▲        ▲
   │        │        │        │
   ▼        ▼        ▼        ▼
┌─────────┐┌──────────┐┌──────────┐┌────────────┐
│ms-booking││ms-catalog││ms-report ││ms-audit    │
│(Reserve)││(Labs)    ││(KPI)     ││(Auditoría) │
└─────────┘└──────────┘└──────────┘└────────────┘
```

## 📦 Componentes Implementados

### 1. Clientes Feign (Fachadas HTTP)

**BookingsClient**
- Interface Feign para comunicación con ms-bookings
- Métodos: getAllBookings, getBookingById, createBooking, updateBooking, cancelBooking, approveBooking

**CatalogClient**
- Interface Feign para comunicación con ms-catalog
- Métodos para laboratorios: getAllLabs, getLabById, getLabsByUbicacion, createLab, updateLab, deleteLab
- Métodos para equipos: getAllEquipment, getEquipmentByLab, getEquipmentById, createEquipment, updateEquipment, deleteEquipment

**ReportClient**
- Interface Feign para comunicación con ms-report
- Métodos: getOcupacionKPI, getCicloTimeKPI, getEquiposOcupadosKPI, getOcupacionReport, generarReporte

### 2. DTOs (Transferencia de Datos)

**LabDTO**
- id, nombre, descripcion, ubicacion, capacidad, estado, responsable
- horarioApertura, horarioCierre, equiposDisponibles

**EquipmentDTO**
- id, nombre, descripcion, labId, labNombre, estado, cantidad
- marca, modelo, numeroSerie, ultimoMantenimiento, proximoMantenimiento

**BookingDTO**
- id, labId, labNombre, estudianteId, estudianteNombre
- fechaInicio, fechaFin, estado, proposito, capacidadEsperada
- fechaCreacion, tecnicoAprobador, observaciones

**ReportDTO**
- id, titulo, descripcion, tipo, fechaGeneracion, periodo
- datos (Map<String, Object>)

### 3. Servicios de Orquestación

**BookingService**
- Orquesta llamadas a BookingsClient y CatalogClient
- Enriquece reservas con información del laboratorio
- Manejo de errores con Circuit Breaker
- Métodos: getAllBookings, getBookingById, createBooking, updateBooking, cancelBooking, checkAvailability, approveBooking

**CatalogService**
- Orquesta llamadas a CatalogClient
- Gestión completa de laboratorios y equipos
- Manejo de errores con Circuit Breaker
- Métodos completos para CRUD de laboratorios y equipos

**ReportService**
- Orquesta llamadas a ReportClient
- Obtención de KPIs y reportes
- Manejo de errores con Circuit Breaker
- Métodos para KPIs y reportes personalizados

### 4. Manejo de Errores

**Excepciones Personalizadas**
- `MicroserviceException`: Excepción base
- `MicroserviceNotFoundException`: Error 404
- `MicroserviceTimeoutException`: Error 408/504

**FeignErrorDecoder**
- Convierte respuestas HTTP de error en excepciones específicas
- Integra con GlobalExceptionHandler para respuestas consistentes

### 5. Resiliencia

**Circuit Breaker (Resilience4j)**
- Cada servicio tiene anotación @CircuitBreaker
- Fallback methods para manejar fallos
- Configuración centralizada en resilience4j-config

## 🔌 Configuración de Microservicios

En `application.yml`, agregar las URLs de los microservicios:

```yaml
microservice:
  bookings:
    url: http://ms-bookings:8081
  catalog:
    url: http://ms-catalog:8082
  report:
    url: http://ms-report:8084

# Resilience4j Configuration
resilience4j:
  circuitbreaker:
    instances:
      bookingService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5000
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
      catalogService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        waitDurationInOpenState: 5000
        failureRateThreshold: 50
      reportService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        waitDurationInOpenState: 5000
        failureRateThreshold: 50
```

## 🎯 Controladores REST (Frontend API)

### BookingController
**Base Path:** `/api/bookings`

```
GET    /api/bookings              → Obtener todas las reservas
GET    /api/bookings/{id}         → Obtener reserva específica
POST   /api/bookings              → Crear reserva
PUT    /api/bookings/{id}         → Actualizar reserva
DELETE /api/bookings/{id}         → Cancelar reserva
GET    /api/bookings/availability/{labId} → Verificar disponibilidad
POST   /api/bookings/{id}/approve → Aprobar reserva (TECNICO/ADMIN)
POST   /api/bookings/{id}/reject  → Rechazar reserva (TECNICO/ADMIN)
```

### LabController
**Base Path:** `/api/labs`

```
GET    /api/labs                  → Obtener todos los laboratorios
GET    /api/labs/{id}             → Obtener laboratorio específico
GET    /api/labs/ubicacion/{ubicacion} → Filtrar por ubicación
GET    /api/labs/disponibles      → Obtener labs disponibles
POST   /api/labs                  → Crear laboratorio (ADMIN)
PUT    /api/labs/{id}             → Actualizar laboratorio (ADMIN)
DELETE /api/labs/{id}             → Eliminar laboratorio (ADMIN)
GET    /api/labs/{id}/equipos     → Obtener equipos del lab
```

### EquipmentController
**Base Path:** `/api/equipment`

```
GET    /api/equipment             → Obtener todos los equipos
GET    /api/equipment/{id}        → Obtener equipo específico
GET    /api/equipment/lab/{labId} → Obtener equipos por lab
POST   /api/equipment             → Crear equipo (ADMIN)
PUT    /api/equipment/{id}        → Actualizar equipo (ADMIN)
DELETE /api/equipment/{id}        → Eliminar equipo (ADMIN)
```

### ReportController
**Base Path:** `/api/reports`

```
GET    /api/reports/kpi/ocupacion              → KPI ocupación general
GET    /api/reports/kpi/ocupacion/{labId}      → KPI ocupación por lab
GET    /api/reports/kpi/tiempo-ciclo           → KPI tiempo de ciclo
GET    /api/reports/kpi/equipos-ocupados       → KPI equipos ocupados
GET    /api/reports/ocupacion                  → Reporte ocupación (fechas)
GET    /api/reports/ocupacion-por-lab          → Reporte por lab
GET    /api/reports/equipos-no-devueltos       → Reporte equipos no devueltos
GET    /api/reports/uso-por-estudiante/{id}    → Reporte uso estudiante
POST   /api/reports/generar                    → Generar reporte personalizado
```

## 🔐 Autorización por Rol

Todos los endpoints heredan la autorización configurada en SecurityConfig:

| Recurso | ADMIN | TECNICO | ESTUDIANTE | AUDITOR |
|---------|-------|---------|-----------|---------|
| GET /bookings | ✓ | ✓ | ✓ (propios) | ✓ |
| POST /bookings | ✓ | - | ✓ | - |
| POST /bookings/{id}/approve | ✓ | ✓ | - | - |
| GET /labs | ✓ | ✓ | ✓ | ✓ |
| POST /labs | ✓ | - | - | - |
| GET /equipment | ✓ | ✓ | ✓ | ✓ |
| POST /equipment | ✓ | - | - | - |
| GET /reports | ✓ | ✓ | - | ✓ |
| POST /reports/generar | ✓ | ✓ | - | - |

## 📊 Enriquecimiento de Datos

El BFF enriquece datos al combinar múltiples microservicios:

**Ejemplo: BookingDTO enriquecido**
```json
{
  "id": "BOOK-001",
  "labId": "LAB-001",
  "labNombre": "Laboratorio de Informática A",  // ← De ms-catalog
  "estudianteId": "EST-001",
  "estudianteNombre": "Juan Pérez",
  "fechaInicio": "2024-09-15T09:00:00",
  "fechaFin": "2024-09-15T11:00:00",
  "estado": "APROBADA",
  "proposito": "Práctica de Programación",
  "capacidadEsperada": 25,
  "equiposDisponibles": 20,  // ← Información agregada
  "tecnicoAprobador": "Carlos Técnico"
}
```

## 🔄 Flujo de Integración

### 1. Crear Reserva (ESTUDIANTE)

```
Frontend                BFF                 ms-bookings         ms-catalog
   │                    │                        │                  │
   ├─ POST /bookings ──>│                        │                  │
   │                    ├─ POST /bookings ─────>│                  │
   │                    │                        ├─ Validar ─────>│
   │                    │<───── BookingDTO ──────┤                  │
   │                    │                        │                  │
   │                    ├─ GET /labs/{id} ─────────────────────────>│
   │                    │<─ LabDTO ──────────────────────────────────┤
   │                    │                        │                  │
   │<── BookingDTO ─────┤                        │                  │
   │    enriquecido     │                        │                  │
```

### 2. Obtener KPIs (ADMIN)

```
Frontend                BFF                 ms-report
   │                    │                        │
   ├─ GET /reports/kpi >│                        │
   │                    ├─ GET /kpi ───────────>│
   │                    │<─ KPI Data ────────────┤
   │                    │                        │
   │<── KPI JSON ───────┤                        │
```

## 🛠️ Testing desde Frontend Angular

**Ejemplo HTTP Client (Angular):**

```typescript
import { HttpClient } from '@angular/common/http';

export class BookingService {
  constructor(private http: HttpClient) {}

  getAllBookings() {
    return this.http.get<BookingDTO[]>('/api/bookings');
  }

  createBooking(booking: BookingDTO) {
    return this.http.post<BookingDTO>('/api/bookings', booking);
  }

  approveBooking(id: string) {
    return this.http.post(`/api/bookings/${id}/approve`, {});
  }
}
```

## 📝 Dependencias Requeridas

```xml
<!-- Spring Cloud OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Resilience4j -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>

<!-- Spring Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 🚀 Cómo Iniciarse

1. **Clonar repositorios de microservicios**
   ```bash
   git clone <ms-bookings>
   git clone <ms-catalog>
   git clone <ms-report>
   ```

2. **Instalar Feign Clients**
   - Los clientes ya están implementados en este BFF

3. **Configurar URLs de microservicios**
   - Editar `application.yml` con las URLs correctas
   - Usar variables de entorno para producción

4. **Iniciar servicios**
   ```bash
   docker-compose up -d
   mvn spring-boot:run
   ```

5. **Probar endpoints**
   ```bash
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/bookings
   ```

## 📚 Referencias Adicionales

- [[AUTHORIZATION_GUIDE]] - Autorización por roles
- [[KEYCLOAK_INTEGRATION]] - Integración con Keycloak
- [[REFACTORING_ROLES]] - Refactorización de roles
