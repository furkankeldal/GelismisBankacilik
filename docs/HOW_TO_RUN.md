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

<!-- Devamında diğer servis endpoint’leri vb. README'deki orijinal HOW_TO_RUN içeriği gibi devam eder -->


