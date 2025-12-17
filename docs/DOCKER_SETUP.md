# Docker Setup Guide

Bu dokümantasyon, projeyi Docker ve Docker Compose kullanarak nasıl çalıştıracağınızı açıklar.

## Gereksinimler

- Docker Desktop veya Docker Engine
- Docker Compose v2.0+

## Hızlı Başlangıç

### Tüm Servisleri Başlatma

```bash
docker compose up -d
```

Bu komut şunları başlatır:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Kafka + Zookeeper (port 9092)
- Config Server (port 8888)
- Eureka Server (port 8761)
- Customer Service (port 9017)
- Account Service (port 9016)
- Process Service (port 9018)
- API Gateway (port 8095)

### Servisleri Durdurma

```bash
docker compose down
```

### Servisleri Durdurma ve Volume'ları Silme

```bash
docker compose down -v
```

## Servis Durumunu Kontrol Etme

```bash
# Tüm servislerin durumunu görüntüle
docker compose ps

# Belirli bir servisin loglarını görüntüle
docker compose logs -f api-gateway

# Tüm servislerin loglarını görüntüle
docker compose logs -f
```

## Health Check

Servislerin sağlık durumunu kontrol etmek için:

```bash
# Eureka Server
curl http://localhost:8761/actuator/health

# Config Server
curl http://localhost:8888/actuator/health

# Customer Service
curl http://localhost:9017/actuator/health

# Account Service
curl http://localhost:9016/actuator/health

# Process Service
curl http://localhost:9018/actuator/health

# API Gateway
curl http://localhost:8095/actuator/health
```

## Dockerfile'lar

Her servis için ayrı Dockerfile mevcuttur:

- `api-gateway/Dockerfile`
- `config-server/Dockerfile`
- `eureka-server/Dockerfile`
- `microservices/customer-service/Dockerfile`
- `microservices/account-service/Dockerfile`
- `microservices/process-service/Dockerfile`

### Dockerfile Yapısı

Tüm Dockerfile'lar multi-stage build kullanır:

1. **Build Stage**: Maven ile uygulama derlenir
2. **Runtime Stage**: Sadece JAR dosyası ve JRE içeren minimal image

## Docker Compose Yapılandırması

### Servis Bağımlılıkları

Servisler aşağıdaki sırayla başlatılır:

1. **PostgreSQL** → Veritabanı
2. **Redis** → Cache
3. **Zookeeper** → Kafka için
4. **Kafka** → Mesajlaşma
5. **Config Server** → Yapılandırma yönetimi
6. **Eureka Server** → Service Discovery
7. **Customer Service** → Müşteri yönetimi
8. **Account Service** → Hesap yönetimi
9. **Process Service** → İşlem yönetimi
10. **API Gateway** → API yönlendirme

### Environment Variables

Docker Compose dosyasında aşağıdaki environment variable'lar kullanılır:

- `SPRING_DATASOURCE_URL`: PostgreSQL bağlantı URL'i
- `SPRING_DATASOURCE_USERNAME`: PostgreSQL kullanıcı adı
- `SPRING_DATASOURCE_PASSWORD`: PostgreSQL şifresi
- `SPRING_REDIS_HOST`: Redis host adresi
- `SPRING_REDIS_PORT`: Redis port numarası
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka broker adresi
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka Server URL'i
- `SPRING_CONFIG_URI`: Config Server URL'i

## Volume'lar

Docker Compose aşağıdaki volume'ları oluşturur:

- `postgres_data`: PostgreSQL verileri
- `redis_data`: Redis verileri

## Network

Tüm servisler `banking-network` adlı bir bridge network'te çalışır.

## Troubleshooting

### Servisler Başlamıyor

1. Port çakışmalarını kontrol edin:
   ```bash
   netstat -an | grep -E "5432|6379|9092|8888|8761|9017|9016|9018|8095"
   ```

2. Docker loglarını kontrol edin:
   ```bash
   docker compose logs [service-name]
   ```

### Database Bağlantı Hatası

1. PostgreSQL'in hazır olduğundan emin olun:
   ```bash
   docker compose ps postgres
   ```

2. Health check'i kontrol edin:
   ```bash
   docker compose exec postgres pg_isready -U postgres
   ```

### Config Server Bağlantı Hatası

1. Config Server'ın çalıştığından emin olun:
   ```bash
   curl http://localhost:8888/actuator/health
   ```

2. Config repo'nun doğru mount edildiğinden emin olun

### Eureka Service Discovery Hatası

1. Eureka Server'ın çalıştığından emin olun:
   ```bash
   curl http://localhost:8761/actuator/health
   ```

2. Eureka Dashboard'u kontrol edin:
   ```
   http://localhost:8761
   ```

## Production Kullanımı

Production ortamında kullanmak için:

1. **Environment Variables**: Hassas bilgileri environment variable olarak geçirin
2. **Secrets Management**: Şifreler için Docker Secrets veya external secret management kullanın
3. **Resource Limits**: Her servis için CPU ve memory limitleri belirleyin
4. **Health Checks**: Health check'leri production gereksinimlerine göre ayarlayın
5. **Logging**: Centralized logging (ELK, Loki, vb.) kullanın
6. **Monitoring**: Prometheus, Grafana gibi monitoring araçları ekleyin

## Örnek Production docker-compose.yml

```yaml
services:
  api-gateway:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    restart: unless-stopped
```

## Daha Fazla Bilgi

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)


