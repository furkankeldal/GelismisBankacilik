# 📋 Log Görüntüleme Kılavuzu

## Postman'den İstek Attığınızda Loglar Nerede Görünür?

### Senaryo 1: Docker Compose ile Çalıştırıyorsanız

Postman'den istek attığınızda loglar **Docker container loglarında** görünür.

#### Real-time Log Takibi (Önerilen)

```bash
# API Gateway loglarını real-time takip et
docker compose logs -f api-gateway
```

Bu komutu çalıştırdıktan sonra Postman'den istek attığınızda loglar anında görünecektir.

#### Son Logları Görüntüleme

```bash
# Son 100 satırı göster
docker compose logs --tail 100 api-gateway

# Son 50 satırı göster
docker compose logs --tail 50 api-gateway
```

#### Belirli Bir Trace ID ile Filtreleme

```bash
# Trace ID ile filtrele (örnek)
docker compose logs api-gateway | grep "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

### Senaryo 2: IDE'de Çalıştırıyorsanız

Eğer API Gateway'i IDE'den (IntelliJ, Eclipse, VS Code) çalıştırıyorsanız:

1. **IDE Console'unda** loglar görünür
2. IDE'nin "Run" veya "Console" sekmesine bakın
3. Postman'den istek attığınızda loglar anında görünecektir

### Senaryo 3: Terminal'de Çalıştırıyorsanız

```bash
cd api-gateway
mvn spring-boot:run
```

Bu durumda loglar **terminal output'unda** görünür.

---

## Örnek Postman İsteği ve Log Çıktısı

### Postman İsteği:
```
POST http://localhost:8095/api/accounts
Headers:
  Content-Type: application/json
  Authorization: Bearer <JWT_TOKEN>
Body:
{
  "customerId": 1,
  "accountType": "VADELI",
  "currencyType": "TRY",
  "balance": 1000.00
}
```

### Beklenen Log Çıktısı:

```log
2025-12-18 10:30:45.123  INFO --- [ctor-http-nio-2] c.e.gateway.filter.RequestResponseLoggingFilter : [REQUEST] TraceId: a1b2c3d4-e5f6-7890-abcd-ef1234567890, Method: POST, Path: /api/accounts, Headers: {Content-Type=[application/json], Authorization=[Bearer eyJhbGci...]}, QueryParams: {}, RemoteAddress: /127.0.0.1:54321

2025-12-18 10:30:45.124  INFO --- [ctor-http-nio-2] c.e.gateway.filter.RequestResponseLoggingFilter : [REQUEST BODY] TraceId: a1b2c3d4-e5f6-7890-abcd-ef1234567890, Body: {"customerId":1,"accountType":"VADELI","currencyType":"TRY","balance":1000.00}

2025-12-18 10:30:45.234  INFO --- [ctor-http-nio-2] c.e.gateway.filter.RequestResponseLoggingFilter : [RESPONSE] TraceId: a1b2c3d4-e5f6-7890-abcd-ef1234567890, Status: 200 OK, Duration: 110ms, Headers: {Content-Type=[application/json]}, Body: {"accountNo":"1234567890","customerId":1,"accountType":"VADELI","currencyType":"TRY","balance":1000.00,"createdDate":"2025-12-18T10:30:45.200Z"}
```

---

## Hızlı Komutlar

### Docker Compose ile

```bash
# Real-time log takibi (en kullanışlı)
docker compose logs -f api-gateway

# Son 100 satır
docker compose logs --tail 100 api-gateway

# Belirli bir tarihten itibaren
docker compose logs --since 2025-12-18T10:00:00 api-gateway

# Tüm servislerin logları
docker compose logs -f

# Sadece REQUEST loglarını göster
docker compose logs api-gateway | grep "\[REQUEST\]"

# Sadece RESPONSE loglarını göster
docker compose logs api-gateway | grep "\[RESPONSE\]"
```

### Docker ile (Docker Compose kullanmıyorsanız)

```bash
# Container loglarını görüntüle
docker logs bank-api-gateway

# Real-time takip
docker logs -f bank-api-gateway

# Son 100 satır
docker logs --tail 100 bank-api-gateway
```

---

## Log Formatı Açıklaması

### [REQUEST] Logu
- **TraceId**: Her request için benzersiz UUID
- **Method**: HTTP metodu (GET, POST, PUT, DELETE)
- **Path**: İstek yolu (/api/accounts)
- **Headers**: Tüm HTTP header'ları
- **QueryParams**: URL query parametreleri
- **RemoteAddress**: Client IP adresi

### [REQUEST BODY] Logu
- **TraceId**: Request ile aynı trace ID
- **Body**: Request body içeriği (JSON)

### [RESPONSE] Logu
- **TraceId**: Request ile aynı trace ID
- **Status**: HTTP status kodu (200, 404, 500, vb.)
- **Duration**: İşlem süresi (milisaniye)
- **Headers**: Response header'ları
- **Body**: Response body (500 karakterden uzunsa kesilir)

---

## İpuçları

1. **Real-time Takip**: `docker compose logs -f api-gateway` komutunu bir terminal'de açık tutun
2. **Trace ID ile Takip**: Her request için benzersiz trace ID oluşturulur, bu ID ile tüm logları filtreleyebilirsiniz
3. **Log Seviyesi**: `config-repo/api-gateway.yml` dosyasında log seviyesini ayarlayabilirsiniz
4. **Log Dosyasına Kaydetme**: İsterseniz logları dosyaya da kaydedebilirsiniz (logback.xml yapılandırması gerekir)

---

## Sorun Giderme

### Loglar görünmüyor
1. API Gateway'in çalıştığından emin olun: `docker compose ps`
2. Log seviyesini kontrol edin: `config-repo/api-gateway.yml` dosyasında `logging.level.com.example.gateway: INFO` olmalı
3. Container'ın loglarını kontrol edin: `docker compose logs api-gateway`

### Loglar çok fazla
- Log seviyesini `DEBUG` yerine `INFO` yapın
- Sadece belirli logları filtreleyin: `docker compose logs api-gateway | grep "\[REQUEST\]"`

### Trace ID bulamıyorum
- Her request için otomatik olarak UUID oluşturulur
- Loglarda `TraceId:` ile arayın
- Aynı trace ID'yi kullanarak tüm logları filtreleyebilirsiniz

