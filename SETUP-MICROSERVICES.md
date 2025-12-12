# Microservice Yapısı Kurulum Rehberi

## Adım 1: Mevcut Kodları Account Service'e Taşıma

Mevcut `src` klasöründeki tüm kodları `account-service/src` klasörüne kopyalayın:

### Windows (PowerShell):
```powershell
# Mevcut src klasörünü account-service'e kopyala
Copy-Item -Path "src\*" -Destination "account-service\src\" -Recurse -Force
```

### Linux/Mac:
```bash
# Mevcut src klasörünü account-service'e kopyala
cp -r src/* account-service/src/
```

## Adım 2: Application Sınıfını Güncelleme

`account-service/src/main/java/com/example/OnlineBankacilik/OnlineBankacilikApplication.java` dosyasını silin veya `AccountServiceApplication.java` ile değiştirin.

## Adım 3: Git Repository Oluşturma

Config Server için ayrı bir Git repository oluşturun:

```bash
# Yeni bir klasör oluşturun
mkdir banking-config-repo
cd banking-config-repo

# Git repository başlatın
git init

# Config dosyalarını kopyalayın
cp -r ../config-repo/* .

# Commit yapın
git add .
git commit -m "Initial config repository"

# Remote repository ekleyin (GitHub, GitLab, vb.)
git remote add origin <your-git-repo-url>
git branch -M main
git push -u origin main
```

## Adım 4: Config Server Yapılandırması

`config-server/src/main/resources/application.yml` dosyasında Git repository URL'ini güncelleyin:

```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-username/banking-config-repo.git
```

## Adım 5: Parent POM Kullanımı

Projeyi multi-module Maven projesi olarak çalıştırmak için:

```bash
# Root dizinde (pom-parent.xml'in olduğu yerde)
mvn clean install

# Tüm modülleri build etmek için
mvn clean install -pl config-server,api-gateway,account-service -am
```

## Adım 6: Servisleri Başlatma

### Sıralı Başlatma:

1. **Config Server** (Port: 8888)
```bash
cd config-server
mvn spring-boot:run
```

2. **Account Service** (Port: 9016)
```bash
cd account-service
mvn spring-boot:run
```

3. **API Gateway** (Port: 8080)
```bash
cd api-gateway
mvn spring-boot:run
```

## Adım 7: Test Etme

### Config Server Test:
```bash
curl http://localhost:8888/account-service/default
```

### Account Service Test:
```bash
curl http://localhost:9016/accounts
```

### Gateway Test:
```bash
curl http://localhost:8080/api/accounts
```

## Notlar

- Eureka Server kullanmak istiyorsanız, ayrı bir eureka-server modülü oluşturmanız gerekir
- Production ortamında H2 yerine PostgreSQL/MySQL kullanın
- Redis ve Kafka servislerinin çalıştığından emin olun
- Config Server'ın Git repository'ye erişim yetkisi olduğundan emin olun

## Sorun Giderme

### Config Server Git bağlantı hatası:
- Git repository URL'ini kontrol edin
- SSH key veya token kullanıyorsanız doğru yapılandırıldığından emin olun
- Repository'nin public olduğundan veya erişim izinlerinin olduğundan emin olun

### Service Discovery hatası:
- Eureka Server eklemeniz gerekebilir
- Veya `application.yml` dosyalarında `eureka.client.enabled=false` yapın

### Feign Client hatası:
- Customer Service'in çalıştığından emin olun
- Veya `CustomerServiceClient` içindeki URL'yi doğru yapılandırın

