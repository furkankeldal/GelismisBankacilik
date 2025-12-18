# 🧪 Test Çalıştırma Kılavuzu

Bu dokümantasyon, projede test'lerin nasıl çalıştırılacağını açıklar.

## 📋 Test Türleri

Projede iki tür test bulunmaktadır:

1. **Unit Test'ler** (`*Test.java`): H2 in-memory database kullanır
2. **Integration Test'ler** (`*IntegrationTest.java`): PostgreSQL database kullanır

## 🚀 Test Çalıştırma Yöntemleri

### 1. Tüm Test'leri Çalıştırma

#### Maven ile (Tüm Modüller)

```bash
# Proje root dizininde
mvn -f pom-parent.xml test
```

#### Belirli Bir Modül İçin

```bash
# Account Service için
cd microservices/account-service
mvn test

# Customer Service için
cd microservices/customer-service
mvn test

# Process Service için
cd microservices/process-service
mvn test
```

### 2. Sadece Unit Test'leri Çalıştırma

#### Tüm Modüller İçin

```bash
# Proje root dizininde
mvn -f pom-parent.xml test -Dtest='*Test' \
  -pl 'microservices/account-service,microservices/customer-service,microservices/process-service' \
  -am
```

#### Belirli Bir Modül İçin

```bash
# Account Service unit test'leri
cd microservices/account-service
mvn test -Dtest='*Test'
```

### 3. Sadece Integration Test'leri Çalıştırma

**ÖNEMLİ:** Integration test'ler PostgreSQL gerektirir. Önce PostgreSQL'in çalıştığından emin olun.

#### PostgreSQL Hazırlığı

```bash
# Docker Compose ile PostgreSQL başlat
docker compose up -d postgres

# Veya manuel PostgreSQL kurulumu
```

#### Integration Test'leri Çalıştırma

```bash
# Tüm modüller için (PostgreSQL environment variable'ları ile)
mvn -f pom-parent.xml test -Dtest='*IntegrationTest' \
  -pl 'microservices/account-service,microservices/customer-service,microservices/process-service' \
  -am \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/bankdb \
  -Dspring.datasource.username=postgres \
  -Dspring.datasource.password=postgres
```

#### Belirli Bir Modül İçin

```bash
# Account Service integration test'leri
cd microservices/account-service
mvn test -Dtest='*IntegrationTest' \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/bankdb \
  -Dspring.datasource.username=postgres \
  -Dspring.datasource.password=postgres
```

### 4. Belirli Bir Test Sınıfını Çalıştırma

```bash
# AccountServiceTest sınıfını çalıştır
cd microservices/account-service
mvn test -Dtest=AccountServiceTest

# AccountServiceIntegrationTest sınıfını çalıştır
mvn test -Dtest=AccountServiceIntegrationTest \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/bankdb \
  -Dspring.datasource.username=postgres \
  -Dspring.datasource.password=postgres
```

### 5. Belirli Bir Test Metodunu Çalıştırma

```bash
# AccountServiceTest.testAccountOpen metodunu çalıştır
cd microservices/account-service
mvn test -Dtest=AccountServiceTest#testAccountOpen
```

## 🛠️ IDE'den Test Çalıştırma

### IntelliJ IDEA

1. **Tüm Test'leri Çalıştırma:**
   - `src/test/java` klasörüne sağ tıklayın
   - "Run 'All Tests'" seçeneğini seçin

2. **Belirli Test Sınıfını Çalıştırma:**
   - Test sınıfına sağ tıklayın
   - "Run 'TestClassName'" seçeneğini seçin

3. **Belirli Test Metodunu Çalıştırma:**
   - Test metodunun yanındaki yeşil oka tıklayın
   - Veya metodun üzerine gelip `Ctrl+Shift+F10` (Windows) / `Cmd+Shift+R` (Mac)

### Eclipse

1. **Tüm Test'leri Çalıştırma:**
   - Projeye sağ tıklayın
   - "Run As" → "JUnit Test" seçeneğini seçin

2. **Belirli Test Sınıfını Çalıştırma:**
   - Test sınıfına sağ tıklayın
   - "Run As" → "JUnit Test" seçeneğini seçin

### VS Code

1. Test sınıfının veya metodunun üzerindeki "Run Test" linkine tıklayın
2. Veya Command Palette'den "Java: Run Tests" komutunu kullanın

## 📊 Test Raporları

### Maven Surefire Raporları

Test'ler çalıştıktan sonra raporlar şu konumda oluşturulur:

```
microservices/account-service/target/surefire-reports/
├── AccountServiceTest.txt
├── AccountServiceTest.xml
├── AccountServiceIntegrationTest.txt
└── AccountServiceIntegrationTest.xml
```

### Raporları Görüntüleme

```bash
# HTML raporu oluştur (Maven Surefire Report plugin gerekli)
mvn surefire-report:report

# Rapor dosyasını aç
# microservices/account-service/target/site/surefire-report.html
```

## 🔧 Test Yapılandırması

### Unit Test Yapılandırması

Unit test'ler `application-test.yml` dosyasını kullanır:

```yaml
# microservices/account-service/src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:accountdb;MODE=PostgreSQL
    driverClassName: org.h2.Driver
    username: sa
    password:
```

### Integration Test Yapılandırması

Integration test'ler `application-integration.yml` dosyasını kullanır:

```yaml
# microservices/account-service/src/test/resources/application-integration.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/bankdb}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
```

## 🐳 Docker Compose ile Test

### Tüm Servisleri Başlat ve Test Et

```bash
# Servisleri başlat
docker compose up -d

# Test'leri çalıştır (local'de)
mvn -f pom-parent.xml test
```

## 📝 CI/CD Pipeline'da Test

GitHub Actions workflow'unda test'ler otomatik olarak çalıştırılır:

```yaml
# .github/workflows/ci-cd.yml
- name: Run unit tests (H2)
  run: mvn -f pom-parent.xml test -Dtest='*Test' ...

- name: Run integration tests (PostgreSQL)
  run: mvn -f pom-parent.xml test -Dtest='*IntegrationTest' ...
```

## ⚠️ Yaygın Sorunlar ve Çözümleri

### 1. PostgreSQL Bağlantı Hatası

**Hata:**
```
Connection refused: connect
```

**Çözüm:**
```bash
# PostgreSQL'in çalıştığından emin olun
docker compose up -d postgres

# Veya manuel PostgreSQL başlatın
```

### 2. Test Bulunamadı Hatası

**Hata:**
```
No tests matching pattern "*Test" were executed!
```

**Çözüm:**
```bash
# Test pattern'ini kontrol edin
mvn test -Dtest='*Test' -DfailIfNoTests=false
```

### 3. H2 Database Hatası

**Hata:**
```
Table "ACCOUNTS" not found
```

**Çözüm:**
- `application-test.yml` dosyasında `ddl-auto: create-drop` olduğundan emin olun
- Test profile'ının aktif olduğundan emin olun: `@ActiveProfiles("test")`

### 4. Redis Bağlantı Hatası

**Hata:**
```
Unable to connect to Redis
```

**Çözüm:**
```bash
# Redis'i başlatın
docker compose up -d redis

# Veya test'lerde Redis'i mock'layın
```

## 🎯 Hızlı Komutlar

```bash
# Tüm test'leri çalıştır
mvn -f pom-parent.xml test

# Sadece unit test'ler
mvn -f pom-parent.xml test -Dtest='*Test' \
  -pl 'microservices/account-service,microservices/customer-service,microservices/process-service' \
  -am

# Sadece integration test'ler (PostgreSQL gerekli)
mvn -f pom-parent.xml test -Dtest='*IntegrationTest' \
  -pl 'microservices/account-service,microservices/customer-service,microservices/process-service' \
  -am \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/bankdb \
  -Dspring.datasource.username=postgres \
  -Dspring.datasource.password=postgres

# Belirli bir test sınıfı
cd microservices/account-service
mvn test -Dtest=AccountServiceTest

# Test'leri atla (sadece build)
mvn -f pom-parent.xml clean install -DskipTests
```

## 📚 Daha Fazla Bilgi

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)

