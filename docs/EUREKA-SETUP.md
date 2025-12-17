# Eureka Server Kurulum Rehberi

## 🎯 Eureka Server Nedir?

Eureka Server, Spring Cloud'un **Service Discovery** (Servis Keşfi) bileşenidir. Microservice'lerin birbirlerini bulmasını ve iletişim kurmasını sağlar.

## 📦 Oluşturulan Yapı

✅ **Eureka Server** - Port 8761
- Tüm microservice'lerin kayıt olduğu merkezi servis
- Service discovery ve load balancing sağlar

✅ **Eureka Client** - Account Service ve API Gateway
- Eureka Server'a kayıt olur
- Diğer servisleri keşfeder

## 🚀 Servisleri Başlatma Sırası

### 1. Config Server (Port: 8888)
```bash
cd config-server
mvn spring-boot:run
```

### 2. Eureka Server (Port: 8761) ⭐ YENİ
```bash
cd eureka-server
mvn spring-boot:run
```

### 3. Account Service (Port: 9016)
```bash
cd account-service
mvn spring-boot:run
```

### 4. API Gateway (Port: 8080)
```bash
cd api-gateway
mvn spring-boot:run
```

## ✅ Eureka Dashboard

Eureka Server başladıktan sonra dashboard'a erişin:

**URL:** http://localhost:8761

Dashboard'da şunları görebilirsiniz:
- Kayıtlı tüm servisler
- Servis durumları (UP/DOWN)
- Instance bilgileri
- Metadata

## 🔍 Servis Kayıt Kontrolü

### Eureka Dashboard'dan:
1. http://localhost:8761 adresine gidin
2. **"Instances currently registered with Eureka"** bölümünde servisleri görün:
   - `ACCOUNT-SERVICE`
   - `API-GATEWAY`

### API ile Kontrol:
```bash
# Tüm kayıtlı servisler
curl http://localhost:8761/eureka/apps

# Account Service bilgisi
curl http://localhost:8761/eureka/apps/ACCOUNT-SERVICE

# API Gateway bilgisi
curl http://localhost:8761/eureka/apps/API-GATEWAY
```

## ⚙️ Yapılandırma

### Eureka Server (`eureka-server/src/main/resources/application.yml`)
```yaml
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false  # Kendini kaydetmez
    fetch-registry: false         # Registry'yi fetch etmez
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Eureka Client (Account Service, API Gateway)
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

## 🔗 Service Discovery Kullanımı

### Feign Client ile:
```java
@FeignClient(name = "account-service")  // Eureka'dan otomatik bulur
public interface AccountServiceClient {
    @GetMapping("/accounts/{id}")
    AccountResponseDto getAccount(@PathVariable String id);
}
```

### Gateway Route ile:
```yaml
routes:
  - id: account-service
    uri: lb://account-service  # Eureka'dan otomatik bulur
    predicates:
      - Path=/api/accounts/**
```

## 🛠️ Sorun Giderme

### Servis Eureka'ya Kayıt Olmuyor
- ✅ Eureka Server'ın çalıştığından emin olun
- ✅ `defaultZone` URL'ini kontrol edin
- ✅ Network bağlantısını kontrol edin
- ✅ Log dosyalarını kontrol edin

### "Connection refused" Hatası
- ✅ Eureka Server'ın 8761 portunda çalıştığını kontrol edin
- ✅ Firewall ayarlarını kontrol edin

### Servis Dashboard'da Görünmüyor
- ✅ Servisin başladığından emin olun
- ✅ Birkaç saniye bekleyin (kayıt işlemi zaman alabilir)
- ✅ Log dosyalarında hata var mı kontrol edin

## 📊 Health Check

Eureka, servislerin health check'lerini otomatik yapar:

```bash
# Account Service health
curl http://localhost:9016/actuator/health

# API Gateway health
curl http://localhost:8080/actuator/health
```

## 🎯 Avantajlar

✅ **Otomatik Service Discovery:** Servisler birbirlerini otomatik bulur  
✅ **Load Balancing:** Eureka ile otomatik load balancing  
✅ **Health Monitoring:** Servis durumlarını izler  
✅ **Dynamic Scaling:** Yeni instance'lar otomatik keşfedilir  
✅ **Resilience:** Servis çökerse otomatik olarak registry'den çıkarılır  

## 📝 Notlar

⚠️ **Eureka Server:** İlk başlatılması gereken servis (Config Server'dan sonra)  
⚠️ **Client Registration:** Servisler başladıktan sonra Eureka'ya kayıt olması 30-60 saniye sürebilir  
⚠️ **Heartbeat:** Servisler her 30 saniyede bir heartbeat gönderir  
✅ **Production:** Production ortamında Eureka Server'ı cluster modunda çalıştırın  

## 🎉 Tamamlandı!

Artık microservice'leriniz Eureka Server üzerinden birbirlerini keşfedebilir ve iletişim kurabilir! 🚀


