# Resilience & Fault Tolerance

Bu dokümantasyon, projede kullanılan Resilience4j ile Circuit Breaker, Retry, Timeout ve Fallback mekanizmalarını açıklar.

## Genel Bakış

Projede Resilience4j kullanılarak microservices arası iletişimde hata toleransı sağlanmaktadır. Bu sayede bir servis down olduğunda veya yavaş yanıt verdiğinde sistemin tamamen çökmesi önlenir.

## Kullanılan Servisler

- **Account Service**: Customer Service'e Feign Client ile bağlanır
- **Process Service**: Account Service'e Feign Client ile bağlanır

## Yapılandırma

### Process Service - Account Service İletişimi

**Dosya**: `config-repo/process-service.yml`

```yaml
resilience4j:
  circuitbreaker:
    instances:
      accountService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
  retry:
    instances:
      accountService:
        maxAttempts: 3
        waitDuration: 1000
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
  timelimiter:
    instances:
      accountService:
        timeoutDuration: 5s
        cancelRunningFuture: true

feign:
  client:
    config:
      account-service:
        connectTimeout: 2000
        readTimeout: 5000
  circuitbreaker:
    enabled: true
  resilience4j:
    enabled: true
```

### Account Service - Customer Service İletişimi

**Dosya**: `config-repo/account-service.yml`

```yaml
resilience4j:
  circuitbreaker:
    instances:
      customerService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
  retry:
    instances:
      customerService:
        maxAttempts: 3
        waitDuration: 1000
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
  timelimiter:
    instances:
      customerService:
        timeoutDuration: 5s
        cancelRunningFuture: true

feign:
  client:
    config:
      customer-service:
        connectTimeout: 2000
        readTimeout: 5000
  circuitbreaker:
    enabled: true
  resilience4j:
    enabled: true
```

## Circuit Breaker

Circuit Breaker, bir servisin başarısız olduğunda otomatik olarak devreye girer ve gereksiz istekleri engeller.

### Durumlar

1. **CLOSED**: Normal çalışma durumu, tüm istekler geçer
2. **OPEN**: Hata eşiği aşıldığında, istekler doğrudan fallback'e yönlendirilir
3. **HALF_OPEN**: Belirli bir süre sonra test istekleri gönderilir

### Parametreler

- **slidingWindowSize**: Hata oranını hesaplamak için kullanılan pencere boyutu (10)
- **minimumNumberOfCalls**: Circuit breaker'ın açılması için minimum çağrı sayısı (5)
- **failureRateThreshold**: Circuit breaker'ın açılması için hata oranı eşiği (%50)
- **waitDurationInOpenState**: OPEN durumunda kalma süresi (10 saniye)
- **permittedNumberOfCallsInHalfOpenState**: HALF_OPEN durumunda izin verilen çağrı sayısı (3)

## Retry

Başarısız istekler otomatik olarak yeniden denenir.

### Parametreler

- **maxAttempts**: Maksimum deneme sayısı (3)
- **waitDuration**: İlk deneme arası bekleme süresi (1000ms)
- **enableExponentialBackoff**: Üstel geri çekilme aktif (true)
- **exponentialBackoffMultiplier**: Üstel geri çekilme çarpanı (2)

### Örnek

1. İlk deneme: 0ms
2. İkinci deneme: 1000ms sonra
3. Üçüncü deneme: 2000ms sonra (1000 * 2)

## Timeout

İstekler belirli bir süre içinde yanıt vermezse iptal edilir.

### Parametreler

- **timeoutDuration**: Timeout süresi (5 saniye)
- **cancelRunningFuture**: Çalışan future'ı iptal et (true)

## Fallback

Servis down olduğunda veya hata durumunda fallback metodları çağrılır.

### Fallback Sınıfları

1. **AccountServiceClientFallback**: Process Service'de Account Service için
2. **CustomerServiceClientFallback**: Account Service'de Customer Service için

### Örnek Fallback

```java
@Component
public class AccountServiceClientFallback implements AccountServiceClient {
    @Override
    public AccountResponseDto getAccount(String accountNo) {
        log.error("Account Service fallback triggered for accountNo: {}", accountNo);
        throw new AccountNotFoundException("Account Service geçici olarak kullanılamıyor.");
    }
}
```

## Monitoring

Circuit Breaker durumunu ve metriklerini izlemek için:

- **Actuator Endpoints**: `/actuator/health` ve `/actuator/metrics`
- **Circuit Breaker Events**: Resilience4j event'leri loglanır

## Test Senaryoları

1. **Account Service Down**: Process Service, Account Service'e istek atamazsa fallback devreye girer
2. **Yavaş Yanıt**: Timeout süresi aşılırsa istek iptal edilir ve retry mekanizması devreye girer
3. **Yüksek Hata Oranı**: %50'den fazla hata olursa Circuit Breaker OPEN durumuna geçer

## Best Practices

1. **Fallback Stratejisi**: Fallback metodları kullanıcıya anlamlı hata mesajları döndürmelidir
2. **Timeout Değerleri**: Timeout değerleri servislerin normal yanıt sürelerine göre ayarlanmalıdır
3. **Retry Stratejisi**: Kritik olmayan işlemler için retry kullanılmalı, idempotent olmayan işlemler için dikkatli olunmalıdır
4. **Circuit Breaker Threshold**: Hata eşiği, servislerin normal hata oranlarına göre ayarlanmalıdır
