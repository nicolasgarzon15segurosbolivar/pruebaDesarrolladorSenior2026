# API de Gestión de Pólizas

API REST para administrar pólizas de arrendamiento (individuales y colectivas) junto con sus riesgos. Permite consultarlas, renovarlas, cancelarlas y gestionar los riesgos de cada una. Cada vez que una póliza o un riesgo cambia de estado, se notifica al CORE de seguros.

Está hecha con Spring Boot 3 y Java 21, con una base de datos H2 en memoria para levantarla sin instalar nada más.

## Requisitos

- Java 21
- Maven 3.9 o superior

Verificar con `java -version` y `mvn -version`.

## Levantar servicio

Desde la carpeta del proyecto:

```
mvn spring-boot:run
```

Queda escuchando en `http://localhost:8080`.

La base de datos es en memoria, así que cada vez que arranca se recrea desde cero con unos datos de ejemplo (tres pólizas y sus riesgos). 

Si necesitas mirar los datos directamente, la consola de H2 está en `http://localhost:8080/h2-console` (URL JDBC `jdbc:h2:mem:polizasdb`, usuario `sa`, sin contraseña).

## Autenticación

Todas las peticiones necesitan el header:

```
x-api-key: 123456
```

Si no lo mandas, la API responde 401. 

## Endpoints

| Método | Ruta | Qué hace |
|--------|------|----------|
| GET | `/polizas` | Lista pólizas (paginado). Acepta `?tipo=` y `?estado=` para filtrar. |
| GET | `/polizas/{id}` | Trae una póliza por su id. |
| GET | `/polizas/{id}/riesgos` | Lista los riesgos de una póliza. |
| POST | `/polizas/{id}/renovar` | Renueva la póliza: sube el canon y la prima según el IPC y la pasa a RENOVADA. |
| POST | `/polizas/{id}/cancelar` | Cancela la póliza y todos sus riesgos. |
| POST | `/polizas/{id}/riesgos` | Agrega un riesgo (solo en pólizas colectivas). |
| POST | `/riesgos/{id}/cancelar` | Cancela un riesgo puntual. |
| POST | `/core-mock/evento` | Simula la recepción de un evento en el CORE. |

`tipo` puede ser `INDIVIDUAL` o `COLECTIVA`. `estado` puede ser `VIGENTE`, `RENOVADA` o `CANCELADA`.

El listado viene paginado. Puedes controlarlo con `page` (desde 0), `size` y `sort`. Por ejemplo, la segunda página de 10 ordenada por canon descendente:

```bash
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?page=1&size=10&sort=canonMensual,desc"
```

La respuesta incluye el contenido en `content` junto con los datos de paginación (`totalElements`, `totalPages`, `number`, etc.).

## Reglas de negocio

- Una póliza individual tiene un solo riesgo; solo las colectivas admiten agregar más.
- Una póliza cancelada no se puede renovar.
- Al cancelar una póliza se cancelan también todos sus riesgos.
- Al renovar, el canon sube según el IPC (configurable en `application.yml`, por defecto 5.5%) y la prima se recalcula como canon × meses de vigencia.

## Cómo está organizado

El código sigue una separación por capas. Siguiendo el recorrido de una petición, de entrada a salida:

- `security` — valida el `x-api-key` antes que nada.
- `controller` — recibe las peticiones HTTP ya autenticadas.
- `dto` — objetos de entrada y salida que viajan por el controller.
- `service` — concentra las reglas de negocio.
- `repository` — acceso a datos con Spring Data JPA.
- `model` — las entidades (`Poliza`, `Riesgo`) y sus enums.
- `core` — la integración con el CORE de seguros tras cada cambio de estado.
- `exception` — manejo centralizado de errores en cualquier punto del flujo.

## Errores

Cuando algo falla, la respuesta trae un JSON con el detalle:

```json
{
  "timestamp": "2026-10-10T20:56:23.11",
  "status": 409,
  "error": "Conflicto de negocio",
  "message": "No se puede renovar la poliza 3 porque esta CANCELADA"
}
```

Los códigos que devuelve: 400 si el cuerpo o un parámetro vienen mal, 401 si falta la api-key, 404 si la póliza o el riesgo no existen, y 409 cuando se rompe una regla de negocio.

## Tests

```
mvn test
```

Hay pruebas unitarias sobre las reglas de negocio del servicio y pruebas de integración que levantan la API y verifican los endpoints, la seguridad y los códigos de respuesta.

## Supuestos y pendientes para una próxima versión

- La renovación se puede hacer en cualquier momento mientras la póliza no esté cancelada. No hay todavía una ventana de renovación (por ejemplo, "solo en los últimos 30 días antes del vencimiento") ni se recalculan las fechas de vigencia; eso habría que definirlo con el negocio.
- Renovar actualiza la misma póliza. Si se necesita conservar el histórico, convendría que la renovación genere un registro nuevo enlazado al anterior.
- El IPC es un valor fijo configurable. En un escenario real vendría de una fuente oficial actualizada.
- La autenticación es por una api-key única. Para producción iría algo más robusto (OAuth2/JWT, claves por cliente y rotación).
- La "renovación automática" que menciona el negocio quedaría como un proceso programado que detecte las pólizas por vencer; aquí la renovación es a demanda por el endpoint.
