# CI/CD Pipeline Documentation

Bu dokümantasyon, projede kullanılan GitHub Actions CI/CD pipeline'ını açıklar.

## Genel Bakış

Projede iki ana CI/CD workflow'u bulunmaktadır:

1. **CI/CD Pipeline** (`ci-cd.yml`): Build, test, Docker build ve security scan
2. **Docker Compose Test** (`docker-compose.yml`): Docker Compose ile entegrasyon testleri

## Workflow'lar

### 1. CI/CD Pipeline (`ci-cd.yml`)

Bu workflow aşağıdaki job'ları içerir:

#### Build Job

- **Trigger**: `push` ve `pull_request` event'leri (main ve develop branch'leri)
- **Services**: PostgreSQL ve Redis
- **Steps**:
  1. Code checkout
  2. JDK 17 setup
  3. Maven build (skip tests)
  4. Test execution
  5. Test report generation

#### Docker Build Job

- **Trigger**: Sadece `push` event'leri (main ve develop branch'leri)
- **Matrix Strategy**: Her servis için ayrı build
- **Services**: config-server, eureka-server, api-gateway, customer-service, account-service, process-service
- **Steps**:
  1. Code checkout
  2. Docker Buildx setup
  3. Docker image build (push yapılmaz, sadece build)

#### Security Scan Job

- **Trigger**: Sadece `push` event'leri
- **Tool**: Trivy vulnerability scanner
- **Steps**:
  1. Code checkout
  2. Trivy scan (filesystem)
  3. SARIF formatında sonuç üretme
  4. GitHub Security'a upload

#### Deploy Job

- **Trigger**: Sadece `main` branch'ine push
- **Environment**: production
- **Steps**:
  1. Code checkout
  2. Deployment steps (örnek: kubectl apply, docker-compose up)

### 2. Docker Compose Test (`docker-compose.yml`)

Bu workflow Docker Compose ile entegrasyon testleri yapar:

- **Trigger**: `push`, `pull_request` (main ve develop) ve `workflow_dispatch`
- **Steps**:
  1. Code checkout
  2. Docker Compose setup
  3. Docker images build
  4. Services start
  5. Health check wait
  6. Service health verification
  7. Log viewing on failure
  8. Services stop

## Workflow Tetikleme

### Otomatik Tetikleme

- **Push**: main veya develop branch'ine push yapıldığında
- **Pull Request**: main veya develop branch'ine PR açıldığında

### Manuel Tetikleme

Docker Compose test workflow'u manuel olarak tetiklenebilir:

1. GitHub Actions sekmesine gidin
2. "Docker Compose Test" workflow'unu seçin
3. "Run workflow" butonuna tıklayın

## Environment Variables

Workflow'larda kullanılan environment variable'lar:

```yaml
env:
  MAVEN_OPTS: -Dmaven.repo.local=.m2/repository
  JAVA_VERSION: '17'
```

## Services

CI/CD pipeline'ında kullanılan servisler:

### PostgreSQL

```yaml
postgres:
  image: postgres:15-alpine
  env:
    POSTGRES_DB: bankdb
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
  ports:
    - 5432:5432
```

### Redis

```yaml
redis:
  image: redis:7-alpine
  ports:
    - 6379:6379
```

## Test Reports

Test sonuçları GitHub Actions'da otomatik olarak görüntülenir:

- **Test Results**: Her job'un test sonuçları görüntülenir
- **Coverage**: Test coverage raporları (eğer yapılandırılmışsa)
- **SARIF**: Security scan sonuçları GitHub Security sekmesinde görüntülenir

## Docker Image Caching

Docker build job'unda GitHub Actions cache kullanılır:

```yaml
cache-from: type=gha
cache-to: type=gha,mode=max
```

Bu sayede Docker image build süreleri önemli ölçüde azalır.

## Deployment

Production deployment için:

1. **Environment**: `production` environment'ı oluşturun
2. **Secrets**: Gerekli secret'ları ekleyin (örnek: deployment credentials)
3. **Deploy Job**: Deploy job'unda gerçek deployment komutlarını ekleyin

### Örnek Deployment Komutları

```yaml
- name: Deploy to production
  run: |
    # Kubernetes deployment
    kubectl apply -f k8s/
    
    # veya Docker Compose
    docker-compose -f docker-compose.prod.yml up -d
```

## Security Scanning

Trivy scanner aşağıdaki güvenlik açıklarını tespit eder:

- **Vulnerabilities**: Bilinen güvenlik açıkları
- **Misconfigurations**: Yapılandırma hataları
- **Secrets**: Hardcoded secret'lar

Sonuçlar GitHub Security sekmesinde görüntülenir.

## Best Practices

1. **Branch Protection**: main branch için branch protection rules ekleyin
2. **Required Checks**: CI/CD job'larını required check olarak işaretleyin
3. **Secrets Management**: Hassas bilgileri GitHub Secrets'da saklayın
4. **Environment Variables**: Environment-specific değişkenleri environment'larda tanımlayın
5. **Cache Usage**: Build sürelerini azaltmak için cache kullanın
6. **Parallel Jobs**: Mümkün olduğunca job'ları paralel çalıştırın

## Troubleshooting

### Build Failures

1. **Logs**: GitHub Actions'da job loglarını kontrol edin
2. **Local Test**: Sorunları lokal olarak reproduce edin
3. **Dependencies**: Dependency sorunlarını kontrol edin

### Test Failures

1. **Service Health**: PostgreSQL ve Redis'in çalıştığından emin olun
2. **Environment Variables**: Test için gerekli environment variable'ların set edildiğinden emin olun
3. **Flaky Tests**: Geçici test hatalarını tespit edin ve düzeltin

### Docker Build Failures

1. **Dockerfile**: Dockerfile'ların doğru olduğundan emin olun
2. **Context**: Docker build context'inin doğru olduğundan emin olun
3. **Dependencies**: Build-time dependency'lerin mevcut olduğundan emin olun

## Daha Fazla Bilgi

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Buildx Documentation](https://docs.docker.com/buildx/)
- [Trivy Documentation](https://aquasecurity.github.io/trivy/)


