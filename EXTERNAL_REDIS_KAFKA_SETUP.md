# External Redis ve Kafka Kullanımı

Eğer Redis ve Kafka'yı zaten Docker'da çalıştırıyorsanız, bu dokümantasyon size mevcut container'larınızı nasıl kullanacağınızı gösterir.

## Seçenek 1: Mevcut Container'ları Aynı Network'e Bağlama (Önerilen)

Bu yöntem, mevcut Redis ve Kafka container'larınızı `banking-network`'e bağlar. Bu sayede container'lar birbirleriyle iletişim kurabilir.

### Adımlar

1. **Mevcut container'larınızın adlarını öğrenin:**
   ```bash
   docker ps
   ```

2. **banking-network'i oluşturun (docker-compose up sırasında otomatik oluşturulur):**
   ```bash
   docker network create banking-network
   ```

3. **Mevcut Redis container'ınızı network'e bağlayın:**
   ```bash
   docker network connect banking-network <redis-container-name>
   ```

4. **Mevcut Kafka container'ınızı network'e bağlayın:**
   ```bash
   docker network connect banking-network <kafka-container-name>
   ```

5. **docker-compose.yml dosyasını güncelleyin:**
   - Redis ve Kafka servislerini yorum satırı yapın (zaten yapıldı)
   - Environment variable'larda container adlarını kullanın:
     ```yaml
     SPRING_REDIS_HOST: redis  # veya mevcut container adınız
     SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092  # veya mevcut container adınız
     ```

6. **Docker Compose'u başlatın:**
   ```bash
   docker compose up -d
   ```

## Seçenek 2: host.docker.internal Kullanma (Windows/Mac)

Eğer container'ları farklı bir network'te çalıştırıyorsanız, `host.docker.internal` kullanabilirsiniz.

### Adımlar

1. **docker-compose.yml dosyasında environment variable'ları güncelleyin:**
   ```yaml
   SPRING_REDIS_HOST: host.docker.internal
   SPRING_REDIS_PORT: 6379
   SPRING_KAFKA_BOOTSTRAP_SERVERS: host.docker.internal:9092
   ```

2. **Docker Compose'u başlatın:**
   ```bash
   docker compose up -d
   ```

**Not:** Linux'ta `host.docker.internal` çalışmaz. Linux için `--add-host=host.docker.internal:host-gateway` ekleyin veya Seçenek 1'i kullanın.

## Seçenek 3: Environment Variable ile Yapılandırma

Environment variable'ları kullanarak Redis ve Kafka host'larını belirtebilirsiniz:

```bash
# Windows PowerShell
$env:REDIS_HOST="redis"
$env:REDIS_PORT="6379"
$env:KAFKA_BOOTSTRAP_SERVERS="kafka:9092"
docker compose up -d

# Linux/Mac
export REDIS_HOST=redis
export REDIS_PORT=6379
export KAFKA_BOOTSTRAP_SERVERS=kafka:9092
docker compose up -d
```

## Mevcut Container'ları Kontrol Etme

Mevcut container'larınızın durumunu kontrol etmek için:

```bash
# Tüm container'ları listele
docker ps

# Redis container'ını kontrol et
docker exec <redis-container-name> redis-cli ping

# Kafka container'ını kontrol et
docker exec <kafka-container-name> kafka-broker-api-versions --bootstrap-server localhost:9092
```

## Network Bağlantısını Kontrol Etme

Container'ların network'e bağlı olup olmadığını kontrol etmek için:

```bash
# Network'ü incele
docker network inspect banking-network

# Container'ın hangi network'lerde olduğunu gör
docker inspect <container-name> | grep Networks
```

## Sorun Giderme

### Container'lar Birbirini Bulamıyor

1. **Network bağlantısını kontrol edin:**
   ```bash
   docker network inspect banking-network
   ```

2. **Container adlarını doğrulayın:**
   - Environment variable'larda kullandığınız container adlarının doğru olduğundan emin olun

3. **Port çakışması:**
   - Mevcut container'larınızın portlarının docker-compose.yml'deki portlarla çakışmadığından emin olun

### Redis Bağlantı Hatası

```bash
# Redis container'ının çalıştığını kontrol edin
docker ps | grep redis

# Redis'e bağlanmayı test edin
docker exec <redis-container-name> redis-cli ping
```

### Kafka Bağlantı Hatası

```bash
# Kafka container'ının çalıştığını kontrol edin
docker ps | grep kafka

# Kafka'ya bağlanmayı test edin
docker exec <kafka-container-name> kafka-broker-api-versions --bootstrap-server localhost:9092
```

## Örnek: Mevcut Container Adları

Eğer mevcut container'larınızın adları farklıysa (örneğin `my-redis`, `my-kafka`), environment variable'ları şu şekilde güncelleyin:

```yaml
SPRING_REDIS_HOST: my-redis
SPRING_KAFKA_BOOTSTRAP_SERVERS: my-kafka:9092
```

## Önerilen Yapılandırma

En iyi performans için:

1. **Aynı Network'te Çalıştırın**: Mevcut container'larınızı `banking-network`'e bağlayın
2. **Container Adlarını Kullanın**: IP adresi yerine container adlarını kullanın
3. **Health Check'leri Kaldırın**: External servisler için health check dependency'lerini kaldırın (zaten yapıldı)

## Daha Fazla Bilgi

- [Docker Networking](https://docs.docker.com/network/)
- [Docker Compose Networking](https://docs.docker.com/compose/networking/)

