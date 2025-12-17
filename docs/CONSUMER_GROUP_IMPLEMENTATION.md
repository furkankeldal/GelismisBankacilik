# Kafka Consumer Group Yönetimi - Uygulama Detayları

## ✅ Uygulanan İyileştirmeler

### 1. Consumer Configuration İyileştirmeleri

**Dosya:** `microservices/account-service/src/main/resources/application.yml`

```yaml
spring:
  kafka:
    consumer:
      group-id: notification-group
      # Consumer Group Yönetimi - Performans ve Güvenilirlik
      max-poll-records: 500  # Her poll'da maksimum mesaj sayısı (batch processing)
      max-poll-interval-ms: 300000  # 5 dakika - Mesaj işleme süresi limiti
      session-timeout-ms: 30000  # 30 saniye - Consumer çöktüğünde tespit süresi
      heartbeat-interval-ms: 10000  # 10 saniye - Consumer'ın aktif olduğunu gösterir
      fetch-min-size: 1  # Minimum fetch size (bytes)
      fetch-max-wait: 500  # Maximum fetch wait time (ms)
      auto-commit-interval-ms: 5000  # 5 saniyede bir offset commit
      partition-assignment-strategy: org.apache.kafka.clients.consumer.RangeAssignor
```

**Açıklamalar:**
- **max-poll-records: 500**: Her poll işleminde maksimum 500 mesaj alınır (batch processing için)
- **max-poll-interval-ms: 300000**: Mesaj işleme süresi 5 dakikayı geçerse consumer group'tan çıkarılır
- **session-timeout-ms: 30000**: Consumer 30 saniye heartbeat göndermezse çökmüş kabul edilir
- **heartbeat-interval-ms: 10000**: Her 10 saniyede bir heartbeat gönderilir
- **auto-commit-interval-ms: 5000**: Her 5 saniyede bir offset commit edilir

### 2. Consumer Group ID Yönetimi

**Dosya:** `microservices/account-service/src/main/resources/application.yml`

```yaml
app:
  kafka:
    consumer-groups:
      notification-group: notification-group  # Transaction event'leri için
      dlq-handler-group: dlq-handler-group  # DLQ mesajları için
```

**Avantajları:**
- Consumer Group ID'leri merkezi olarak yönetilir
- Farklı environment'larda (dev, test, prod) farklı group ID'ler kullanılabilir
- Hardcoded değerler yerine configuration'dan alınır

### 3. Consumer Kod İyileştirmeleri

**Dosya:** `microservices/account-service/src/main/java/.../TransactionNotificationConsumer.java`

```java
@Value("${app.kafka.consumer-groups.notification-group:notification-group}")
private String notificationGroupId;

@Value("${app.kafka.consumer-groups.dlq-handler-group:dlq-handler-group}")
private String dlqHandlerGroupId;

@KafkaListener(
    topics = "${app.kafka.transaction-topic:transaction-events}", 
    groupId = "${app.kafka.consumer-groups.notification-group:notification-group}"
)
public void consume(String message) {
    // Consumer Group yönetimi ile mesajlar partition'lara göre dağıtılır
}
```

**Avantajları:**
- Consumer Group ID'leri configuration'dan alınır
- Kod daha esnek ve yönetilebilir hale gelir
- Environment-specific group ID'ler kullanılabilir

### 4. Monitoring ve Observability

**Dosya:** `microservices/account-service/src/main/resources/application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,kafka
  metrics:
    export:
      prometheus:
        enabled: true
```

**Avantajları:**
- Kafka consumer group durumu `/actuator/kafka` endpoint'inden izlenebilir
- Prometheus metrics ile consumer lag, offset, partition assignment gibi metrikler takip edilebilir

## 📊 Consumer Group Yönetimi Nasıl Çalışıyor?

### Senaryo 1: Normal İşleyiş

```
Topic: transaction-events (3 partition)
Consumer Group: notification-group

Instance 1 (account-service-1) → Partition 0 okuyor
Instance 2 (account-service-2) → Partition 1 okuyor
Instance 3 (account-service-3) → Partition 2 okuyor

Her instance kendi partition'ından mesajları okur ve işler.
```

### Senaryo 2: Instance Çökmesi (Fault Tolerance)

```
Başlangıç:
Instance 1 → Partition 0
Instance 2 → Partition 1
Instance 3 → Partition 2

Instance 2 çöküyor (30 saniye heartbeat yok):
Kafka otomatik olarak:
- Instance 2'yi consumer group'tan çıkarır
- Partition 1'i Instance 1 veya 3'e atar

Sonuç:
Instance 1 → Partition 0 + Partition 1
Instance 3 → Partition 2

Hiçbir mesaj kaybolmaz, işlem devam eder.
```

### Senaryo 3: Yeni Instance Ekleme (Scalability)

```
Başlangıç:
Instance 1 → Partition 0
Instance 2 → Partition 1
Instance 3 → Partition 2

Yeni Instance 4 eklendi:
Kafka otomatik olarak rebalance yapar:
Instance 1 → Partition 0
Instance 2 → Partition 1
Instance 3 → Partition 2
Instance 4 → (Beklemede, yeni partition eklenirse atanır)

Veya partition sayısı artırılırsa:
Instance 1 → Partition 0
Instance 2 → Partition 1
Instance 3 → Partition 2
Instance 4 → Partition 3 (yeni)
```

## 🔍 Monitoring ve Debugging

### 1. Consumer Group Durumu Kontrolü

```bash
# Kafka consumer group durumunu kontrol et
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group notification-group --describe

# Consumer lag kontrolü
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group notification-group --describe | grep LAG
```

### 2. Actuator Endpoints

```bash
# Health check
curl http://localhost:9016/actuator/health

# Kafka consumer group bilgileri
curl http://localhost:9016/actuator/kafka

# Metrics
curl http://localhost:9016/actuator/metrics
```

### 3. Log Monitoring

Consumer Group yönetimi ile ilgili loglar:

```
[INFO] Consumer group rebalancing started
[INFO] Partition assignment: Partition 0 → Instance 1
[INFO] Consumer group rebalancing completed
[WARN] Consumer heartbeat timeout - removing from group
```

## ⚙️ Configuration Best Practices

### Production Ortamı İçin Öneriler:

```yaml
spring:
  kafka:
    consumer:
      # Daha agresif timeout'lar (hızlı failover)
      session-timeout-ms: 10000  # 10 saniye
      heartbeat-interval-ms: 3000  # 3 saniye
      
      # Daha büyük batch size (yüksek throughput)
      max-poll-records: 1000
      
      # Daha uzun işleme süresi (karmaşık işlemler için)
      max-poll-interval-ms: 600000  # 10 dakika
      
      # Manual offset commit (daha güvenli)
      enable-auto-commit: false
```

### Development Ortamı İçin:

```yaml
spring:
  kafka:
    consumer:
      # Daha toleranslı timeout'lar
      session-timeout-ms: 30000  # 30 saniye
      heartbeat-interval-ms: 10000  # 10 saniye
      
      # Küçük batch size (hızlı test)
      max-poll-records: 100
      
      # Auto commit (kolay test)
      enable-auto-commit: true
```

## 🎯 Sonuç

Consumer Group yönetimi başarıyla uygulandı:

✅ **Performans**: Batch processing ve parallel işleme  
✅ **Güvenilirlik**: Fault tolerance ve otomatik recovery  
✅ **Ölçeklenebilirlik**: Yeni instance'lar kolayca eklenebilir  
✅ **Monitoring**: Actuator endpoints ve metrics  
✅ **Yönetilebilirlik**: Configuration-based group ID yönetimi  

Sistem artık production-ready bir Consumer Group yönetimine sahip!


