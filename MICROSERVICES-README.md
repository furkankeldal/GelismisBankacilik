# Banking Microservices - Hızlı Başlangıç

## Oluşturulan Yapı

✅ **Config Server** - Port 8888
- Git repository'den merkezi konfigürasyon yönetimi
- Tüm servislerin konfigürasyonlarını yönetir

✅ **API Gateway** - Port 8080  
- Spring Cloud Gateway
- Tüm servislere tek noktadan erişim
- Route yönetimi ve load balancing

✅ **Account Service** - Port 9016
- Mevcut hesap yönetimi servisi
- Feign Client ile diğer servislerle iletişim
- Kafka ve Redis entegrasyonu

## Hızlı Kurulum

### 1. Mevcut Kodları Account Service'e Kopyalama

**Windows:**
```powershell
.\copy-to-account-service.ps1
```

**Linux/Mac:**
```bash
chmod +x copy-to-account-service.sh
./copy-to-account-service.sh
```

### 2. Config Repository Oluşturma

```bash
# Yeni Git repository oluşturun (GitHub, GitLab, vb.)
# Sonra config-repo klasöründeki dosyaları oraya yükleyin
```

### 3. Config Server Yapılandırması

`config-server/src/main/resources/application.yml` dosyasında Git URL'ini güncelleyin:
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-username/banking-config-repo.git
```

### 4. Servisleri Başlatma

```bash
# 1. Config Server
cd config-server
mvn spring-boot:run

# 2. Account Service (yeni terminal)
cd account-service  
mvn spring-boot:run

# 3. API Gateway (yeni terminal)
cd api-gateway
mvn spring-boot:run
```

## API Kullanımı

### Gateway Üzerinden:
```bash
# Tüm hesapları listele
curl http://localhost:8080/api/accounts

# Hesap detayı
curl http://localhost:8080/api/accounts/{accountNo}
```

### Doğrudan Account Service:
```bash
curl http://localhost:9016/accounts
```

## Feign Client

Account Service içinde `CustomerServiceClient` kullanarak diğer servislere erişim sağlanır:

```java
@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    @GetMapping("/customers/{id}")
    CustomerResponseDto getCustomerById(@PathVariable("id") Long id);
}
```

## Dosya Yapısı

```
banking-microservices/
├── pom-parent.xml              # Parent POM
├── config-server/             # Config Server microservice
├── api-gateway/               # API Gateway microservice
├── account-service/           # Account Service microservice
├── config-repo/               # Config dosyaları (Git'e yüklenecek)
├── SETUP-MICROSERVICES.md     # Detaylı kurulum rehberi
└── README-MICROSERVICES.md    # Genel dokümantasyon
```

## Önemli Notlar

⚠️ **Eureka Server:** Service discovery için Eureka Server eklenmesi önerilir (şu an opsiyonel)

⚠️ **Git Repository:** Config Server için ayrı bir Git repository oluşturmanız gerekiyor

⚠️ **Database:** Production'da H2 yerine PostgreSQL/MySQL kullanın

⚠️ **Redis & Kafka:** Servislerin çalıştığından emin olun

## Sonraki Adımlar

1. Eureka Server ekleyin (service discovery için)
2. Customer Service ve Process Service'i ayrı microservice'ler olarak oluşturun
3. Docker containerization ekleyin
4. CI/CD pipeline kurun

