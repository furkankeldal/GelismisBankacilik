# API Gateway İyileştirmeleri

## ✅ Eklenen Özellikler

### 1. Authentication & Authorization

**Dosya:** `api-gateway/src/main/java/com/example/gateway/filter/AuthenticationFilter.java`

**Özellikler:**
- JWT token validation (Authorization header)
- API key validation (X-API-Key header)
- Public path tanımlama (authentication bypass)
- Configurable authentication (enabled/disabled)

**Konfigürasyon:**
```yaml
app:
  gateway:
    auth:
      enabled: true
      api-key-header: X-API-Key
      jwt-header: Authorization
      public-paths: /api/customers/public,/actuator
```

**Kullanım:**
```bash
# JWT ile istek
curl -H "Authorization: Bearer <token>" http://localhost:8095/api/accounts/123

# API Key ile istek
curl -H "X-API-Key: <api-key>" http://localhost:8095/api/accounts/123
```

---

### 2. Rate Limiting

**Dosyalar:**
- `config-repo/api-gateway.yml` - Route-level rate limiting
- `api-gateway/src/main/java/com/example/gateway/config/RateLimiterConfig.java` - Key resolver
- `api-gateway/src/main/java/com/example/gateway/filter/RateLimitFilter.java` - Logging

**Özellikler:**
- Redis-based rate limiting
- IP-based veya API Key-based limiting
- Per-route rate limit ayarları
- Rate limit header'ları (X-RateLimit-Limit, X-RateLimit-Remaining)

**Konfigürasyon:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: account-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10  # Saniyede 10 request
                redis-rate-limiter.burstCapacity: 20  # Maksimum 20 request
                key-resolver: "#{@ipKeyResolver}"
```

**Response Headers:**
- `X-RateLimit-Limit`: İzin verilen maksimum request sayısı
- `X-RateLimit-Remaining`: Kalan request sayısı
- `X-RateLimit-Reset`: Rate limit'in sıfırlanacağı zaman

---

### 3. Request/Response Transformation & Logging

**Dosya:** `api-gateway/src/main/java/com/example/gateway/filter/RequestResponseLoggingFilter.java`

**Özellikler:**
- Request logging (method, path, headers, query params, body)
- Response logging (status, duration, headers, body)
- Distributed tracing (Trace ID generation ve propagation)
- Response time tracking

**Trace ID:**
- Her request için unique trace ID oluşturulur
- Request ve response header'larında `X-Trace-Id` olarak gönderilir
- Log'larda trace ID ile request'leri takip edebilirsiniz

**Log Formatı:**
```
[REQUEST] TraceId: abc-123, Method: GET, Path: /api/accounts/123, Headers: {...}
[REQUEST BODY] TraceId: abc-123, Body: {...}
[RESPONSE] TraceId: abc-123, Status: 200, Duration: 45ms, Headers: {...}
```

---

### 4. Monitoring & Metrics

**Dosya:** `config-repo/api-gateway.yml`

**Özellikler:**
- Prometheus metrics endpoint (`/actuator/prometheus`)
- Gateway metrics endpoint (`/actuator/gateway`)
- Health check endpoint (`/actuator/health`)
- Metrics tagging (application name)

**Konfigürasyon:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

**Metrics Endpoints:**
- `/actuator/metrics` - Tüm metrikler
- `/actuator/prometheus` - Prometheus formatında metrikler
- `/actuator/gateway/routes` - Route bilgileri
- `/actuator/health` - Health check

---

## 📋 Dependency'ler

**Eklenen Dependency'ler:**
```xml
<!-- JWT Authentication -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Rate Limiting - Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>

<!-- Monitoring & Metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Distributed Tracing -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
```

---

## 🔧 Yapılandırma Örnekleri

### Development Ortamı

```yaml
app:
  gateway:
    auth:
      enabled: false  # Development'ta auth kapalı
    rate-limit:
      enabled: true
      requests-per-minute: 100  # Daha yüksek limit
```

### Production Ortamı

```yaml
app:
  gateway:
    auth:
      enabled: true
      public-paths: /actuator/health  # Sadece health check public
    rate-limit:
      enabled: true
      requests-per-minute: 60  # Daha düşük limit
```

---

## 🚀 Kullanım Senaryoları

### Senaryo 1: JWT Authentication ile İstek

```bash
# Token ile istek
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  http://localhost:8095/api/accounts/123
```

### Senaryo 2: API Key ile İstek

```bash
# API Key ile istek
curl -H "X-API-Key: my-secret-api-key" \
  http://localhost:8095/api/accounts/123
```

### Senaryo 3: Rate Limit Kontrolü

```bash
# Rate limit header'larını kontrol et
curl -v http://localhost:8095/api/accounts/123

# Response headers:
# X-RateLimit-Limit: 10
# X-RateLimit-Remaining: 9
# X-RateLimit-Reset: 1699123456
```

### Senaryo 4: Trace ID ile Log Takibi

```bash
# İstek yap
curl http://localhost:8095/api/accounts/123

# Response header'da trace ID alınır:
# X-Trace-Id: abc-123-def-456

# Log'larda trace ID ile filtreleme:
grep "abc-123-def-456" gateway.log
```

---

## 📊 Monitoring Dashboard

Prometheus + Grafana ile monitoring:

1. **Prometheus Configuration:**
```yaml
scrape_configs:
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8095']
```

2. **Grafana Dashboard:**
- Request rate (requests/second)
- Response time (p50, p95, p99)
- Error rate (4xx, 5xx)
- Rate limit violations
- Active connections

---

## ✅ Sonuç

API Gateway artık production-ready:
- ✅ Authentication & Authorization (JWT + API Key)
- ✅ Rate Limiting (Redis-based)
- ✅ Request/Response Logging & Tracing
- ✅ Monitoring & Metrics (Prometheus)

Detaylı konfigürasyon için `config-repo/api-gateway.yml` dosyasına bakın.


