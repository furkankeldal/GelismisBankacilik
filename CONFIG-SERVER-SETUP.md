# Config Server Kurulum Rehberi

## Sorun: Git Repository Erişim Hatası

Config Server'da Git repository erişim hatası alıyorsanız, iki çözüm seçeneğiniz var:

## Çözüm 1: Native (Local) Mod (Önerilen - Test için)

Config Server şu anda **native mod** kullanıyor. Bu mod, Git repository yerine local dosya sistemini kullanır.

### Avantajları:
- ✅ Git repository gerekmez
- ✅ Hızlı test ve geliştirme
- ✅ İnternet bağlantısı gerekmez

### Kullanım:
Config dosyaları `config-repo/` klasöründe bulunuyor. Config Server bu klasörü otomatik olarak okur.

### Test:
```bash
# Config Server'ı başlatın
cd config-server
mvn spring-boot:run

# Test edin
curl http://localhost:8888/account-service/default
```

## Çözüm 2: Git Repository Kullanımı

### Adım 1: Git Repository Oluşturma

1. GitHub'da yeni bir repository oluşturun:
   - Repository adı: `banking-config-repo`
   - Public veya Private (Private için authentication gerekir)

2. Config dosyalarını repository'ye yükleyin:

```bash
# Yeni klasör oluşturun
mkdir banking-config-repo
cd banking-config-repo

# Git başlatın
git init

# Config dosyalarını kopyalayın
cp -r ../config-repo/* .

# Commit yapın
git add .
git commit -m "Initial config files"

# Remote ekleyin
git remote add origin https://github.com/furkankeldal/banking-config-repo.git
git branch -M main
git push -u origin main
```

### Adım 2: Config Server'ı Git Moduna Geçirme

1. `config-server/src/main/resources/application.yml` dosyasını düzenleyin:

```yaml
spring:
  profiles:
    active: git  # native yerine git
  cloud:
    config:
      server:
        git:
          uri: https://github.com/furkankeldal/banking-config-repo.git
```

VEYA

2. `application-git.yml` profilini kullanın:

```bash
# Config Server'ı Git modunda başlatın
cd config-server
mvn spring-boot:run -Dspring-boot.run.profiles=git
```

### Adım 3: Private Repository için Authentication

Eğer repository private ise, authentication ekleyin:

#### Seçenek A: Username/Password
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/furkankeldal/banking-config-repo.git
          username: ${GIT_USERNAME:your-username}
          password: ${GIT_PASSWORD:your-token}
```

**Not:** GitHub için password yerine Personal Access Token kullanın.

#### Seçenek B: SSH
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: git@github.com:furkankeldal/banking-config-repo.git
          ignore-local-ssh-settings: false
          private-key: ${GIT_PRIVATE_KEY:}
```

### Adım 4: Environment Variables (Güvenli Yöntem)

Sensitive bilgileri environment variable olarak kullanın:

```bash
# Windows PowerShell
$env:GIT_USERNAME="your-username"
$env:GIT_PASSWORD="your-token"
$env:GIT_REPO_URI="https://github.com/furkankeldal/GelismisBankacilik.git"

# Linux/Mac
export GIT_USERNAME="your-username"
export GIT_PASSWORD="your-token"
export GIT_REPO_URI="https://github.com/furkankeldal/GelismisBankacilik.git"
```

## Hata Giderme

### Hata: "Could not fetch remote for default remote"
- Repository URL'ini kontrol edin
- Repository'nin public olduğundan veya authentication bilgilerinin doğru olduğundan emin olun
- İnternet bağlantınızı kontrol edin

### Hata: "Authentication failed"
- GitHub Personal Access Token kullanın (password yerine)
- Token'ın `repo` yetkisine sahip olduğundan emin olun

### Hata: "Repository not found"
- Repository adını ve kullanıcı adını kontrol edin
- Repository'nin var olduğundan ve erişilebilir olduğundan emin olun

## Öneri

**Geliştirme ortamı için:** Native mod kullanın (şu anki yapılandırma)
**Production ortamı için:** Git repository kullanın

## Mevcut Yapılandırma

Şu anda Config Server **native mod** kullanıyor ve `config-repo/` klasöründeki dosyaları okuyor. Bu yapılandırma çalışmaya hazır!

