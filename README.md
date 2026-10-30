# Plataforma de consumo de IA — Backend

API en Spring Boot que expone un servicio de generación de texto simulado protegido por una **cadena de proxies**: primero se controla el número de solicitudes por minuto y luego la cuota mensual de tokens según el plan del usuario.

## Patrón Proxy

```
Cliente ──► RateLimitProxyService ──► QuotaProxyService ──► MockAIGenerationService
              (429 Too Many Requests)   (402 Payment Required)   (servicio real)
```

| Rol del patrón | Clase |
|---|---|
| Sujeto (interfaz común) | `AIGenerationService` |
| Sujeto real | `MockAIGenerationService` — simula la generación con `Thread.sleep(1200)` y devuelve un texto predefinido aleatorio |
| Proxy 1 | `RateLimitProxyService` — verifica las solicitudes por minuto; si se supera responde **429** con el encabezado `Retry-After` |
| Proxy 2 | `QuotaProxyService` — verifica la cuota mensual, descuenta los tokens consumidos y responde **402** si no alcanza |

La cadena se arma en `ProxyChainConfig`. El controlador solo conoce la interfaz `AIGenerationService`, así que no sabe si habla con un proxy o con el servicio real.

## Planes

| Plan | Solicitudes por minuto | Tokens al mes |
|---|---|---|
| FREE | 10 | 50.000 |
| PRO | 60 | 500.000 |
| ENTERPRISE | Ilimitadas | Ilimitados |

Los tokens se estiman como `ceil(caracteres / 4)` para el prompt y para la respuesta.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/ai/generate` | Genera texto pasando por toda la cadena de proxies. Cuerpo: `{ "userId", "prompt" }` |
| GET | `/api/quota/status?userId=` | Tokens usados, tokens restantes, fecha de reinicio, plan actual y estado del límite por minuto |
| GET | `/api/quota/history?userId=` | Uso diario de los últimos 7 días |
| POST | `/api/quota/upgrade` | Mejora de plan (FREE → PRO, o al plan indicado). Cuerpo: `{ "userId", "targetPlan" }` |
| GET | `/api/quota/plans` | Lista de planes disponibles |

## Tareas programadas

`QuotaResetScheduler` reinicia el contador de solicitudes cada minuto (`0 * * * * *`) y la cuota mensual el primer día de cada mes (`0 0 0 1 * *`).

## Ejecutar en local

```bash
mvn spring-boot:run
```

Variables de entorno:

| Variable | Uso | Valor por defecto |
|---|---|---|
| `PORT` | Puerto del servidor | `8080` |
| `ALLOWED_ORIGINS` | Orígenes permitidos por CORS, separados por comas | `http://localhost:5173` |
| `MOCK_LATENCY_MS` | Latencia simulada del servicio de IA | `1200` |

## Pruebas

```bash
mvn test
```

## Despliegue

Desplegado en Render con el `Dockerfile` del repositorio.
