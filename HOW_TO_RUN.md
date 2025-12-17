# Sistem Çalıştırma ve API İstekleri Kılavuzu

## 📋 Gereksinimler

### Yazılım Gereksinimleri
- Java 17+ (Java 24 kullanılıyor)
- Maven 3.6+
- PostgreSQL 15+
- Redis 6+
- Apache Kafka 3+

### Servis Portları
- Config Server: `8888`
- Eureka Server: `8761`
- API Gateway: `8095`
- Account Service: `9016`
- Customer Service: `9017`
- Process Service: `9018`

## 🚀 Adım Adım Başlatma

### 1. PostgreSQL Başlatma

**PostgreSQL'i başlatın ve veritabanını oluşturun:**

```bash
# PostgreSQL'e bağlan
psql -U postgres

# Veritabanı oluştur
CREATE DATABASE bankdb;

# Çıkış
\q
```

**Alternatif (Docker):**
```bash
docker run --name bank-postgres -e POSTGRES_DB=bankdb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15-alpine
```

### 2. Redis Başlatma

**Redis'i başlatın:**

**Windows:**
```bash
# Redis'i indirin: https://github.com/microsoftarchive/redis/releases
# redis-server.exe'yi çalıştırın
```

**macOS:**
```bash
brew install redis
brew services start redis
```

**Linux:**
```bash
sudo systemctl start redis
sudo systemctl enable redis
```

**Alternatif (Docker):**
```bash
docker run --name bank-redis -p 6379:6379 -d redis:7-alpine
```

### 3. Kafka Başlatma

**Kafka'yu başlatın:**

**Zookeeper + Kafka (Docker Compose önerilir):**
```yaml
# docker-compose-kafka.yml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

```bash
docker-compose -f docker-compose-kafka.yml up -d
```

### 4. Servisleri Başlatma (Sırayla)

**Önemli:** Servisleri sırayla başlatın çünkü bağımlılıklar var.

**Başlatma Sırası:**
1. **Eureka Server** → Service Discovery (bağımlılığı yok)
2. **Config Server** → Configuration Server (Eureka'ya kayıt olabilir)
3. **API Gateway** → Gateway (Config Server ve Eureka'ya bağımlı)
4. **Microservices** → Account, Customer, Process (Config Server ve Eureka'ya bağımlı)

#### 4.1. Eureka Server

**Önce Eureka Server'ı başlatın** (diğer servislerin kayıt olabilmesi için):

```bash
cd eureka-server
mvn spring-boot:run
```

**Veya IDE'den:**
- `EurekaServerApplication.java` dosyasını çalıştırın
- Port: `8761`
- Dashboard: http://localhost:8761
- **Not:** Eureka başladıktan sonra birkaç saniye bekleyin

#### 4.2. Config Server

**Eureka'dan sonra Config Server'ı başlatın:**

```bash
cd config-server
mvn spring-boot:run
```

**Veya IDE'den:**
- `ConfigServerApplication.java` dosyasını çalıştırın
- Port: `8888`
- Health check: http://localhost:8888/actuator/health
- **Not:** Config Server, Eureka'ya kayıt olacak (Eureka'nın çalışıyor olması gerekir)

#### 4.3. API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

**Veya IDE'den:**
- `ApiGatewayApplication.java` dosyasını çalıştırın
- Port: `8095`
- Health check: http://localhost:8095/actuator/health

#### 4.4. Account Service

```bash
cd microservices/account-service
mvn spring-boot:run
```

**Veya IDE'den:**
- `AccountServiceApplication.java` dosyasını çalıştırın
- Port: `9016`
- Health check: http://localhost:9016/actuator/health

#### 4.5. Customer Service

```bash
cd microservices/customer-service
mvn spring-boot:run
```

**Veya IDE'den:**
- `CustomerServiceApplication.java` dosyasını çalıştırın
- Port: `9017`
- Health check: http://localhost:9017/actuator/health

#### 4.6. Process Service

```bash
cd microservices/process-service
mvn spring-boot:run
```

**Veya IDE'den:**
- `ProcessServiceApplication.java` dosyasını çalıştırın
- Port: `9018`
- Health check: http://localhost:9018/actuator/health

### 5. Tüm Servisleri Kontrol Etme

**Eureka Dashboard:** http://localhost:8761

Tüm servislerin registered olduğundan emin olun:
- `CONFIG-SERVER`
- `API-GATEWAY`
- `ACCOUNT-SERVICE`
- `CUSTOMER-SERVICE`
- `PROCESS-SERVICE`

## 📡 API İstekleri

### Customer Service

#### 1. Yeni Müşteri Ekleme

```bash
curl -X POST http://localhost:8095/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ahmet",
    "lastName": "Yılmaz",
    "email": "ahmet.yilmaz@example.com",
    "phoneNumber": "05551234567",
    "address": "İstanbul, Türkiye"
  }'
```

**Response:**
```json
{
  "id": 1,
  "firstName": "Ahmet",
  "lastName": "Yılmaz",
  "email": "ahmet.yilmaz@example.com",
  "phoneNumber": "05551234567",
  "address": "İstanbul, Türkiye",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### 2. Tüm Müşterileri Listeleme

```bash
curl http://localhost:8095/api/customers
```

#### 3. Müşteri Bilgisi Getirme

```bash
curl http://localhost:8095/api/customers/1
```

#### 4. Müşteri Güncelleme

```bash
curl -X PUT http://localhost:8095/api/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ahmet",
    "lastName": "Yılmaz",
    "email": "ahmet.yilmaz.updated@example.com",
    "phoneNumber": "05551234567",
    "address": "Ankara, Türkiye"
  }'
```

#### 5. Müşteri Silme

```bash
curl -X DELETE http://localhost:8095/api/customers/1
```

### Account Service

**Not:** Account Service endpoint'leri sadece hesap bakiyesini günceller. İşlem geçmişi kaydedilmez ve Kafka event gönderilmez. İşlem geçmişi için Process Service kullanın.

#### 1. Yeni Hesap Açma

```bash
curl -X POST http://localhost:8095/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "accountType": "CHECKING",
    "initialBalance": 1000.00
  }'
```

**Response:**
```json
{
  "accountNo": "ACC123456789",
  "customerId": 1,
  "accountType": "CHECKING",
  "balance": 1000.00,
  "createdAt": "2024-01-15T10:35:00"
}
```

#### 2. Tüm Hesapları Listeleme

```bash
curl http://localhost:8095/api/accounts
```

#### 3. Hesap Bilgisi Getirme

```bash
curl http://localhost:8095/api/accounts/ACC123456789
```

#### 4. Müşterinin Hesaplarını Listeleme

```bash
curl http://localhost:8095/api/accounts/customer/1
```

#### 5. Para Yatırma

```bash
curl -X POST http://localhost:8095/api/accounts/ACC123456789/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00,
    "explanation": "Para yatırma"
  }'
```

#### 6. Para Çekme

```bash
curl -X POST http://localhost:8095/api/accounts/ACC123456789/withdraw \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 200.00,
    "explanation": "Para çekme"
  }'
```

#### 7. Faiz İşleme (Vadeli Hesap)

```bash
curl -X POST http://localhost:8095/api/accounts/ACC123456789/interest
```

#### 8. Hesap Kapatma

```bash
curl -X DELETE http://localhost:8095/api/accounts/ACC123456789
```

### Process Service

**Not:** Process Service endpoint'leri:
- Hesap bakiyesini günceller (Account Service'i çağırarak)
- İşlem geçmişini kaydeder (Process entity)
- Kafka event gönderir (event-driven architecture)
- Transaction code oluşturur
- Önceki ve yeni bakiyeyi kaydeder
- Açıklama (explanation) bilgisini saklar ve response'ta döner

**Process Service vs Account Service:**
- **Account Service**: Sadece bakiye güncelleme (basit işlem)
- **Process Service**: Bakiye güncelleme + İşlem geçmişi + Event gönderme (tam işlem takibi)

#### 1. Para Yatırma İşlemi

```bash
curl -X POST http://localhost:8095/api/processes/deposit-money \
  -H "Content-Type: application/json" \
  -d '{
    "accountNo": "ACC123456789",
    "amount": 1000.00,
    "explanation": "Para yatırma işlemi"
  }'
```

#### 2. Para Çekme İşlemi

```bash
curl -X POST http://localhost:8095/api/processes/withdraw-money \
  -H "Content-Type: application/json" \
  -d '{
    "accountNo": "ACC123456789",
    "amount": 500.00,
    "explanation": "Para çekme işlemi"
  }'
```

#### 3. Hesap Bakiyesi Sorgulama

```bash
curl http://localhost:8095/api/processes/amount/ACC123456789
```

#### 4. Faiz Kazanma (Vadeli Hesap)

```bash
curl -X POST http://localhost:8095/api/processes/interest-earn/ACC123456789
```

#### 5. Hesap Geçmişi

```bash
curl http://localhost:8095/api/processes/account-history/ACC123456789
```

## 🔐 API Gateway Authentication

### 1. Giriş Yapma (Login)

Kullanıcı adı ve şifre ile giriş yaparak JWT token alın:

```bash
curl -X POST http://localhost:8095/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "expiresIn": 86400000
}
```

**Varsayılan Kullanıcılar:**
- Username: `admin`, Password: `admin123`
- Username: `user`, Password: `user123`

**Not:** Kullanıcılar config dosyasından (`app.auth.default-users`) tanımlanır. Database gerekmez.

### 2. Token ile İstek Yapma

Token'ı aldıktan sonra, tüm API isteklerinde `Authorization` header'ında kullanın:

```bash
# Token'ı değişkene kaydedin
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Token ile istek yapın
curl -X GET http://localhost:8095/api/accounts \
  -H "Authorization: Bearer $TOKEN"
```

### 2. Örnek: Login ve İstek Yapma

```bash
# 1. Login yap ve token al
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8095/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

# 2. Token'ı çıkar
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')

# 3. Token ile istek yap
curl -X GET http://localhost:8095/api/accounts \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Token Doğrulama

Token'ın geçerli olup olmadığını kontrol edin:

```bash
curl -X GET http://localhost:8095/api/auth/validate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. API Key ile İstek (Alternatif)

API Key desteği de mevcuttur (gelecekte implement edilecek):

```bash
curl -X GET http://localhost:8095/api/accounts \
  -H "X-API-Key: YOUR_API_KEY"
```

**Not:** Authentication aktif olduğunda, `/api/auth` ve `/actuator` endpoint'leri public'tir. Diğer tüm endpoint'ler için token gereklidir.

## 📊 Monitoring Endpoints

### Actuator Endpoints

**API Gateway:**
```bash
# Health check
curl http://localhost:8095/actuator/health

# Gateway routes
curl http://localhost:8095/actuator/gateway/routes

# Prometheus metrics
curl http://localhost:8095/actuator/prometheus
```

**Account Service:**
```bash
curl http://localhost:9016/actuator/health
curl http://localhost:9016/actuator/info
```

**Customer Service:**
```bash
curl http://localhost:9017/actuator/health
```

**Process Service:**
```bash
curl http://localhost:9018/actuator/health
```

## 🔍 Troubleshooting

### Servis Başlamıyor

1. **Eureka Server çalışıyor mu?** (ÖNCE Eureka başlatılmalı!)
   - Dashboard: http://localhost:8761
   - Servislerin kayıtlı olduğundan emin olun
   - Config Server ve diğer servisler Eureka'ya bağımlıdır

2. **Config Server çalışıyor mu?**
   ```bash
   curl http://localhost:8888/actuator/health
   ```
   - Config Server, Eureka'ya kayıt olmak için Eureka'nın çalışıyor olması gerekir
   - Config Server kendi `application.yml`'den config alır, başlatılabilir

3. **PostgreSQL bağlantısı:**
   ```bash
   psql -U postgres -d bankdb -c "SELECT version();"
   ```

4. **Redis bağlantısı:**
   ```bash
   redis-cli ping
   # Response: PONG
   ```

5. **Kafka bağlantısı:**
   ```bash
   # Kafka topic'lerini listeleyin
   kafka-topics.sh --bootstrap-server localhost:9092 --list
   ```

### Port Çakışması

Eğer port zaten kullanılıyorsa:
```bash
# Windows
netstat -ano | findstr :9016

# macOS/Linux
lsof -i :9016

# Process'i kill edin
```

### Log Kontrolü

Her servisin log'larını kontrol edin:
- Console output
- IDE console
- Log dosyaları (eğer yapılandırıldıysa)

## 📝 Örnek Test Senaryosu

### Tam Senaryo: Müşteri Oluştur → Hesap Aç → Para Yatır

```bash
# 1. Müşteri oluştur
CUSTOMER_RESPONSE=$(curl -s -X POST http://localhost:8095/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Mehmet",
    "lastName": "Demir",
    "email": "mehmet.demir@example.com",
    "phoneNumber": "05559876543",
    "address": "İzmir, Türkiye"
  }')

CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id')
echo "Müşteri ID: $CUSTOMER_ID"

# 2. Hesap aç
ACCOUNT_RESPONSE=$(curl -s -X POST http://localhost:8095/api/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"accountType\": \"CHECKING\",
    \"initialBalance\": 500.00
  }")

ACCOUNT_NO=$(echo $ACCOUNT_RESPONSE | jq -r '.accountNo')
echo "Hesap No: $ACCOUNT_NO"

# 3. Para yatır
curl -X POST http://localhost:8095/api/accounts/$ACCOUNT_NO/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "description": "İlk para yatırma"
  }'

# 4. Bakiye kontrolü
curl http://localhost:8095/api/processes/amount/$ACCOUNT_NO
```

## 🎯 Postman Collection

Postman kullanıyorsanız, environment değişkenleri:
- `base_url`: `http://localhost:8095`
- `api_key`: `YOUR_API_KEY` (opsiyonel)
- `jwt_token`: `YOUR_JWT_TOKEN` (opsiyonel)

## ✅ Başarı Kontrolü

Tüm servislerin çalıştığından emin olun:
1. ✅ Eureka Dashboard: http://localhost:8761 (5 servis görünmeli: CONFIG-SERVER, API-GATEWAY, ACCOUNT-SERVICE, CUSTOMER-SERVICE, PROCESS-SERVICE)
2. ✅ Config Server: http://localhost:8888/actuator/health
3. ✅ API Gateway: http://localhost:8095/actuator/health
4. ✅ Test isteği: `curl http://localhost:8095/api/customers`

**Önemli:** Tüm servislerin Eureka'da "UP" durumunda olduğundan emin olun.

Sistem hazır! 🚀

