# Gelişmiş Bankacılık Sistemi - Microservices Architecture

Bu proje, Spring Cloud kullanılarak geliştirilmiş bir microservices bankacılık uygulamasıdır.

## 🚀 Özellikler

- **Microservices Architecture**: Spring Cloud ile dağıtık mimari
- **Service Discovery**: Eureka Server ile servis keşfi
- **API Gateway**: Spring Cloud Gateway ile merkezi API yönetimi
- **Configuration Management**: Spring Cloud Config Server ile merkezi yapılandırma
- **Resilience & Fault Tolerance**: Resilience4j ile Circuit Breaker, Retry, Timeout ve Fallback
- **API Documentation**: Swagger/OpenAPI ile otomatik API dokümantasyonu
- **Docker Support**: Docker ve Docker Compose ile containerization
- **CI/CD Pipeline**: GitHub Actions ile otomatik build, test ve deployment
- **Caching**: Redis ile performans optimizasyonu
- **Message Queue**: Kafka ile asenkron mesajlaşma
- **Authentication**: JWT token tabanlı kimlik doğrulama

## 📋 Servisler

- **API Gateway** (Port 8095): Tüm API isteklerinin giriş noktası
- **Customer Service** (Port 9017): Müşteri yönetimi
- **Account Service** (Port 9016): Hesap yönetimi
- **Process Service** (Port 9018): İşlem yönetimi
- **Eureka Server** (Port 8761): Service Discovery
- **Config Server** (Port 8888): Configuration Management

## 🛠️ Teknolojiler

- **Java 17**
- **Spring Boot 3.4.1**
- **Spring Cloud 2024.0.0**
- **PostgreSQL**: Veritabanı
- **Redis**: Cache
- **Kafka**: Message Queue
- **Resilience4j**: Fault Tolerance
- **Swagger/OpenAPI**: API Documentation
- **Docker**: Containerization
- **GitHub Actions**: CI/CD

## 📚 Dokümantasyon

- [HOW_TO_RUN.md](HOW_TO_RUN.md): Sistem çalıştırma kılavuzu
- [RESILIENCE_FAULT_TOLERANCE.md](RESILIENCE_FAULT_TOLERANCE.md): Resilience ve Fault Tolerance açıklamaları
- [DOCKER_SETUP.md](DOCKER_SETUP.md): Docker kurulum ve kullanım kılavuzu
- [SWAGGER_OPENAPI.md](SWAGGER_OPENAPI.md): Swagger/OpenAPI dokümantasyonu
- [CI_CD_PIPELINE.md](CI_CD_PIPELINE.md): CI/CD pipeline dokümantasyonu

## 🚀 Hızlı Başlangıç

### Docker Compose ile Çalıştırma

```bash
docker compose up -d
```

### Manuel Çalıştırma

Detaylı bilgi için [HOW_TO_RUN.md](HOW_TO_RUN.md) dosyasına bakın.

## 📡 API Endpoints

### Authentication
- `POST /api/auth/login` - Kullanıcı girişi
- `GET /api/auth/validate` - Token doğrulama

### Müşteri İşlemleri
- `POST /api/customers` - Yeni müşteri ekleme
- `GET /api/customers` - Tüm müşterileri listeleme
- `GET /api/customers/{id}` - ID'ye göre müşteri getirme
- `PUT /api/customers/{id}` - Müşteri bilgilerini güncelleme
- `DELETE /api/customers/{id}` - Müşteri silme

### Hesap İşlemleri
- `POST /api/accounts` - Yeni hesap açma (vadesiz/vadeli)
- `GET /api/accounts` - Tüm hesapları listeleme
- `GET /api/accounts/{accountNo}` - Hesap numarasına göre hesap getirme
- `GET /api/accounts/customer/{customerId}` - Müşteriye ait tüm hesapları listeleme
- `DELETE /api/accounts/{accountNo}` - Hesap kapatma

### Bankacılık İşlemleri
- `POST /api/processes/deposit-money` - Para yatırma
- `POST /api/processes/withdraw-money` - Para çekme
- `GET /api/processes/amount/{accountNo}` - Bakiye görüntüleme
- `POST /api/processes/interest-earn/{accountNo}` - Vadeli hesaba faiz işleme
- `GET /api/processes/account-history/{accountNo}` - Hesap özeti

## 🔒 Resilience & Fault Tolerance

Projede Resilience4j kullanılarak aşağıdaki özellikler sağlanmaktadır:

- **Circuit Breaker**: Servis hatalarında otomatik devre kesici
- **Retry**: Başarısız istekler için otomatik yeniden deneme
- **Timeout**: İstek timeout yönetimi
- **Fallback**: Servis down olduğunda alternatif yanıt

Detaylı bilgi için [RESILIENCE_FAULT_TOLERANCE.md](RESILIENCE_FAULT_TOLERANCE.md) dosyasına bakın.

## 🐳 Docker

Tüm servisler Docker container'ları olarak çalıştırılabilir. Detaylı bilgi için [DOCKER_SETUP.md](DOCKER_SETUP.md) dosyasına bakın.

## 🔄 CI/CD

GitHub Actions ile otomatik build, test ve deployment pipeline'ı mevcuttur. Detaylı bilgi için [CI_CD_PIPELINE.md](CI_CD_PIPELINE.md) dosyasına bakın.

## 📝 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.
