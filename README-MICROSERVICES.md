# Banking Microservices Architecture

Bu proje, Spring Cloud kullanılarak oluşturulmuş bir microservice mimarisidir.

## Mimari Yapı

### 1. Config Server (Port: 8888)
- Spring Cloud Config Server
- Git repository'den konfigürasyon yönetimi
- Tüm servislerin merkezi konfigürasyon yönetimi

### 2. API Gateway (Port: 8080)
- Spring Cloud Gateway
- Tüm servislere tek noktadan erişim
- Route yönetimi ve load balancing

### 3. Account Service (Port: 9016)
- Hesap yönetimi servisi
- Feign Client ile diğer servislerle iletişim
- Kafka entegrasyonu
- Redis cache desteği

## Kurulum

### Gereksinimler
- Java 17
- Maven 3.6+
- Git
- Redis (opsiyonel)
- Kafka (opsiyonel)
- H2 Database (in-memory)

### Adımlar

1. **Config Repository Oluşturma**
   ```bash
   # Yeni bir Git repository oluşturun
   git init banking-config-repo
   cd banking-config-repo
   
   # Config dosyalarını kopyalayın
   cp -r ../config-repo/* .
   git add .
   git commit -m "Initial config"
   git remote add origin <your-git-repo-url>
   git push -u origin main
   ```

2. **Config Server'ı Güncelleme**
   - `config-server/src/main/resources/application.yml` dosyasında Git repository URL'ini güncelleyin:
   ```yaml
   spring:
     cloud:
       config:
         server:
           git:
             uri: https://github.com/your-username/banking-config-repo.git
   ```

3. **Servisleri Başlatma Sırası**
   ```bash
   # 1. Config Server
   cd config-server
   mvn spring-boot:run
   
   # 2. Account Service
   cd ../account-service
   mvn spring-boot:run
   
   # 3. API Gateway
   cd ../api-gateway
   mvn spring-boot:run
   ```

## API Endpoints

### Account Service
- `GET /api/accounts` - Tüm hesapları listele
- `GET /api/accounts/{accountNo}` - Hesap detayı
- `POST /api/accounts` - Yeni hesap oluştur
- `DELETE /api/accounts/{accountNo}` - Hesap kapat

### Gateway üzerinden erişim
Tüm istekler Gateway üzerinden yapılır:
- `http://localhost:8080/api/accounts/**`

## Feign Client Kullanımı

Account Service içinde Customer Service'e erişim için Feign Client kullanılır:

```java
@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    @GetMapping("/customers/{id}")
    CustomerResponseDto getCustomerById(@PathVariable("id") Long id);
}
```

## Konfigürasyon Yönetimi

Tüm servislerin konfigürasyonları Git repository'de tutulur:
- `config-repo/account-service.yml` - Account Service konfigürasyonu
- `config-repo/api-gateway.yml` - Gateway konfigürasyonu
- `config-repo/application.yml` - Global konfigürasyon

## Notlar

- Eureka Server eklenmesi önerilir (service discovery için)
- Production ortamında H2 yerine PostgreSQL/MySQL kullanılmalıdır
- Redis ve Kafka production için yapılandırılmalıdır

