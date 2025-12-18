# 🏗️ Gelişmiş Bankacılık Sistemi - Mimari Genel Bakış

## 📋 İçindekiler
1. [Genel Mimari](#genel-mimari)
2. [Bileşenler ve Rolleri](#bileşenler-ve-rolleri)
3. [İşleyiş Akışı](#işleyiş-akışı)
4. [Önemli Anotasyonlar](#önemli-anotasyonlar)
5. [Servisler Arası İletişim](#servisler-arası-iletişim)
6. [Görsel Mimari Diyagramı](#görsel-mimari-diyagramı)

---

## 🏛️ Genel Mimari

Bu proje **Microservices Architecture** kullanarak geliştirilmiş bir bankacılık sistemidir. Sistem, birbirinden bağımsız çalışan küçük servislerden oluşur ve Spring Cloud teknolojileri ile yönetilir.

### Mimari Prensipler
- **Service-Oriented Architecture (SOA)**: Her servis kendi sorumluluğuna sahip
- **Decentralized**: Servisler bağımsız deploy edilebilir
- **Resilient**: Hata toleransı ve dayanıklılık
- **Scalable**: Her servis bağımsız olarak ölçeklenebilir

---

## 🧩 Bileşenler ve Rolleri

### 1. 🔐 **Eureka Server** (Port: 8761)

**Rol**: Service Discovery (Servis Keşfi)

**Ne İşe Yarar?**
- Tüm microservice'lerin kayıt olduğu merkezi servis
- Servislerin birbirlerini bulmasını sağlar
- Load balancing için servis instance'larını yönetir
- Health monitoring yapar

**Önemli Anotasyonlar:**
```java
@SpringBootApplication
@EnableEurekaServer  // Eureka Server'ı aktif eder
```

**Yapılandırma:**
```yaml
eureka:
  client:
    register-with-eureka: false  # Kendi kendine kayıt olmaz
    fetch-registry: false        # Registry'yi fetch etmez
```

**Nasıl Çalışır?**
1. Eureka Server başlar
2. Microservice'ler Eureka'ya kayıt olur (heartbeat gönderir)
3. Eureka, kayıtlı servislerin listesini tutar
4. Servisler birbirlerini Eureka'dan öğrenir

---

### 2. ⚙️ **Config Server** (Port: 8888)

**Rol**: Centralized Configuration Management (Merkezi Yapılandırma Yönetimi)

**Ne İşe Yarar?**
- Tüm servislerin yapılandırmalarını merkezi bir yerden yönetir
- Git repository'den yapılandırma dosyalarını okur
- Environment-specific yapılandırmalar sağlar
- Yapılandırma değişikliklerini dinamik olarak yönetir

**Önemli Anotasyonlar:**
```java
@SpringBootApplication
@EnableConfigServer      // Config Server'ı aktif eder
@EnableDiscoveryClient   // Eureka'ya kayıt olur
```

**Yapılandırma:**
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-username/config-repo.git
          default-label: main
```

**Nasıl Çalışır?**
1. Config Server başlar ve Git repository'ye bağlanır
2. Microservice'ler başlarken Config Server'dan yapılandırma alır
3. `config-repo/` klasöründeki YAML dosyaları servislere dağıtılır
4. Her servis kendi adıyla yapılandırma dosyasını alır (örn: `account-service.yml`)

**Config Repository Yapısı:**
```
config-repo/
├── application.yml          # Global yapılandırma
├── account-service.yml      # Account Service'e özel
├── customer-service.yml     # Customer Service'e özel
├── process-service.yml      # Process Service'e özel
└── api-gateway.yml         # API Gateway'e özel
```

---

### 3. 🚪 **API Gateway** (Port: 8095)

**Rol**: Single Entry Point (Tek Giriş Noktası)

**Ne İşe Yarar?**
- Tüm API isteklerinin tek giriş noktası
- Routing: İstekleri doğru microservice'e yönlendirir
- Authentication & Authorization: JWT token doğrulama
- Rate Limiting: İstek sayısını sınırlar
- Load Balancing: Eureka ile servis instance'ları arasında yük dağıtımı
- Request/Response Logging: Tüm istekleri loglar

**Önemli Anotasyonlar:**
```java
@SpringBootApplication
@EnableDiscoveryClient  // Eureka'ya kayıt olur ve servisleri keşfeder
```

**Routing Yapılandırması:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: account-service
          uri: lb://account-service  # Eureka'dan bulur
          predicates:
            - Path=/api/accounts/**
          filters:
            - StripPrefix=1  # /api prefix'ini kaldırır
```

**Nasıl Çalışır?**
1. Client isteği API Gateway'e gönderir: `POST http://localhost:8095/api/accounts`
2. Gateway, route kurallarına göre isteği yönlendirir
3. Eureka'dan `account-service` instance'ını bulur
4. İsteği `http://account-service:9016/accounts` adresine yönlendirir
5. Response'u client'a döndürür

**Gateway Filtreleri (Sırayla):**
1. **AuthenticationFilter**: JWT token doğrulama
2. **RateLimitFilter**: Rate limiting kontrolü
3. **RequestResponseLoggingFilter**: Loglama

---

### 4. 👤 **Customer Service** (Port: 9017)

**Rol**: Müşteri Yönetimi

**Ne İşe Yarar?**
- Müşteri CRUD işlemleri (Create, Read, Update, Delete)
- Müşteri bilgilerini yönetir
- Diğer servislerin müşteri bilgilerine erişmesini sağlar

**Önemli Anotasyonlar:**
```java
@SpringBootApplication
@EnableDiscoveryClient  // Eureka'ya kayıt olur
```

**API Endpoints:**
- `POST /customers` - Yeni müşteri ekle
- `GET /customers` - Tüm müşterileri listele
- `GET /customers/{id}` - Müşteri detayı
- `PUT /customers/{id}` - Müşteri güncelle
- `DELETE /customers/{id}` - Müşteri sil

---

### 5. 💳 **Account Service** (Port: 9016)

**Rol**: Hesap Yönetimi

**Ne İşe Yarar?**
- Hesap açma/kapama işlemleri
- Vadesiz ve vadeli hesap yönetimi
- Hesap bilgilerini yönetir
- Customer Service ile iletişim kurar (Feign Client)
- Kafka ile transaction event'leri yayınlar
- Redis ile cache yönetimi

**Önemli Anotasyonlar:**
```java
@SpringBootApplication
@EnableCaching           // Redis cache'i aktif eder
@EnableDiscoveryClient   // Eureka'ya kayıt olur
@EnableFeignClients      // Feign Client'ları aktif eder
```

**Feign Client Kullanımı:**
```java
@FeignClient(name = "customer-service", fallback = CustomerServiceClientFallback.class)
public interface CustomerServiceClient {
    @GetMapping("/customers/{id}")
    CustomerResponseDto getCustomerById(@PathVariable("id") Long id);
}
```

**API Endpoints:**
- `POST /accounts` - Hesap aç
- `GET /accounts` - Tüm hesapları listele
- `GET /accounts/{accountNo}` - Hesap detayı
- `GET /accounts/customer/{customerId}` - Müşteri hesapları
- `POST /accounts/{accountNo}/deposit` - Para yatır
- `POST /accounts/{accountNo}/withdraw` - Para çek
- `DELETE /accounts/{accountNo}` - Hesap kapat

---

### 6. 💰 **Process Service** (Port: 9018)

**Rol**: İşlem Yönetimi

**Ne İşe Yarar?**
- Para yatırma/çekme işlemleri
- Vadeli hesap faiz hesaplama
- İşlem geçmişi yönetimi
- Account Service ile iletişim kurar (Feign Client)
- Kafka ile event'leri dinler

**Önemli Anotasyonlar:**
```java
@SpringBootApplication
@EnableDiscoveryClient  // Eureka'ya kayıt olur
@EnableFeignClients     // Feign Client'ları aktif eder
```

**Feign Client Kullanımı:**
```java
@FeignClient(name = "account-service", fallback = AccountServiceClientFallback.class)
public interface AccountServiceClient {
    @PostMapping("/accounts/{accountNo}/deposit")
    AccountResponseDto deposit(@PathVariable("accountNo") String accountNo, 
                               @RequestBody TransactionRequestDto request);
}
```

**API Endpoints:**
- `POST /processes/deposit-money` - Para yatır
- `POST /processes/withdraw-money` - Para çek
- `GET /processes/amount/{accountNo}` - Bakiye görüntüle
- `POST /processes/interest-earn/{accountNo}` - Faiz işle
- `GET /processes/account-history/{accountNo}` - İşlem geçmişi

---

## 🔄 İşleyiş Akışı

### Senaryo: Yeni Hesap Açma İşlemi

```
1. Client → API Gateway
   POST http://localhost:8095/api/accounts
   Headers: Authorization: Bearer <JWT_TOKEN>
   
2. API Gateway
   ├─ AuthenticationFilter: JWT token doğrula
   ├─ RateLimitFilter: Rate limit kontrolü
   └─ Route: /api/accounts/** → account-service
   
3. API Gateway → Account Service (Eureka'dan bulur)
   POST http://account-service:9016/accounts
   
4. Account Service
   ├─ CustomerServiceClient.getCustomerById() → Customer Service
   │  └─ Eureka'dan customer-service'i bulur
   │  └─ Feign Client ile istek gönderir
   │  └─ Resilience4j: Circuit Breaker, Retry, Timeout
   │
   ├─ Hesap oluştur (PostgreSQL)
   ├─ Redis cache'e kaydet
   └─ Kafka'ya transaction event yayınla
   
5. Account Service → API Gateway
   Response: AccountResponseDto
   
6. API Gateway → Client
   Response: AccountResponseDto
```

### Senaryo: Para Yatırma İşlemi

```
1. Client → API Gateway
   POST http://localhost:8095/api/processes/deposit-money
   
2. API Gateway → Process Service
   POST http://process-service:9018/processes/deposit-money
   
3. Process Service
   ├─ AccountServiceClient.deposit() → Account Service
   │  └─ Eureka'dan account-service'i bulur
   │  └─ Feign Client ile istek gönderir
   │  └─ Resilience4j: Circuit Breaker, Retry, Timeout
   │
   └─ İşlem kaydı oluştur (PostgreSQL)
   
4. Account Service
   ├─ Hesap bakiyesini güncelle (PostgreSQL)
   ├─ Redis cache'i güncelle
   └─ Kafka'ya transaction event yayınla
   
5. Process Service → API Gateway → Client
   Response: TransactionResponseDto
```

---

## 🏷️ Önemli Anotasyonlar

### Spring Boot Anotasyonları

#### `@SpringBootApplication`
```java
@SpringBootApplication
public class AccountServiceApplication {
    // Spring Boot uygulamasını başlatır
    // @Configuration, @EnableAutoConfiguration, @ComponentScan içerir
}
```

#### `@EnableDiscoveryClient`
```java
@EnableDiscoveryClient
// Eureka Server'a kayıt olur ve diğer servisleri keşfeder
// Tüm microservice'lerde kullanılır (Eureka Server hariç)
```

#### `@EnableEurekaServer`
```java
@EnableEurekaServer
// Sadece Eureka Server'da kullanılır
// Service Discovery server'ı aktif eder
```

#### `@EnableConfigServer`
```java
@EnableConfigServer
// Sadece Config Server'da kullanılır
// Merkezi yapılandırma server'ı aktif eder
```

#### `@EnableFeignClients`
```java
@EnableFeignClients
// Feign Client'ları aktif eder
// Account Service ve Process Service'de kullanılır
```

#### `@EnableCaching`
```java
@EnableCaching
// Redis cache'i aktif eder
// Account Service'de kullanılır
```

### Feign Client Anotasyonları

#### `@FeignClient`
```java
@FeignClient(
    name = "customer-service",  // Eureka'daki servis adı
    fallback = CustomerServiceClientFallback.class  // Hata durumunda fallback
)
public interface CustomerServiceClient {
    @GetMapping("/customers/{id}")
    CustomerResponseDto getCustomerById(@PathVariable("id") Long id);
}
```

**Özellikler:**
- `name`: Eureka'daki servis adı (büyük/küçük harf duyarsız)
- `fallback`: Servis down olduğunda çalışacak fallback class
- `url`: Eureka kullanmıyorsanız direkt URL (opsiyonel)

### Resilience4j Anotasyonları

#### `@CircuitBreaker`
```java
@CircuitBreaker(name = "customerService", fallbackMethod = "fallbackMethod")
public CustomerResponseDto getCustomer(Long id) {
    // Servis çağrısı
}
```

#### `@Retry`
```java
@Retry(name = "customerService")
public CustomerResponseDto getCustomer(Long id) {
    // Başarısız olursa otomatik tekrar dener
}
```

#### `@TimeLimiter`
```java
@TimeLimiter(name = "customerService")
public CompletableFuture<CustomerResponseDto> getCustomer(Long id) {
    // Timeout kontrolü
}
```

### Spring Cloud Gateway Anotasyonları

#### `@Component` + `GlobalFilter`
```java
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Request'i filtrele
    }
    
    @Override
    public int getOrder() {
        return -100;  // Filtre sırası (düşük sayı = önce çalışır)
    }
}
```

---

## 🔗 Servisler Arası İletişim

### 1. **Feign Client** (Senkron HTTP İletişimi)

**Kullanım Senaryosu:**
- Account Service → Customer Service (müşteri bilgisi almak için)
- Process Service → Account Service (hesap işlemleri için)

**Avantajlar:**
- Declarative: Interface tanımlayarak kullanım
- Eureka Integration: Otomatik servis keşfi
- Load Balancing: Otomatik yük dağıtımı
- Resilience4j: Circuit Breaker, Retry, Timeout desteği

**Örnek:**
```java
// Account Service içinde
@FeignClient(name = "customer-service", fallback = CustomerServiceClientFallback.class)
public interface CustomerServiceClient {
    @GetMapping("/customers/{id}")
    CustomerResponseDto getCustomerById(@PathVariable("id") Long id);
}

// Kullanım
@Autowired
private CustomerServiceClient customerServiceClient;

public void accountOpen(AccountRequestDto request) {
    // Feign Client ile müşteri bilgisini al
    CustomerResponseDto customer = customerServiceClient.getCustomerById(request.getCustomerId());
    // ...
}
```

### 2. **Kafka** (Asenkron Mesajlaşma)

**Kullanım Senaryosu:**
- Account Service → Kafka (transaction event'leri yayınlar)
- Process Service → Kafka (event'leri dinler)

**Avantajlar:**
- Asenkron: Servisler birbirini beklemez
- Decoupling: Servisler birbirinden bağımsız
- Scalability: Yüksek throughput
- Event-Driven: Event-driven architecture

**Örnek:**
```java
// Account Service - Producer
@Autowired
private KafkaTemplate<String, String> kafkaTemplate;

public void deposit(String accountNo, BigDecimal amount) {
    // İşlem yap
    // ...
    
    // Kafka'ya event yayınla
    TransactionEvent event = new TransactionEvent(accountNo, amount, "DEPOSIT");
    kafkaTemplate.send("transaction-events", event.toJson());
}

// Process Service - Consumer
@KafkaListener(topics = "transaction-events", groupId = "notification-group")
public void handleTransactionEvent(String message) {
    TransactionEvent event = TransactionEvent.fromJson(message);
    // Event'i işle
    // ...
}
```

### 3. **Eureka Service Discovery**

**Nasıl Çalışır?**
1. Servisler başlarken Eureka'ya kayıt olur
2. Eureka, servis adı ve instance bilgilerini tutar
3. Feign Client veya Gateway, servis adını kullanarak Eureka'dan instance bulur
4. Load balancing otomatik yapılır

**Örnek:**
```java
// Account Service başlarken
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    prefer-ip-address: true

// Eureka'da görünen servis adı: ACCOUNT-SERVICE
// Feign Client kullanırken:
@FeignClient(name = "account-service")  // Eureka'dan otomatik bulur
```

---

## 📊 Görsel Mimari Diyagramı

### 1. Genel Sistem Mimarisi

```
                    ┌─────────────────────────────────────┐
                    │      CLIENT (Browser/Mobile)       │
                    │                                     │
                    │  HTTP/HTTPS Requests                │
                    │  JWT Token Authentication           │
                    └──────────────┬──────────────────────┘
                                   │
                                   │ POST /api/accounts
                                   │ GET /api/customers/{id}
                                   │ POST /api/processes/deposit
                                   │
                    ┌──────────────▼──────────────────────┐
                    │      API GATEWAY (Port: 8095)        │
                    │  ┌────────────────────────────────┐  │
                    │  │  Global Filters (Sırayla):     │  │
                    │  │  1. AuthenticationFilter       │  │
                    │  │     - JWT Token Validation    │  │
                    │  │     - API Key Check           │  │
                    │  │  2. RateLimitFilter           │  │
                    │  │     - Redis Rate Limiting     │  │
                    │  │  3. LoggingFilter             │  │
                    │  │     - Request/Response Log    │  │
                    │  └────────────────────────────────┘  │
                    │                                     │
                    │  Routing Rules:                    │
                    │  /api/accounts/**                  │
                    │    → lb://account-service          │
                    │  /api/customers/**                 │
                    │    → lb://customer-service         │
                    │  /api/processes/**                 │
                    │    → lb://process-service          │
                    └───────┬───────────────┬─────────────┘
                            │               │
            ┌───────────────┘               └───────────────┐
            │                                               │
            │ Eureka Service Discovery                     │ Config Server
            │                                               │
    ┌───────▼────────┐                            ┌────────▼────────┐
    │ EUREKA SERVER  │                            │ CONFIG SERVER   │
    │ (Port: 8761)   │                            │ (Port: 8888)    │
    │                │                            │                 │
    │ Service        │                            │ Git Repository: │
    │ Registry:      │                            │ - application.yml│
    │ ✓ account-svc  │                            │ - account-svc.yml│
    │ ✓ customer-svc │                            │ - customer-svc.yml│
    │ ✓ process-svc  │                            │ - process-svc.yml│
    │ ✓ api-gateway  │                            │ - api-gateway.yml│
    │ ✓ config-server│                            │                 │
    └───────┬────────┘                            └────────┬────────┘
            │                                               │
            │ Service Discovery                            │ Configuration
            │                                               │
            └───────────────┬───────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
    ┌───────▼────────┐            ┌────────▼────────┐
    │ MICROSERVICES  │            │ MICROSERVICES    │
    │                │            │                 │
    └────────────────┘            └─────────────────┘
```

### 2. Microservices Detaylı Yapı

```
┌─────────────────────────────────────────────────────────────────────┐
│                    MICROSERVICES LAYER                              │
│                                                                      │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌────────────┐│
│  │ CUSTOMER SERVICE     │  │ ACCOUNT SERVICE      │  │ PROCESS    ││
│  │ (Port: 9017)        │  │ (Port: 9016)         │  │ SERVICE    ││
│  │                      │  │                      │  │ (Port: 9018)││
│  │ @RestController      │  │ @RestController      │  │ @RestController││
│  │ @EnableDiscoveryClient│ │ @EnableDiscoveryClient│ │ @EnableDiscoveryClient││
│  │                      │  │ @EnableFeignClients  │  │ @EnableFeignClients││
│  │                      │  │ @EnableCaching       │  │            ││
│  │                      │  │                      │  │            ││
│  │ Controllers:         │  │ Controllers:         │  │ Controllers:││
│  │ - CustomerController │  │ - AccountController  │  │ - ProcessController││
│  │                      │  │                      │  │            ││
│  │ Services:            │  │ Services:            │  │ Services:  ││
│  │ - CustomerService    │  │ - AccountService     │  │ - ProcessService││
│  │                      │  │                      │  │            ││
│  │ Repositories:        │  │ Repositories:        │  │ Repositories:││
│  │ - CustomerRepository │  │ - AccountRepository  │  │ - ProcessRepository││
│  │                      │  │                      │  │            ││
│  │ Feign Clients:       │  │ Feign Clients:      │  │ Feign Clients:││
│  │ (Yok)                │  │ - CustomerServiceClient│ │ - AccountServiceClient││
│  │                      │  │   (→ customer-svc)  │  │   (→ account-svc)││
│  │                      │  │                      │  │            ││
│  │ Kafka:               │  │ Kafka:               │  │ Kafka:     ││
│  │ (Yok)                │  │ - Producer           │  │ - Consumer ││
│  │                      │  │   (transaction-events)│ │   (transaction-events)││
│  │                      │  │                      │  │            ││
│  │ Cache:               │  │ Cache:               │  │ Cache:     ││
│  │ (Yok)                │  │ - Redis Cache        │  │ (Yok)      ││
│  │                      │  │   (@Cacheable)       │  │            ││
│  │                      │  │                      │  │            ││
│  │ Database:            │  │ Database:            │  │ Database:  ││
│  │ - PostgreSQL         │  │ - PostgreSQL         │  │ - PostgreSQL││
│  │   (customers table)  │  │   (accounts table)   │  │   (processes table)││
│  └──────────────────────┘  └──────────────────────┘  └────────────┘│
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
            │                    │                    │
            │                    │                    │
            ▼                    ▼                    ▼
    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
    │  POSTGRESQL  │    │    REDIS     │    │    KAFKA     │
    │  (Port: 5432)│    │  (Port: 6379)│    │  (Port: 9092)│
    │              │    │              │    │              │
    │  bankdb      │    │  - Cache     │    │  - transaction│
    │  customers   │    │  - Rate Limit│    │    events     │
    │  accounts    │    │              │    │  - DLQ        │
    │  processes   │    │              │    │              │
    └──────────────┘    └──────────────┘    └──────────────┘
```

### 3. İstek Akış Diyagramı (Request Flow)

```
┌─────────┐
│ CLIENT  │
└────┬────┘
     │
     │ 1. POST /api/accounts
     │    Headers: Authorization: Bearer <JWT>
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY (Port: 8095)                                   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Step 1: AuthenticationFilter                         │ │
│  │   - JWT token'ı validate et                         │ │
│  │   - Token geçerli mi kontrol et                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                          │                                 │
│                          ▼                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Step 2: RateLimitFilter                             │ │
│  │   - IP bazlı rate limit kontrolü                    │ │
│  │   - Redis'ten limit bilgisi al                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                          │                                 │
│                          ▼                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Step 3: Route Matching                               │ │
│  │   - Path: /api/accounts/**                          │ │
│  │   - Route: account-service                          │ │
│  │   - Eureka'dan account-service instance bul         │ │
│  └──────────────────────────────────────────────────────┘ │
│                          │                                 │
└──────────────────────────┼─────────────────────────────────┘
                           │
                           │ 2. POST /accounts
                           │    (StripPrefix: /api kaldırıldı)
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ ACCOUNT SERVICE (Port: 9016)                                │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ AccountController.openAnAcoount()                    │ │
│  │   @PostMapping("/accounts")                          │ │
│  └──────────────────────────────────────────────────────┘ │
│                          │                                 │
│                          ▼                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ AccountService.accountOpen()                         │ │
│  │                                                       │ │
│  │  Step 1: CustomerServiceClient.getCustomerById()     │ │
│  │    ┌──────────────────────────────────────────────┐  │ │
│  │    │ Feign Client → Eureka → customer-service     │  │ │
│  │    │ Resilience4j:                                │  │ │
│  │    │ - Circuit Breaker (hata durumunda)           │  │ │
│  │    │ - Retry (3 kez dene)                         │  │ │
│  │    │ - Timeout (5 saniye)                          │  │ │
│  │    │ - Fallback (CustomerServiceClientFallback)    │  │ │
│  │    └──────────────────────────────────────────────┘  │ │
│  │                          │                            │ │
│  │                          ▼                            │ │
│  │    ┌──────────────────────────────────────────────┐  │ │
│  │    │ CUSTOMER SERVICE (Port: 9017)                │  │ │
│  │    │ CustomerController.getByCustomerId()         │  │ │
│  │    │ → CustomerService.getById()                  │  │ │
│  │    │ → CustomerRepository.findById()              │  │ │
│  │    │ → PostgreSQL: SELECT * FROM customers       │  │ │
│  │    └──────────────────────────────────────────────┘  │ │
│  │                          │                            │ │
│  │                          ▼                            │ │
│  │    Response: CustomerResponseDto                      │ │
│  │                                                       │ │
│  │  Step 2: AccountRepository.save()                   │ │
│  │    → PostgreSQL: INSERT INTO accounts                │ │
│  │                                                       │ │
│  │  Step 3: Redis Cache Update                         │ │
│  │    @Cacheable("accounts")                            │ │
│  │    → Redis: SET account:{accountNo}                  │ │
│  │                                                       │ │
│  │  Step 4: Kafka Producer                             │ │
│  │    TransactionProducer.publish()                     │ │
│  │    → Kafka: transaction-events topic                │ │
│  └──────────────────────────────────────────────────────┘ │
│                          │                                 │
└──────────────────────────┼─────────────────────────────────┘
                           │
                           │ 3. Response: AccountResponseDto
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ API GATEWAY                                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ RequestResponseLoggingFilter                         │ │
│  │   - Request ve Response'u logla                     │ │
│  └──────────────────────────────────────────────────────┘ │
└──────────────────────────┬─────────────────────────────────┘
                           │
                           │ 4. Response: AccountResponseDto
                           │
                           ▼
┌─────────┐
│ CLIENT  │
└─────────┘
```

### 4. Servisler Arası İletişim Diyagramı

```
┌─────────────────────────────────────────────────────────────────┐
│                    SERVİSLER ARASI İLETİŞİM                     │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 1. FEIGN CLIENT (Senkron HTTP)                          │  │
│  │                                                          │  │
│  │  Account Service → Customer Service                     │  │
│  │  ┌──────────────────────────────────────────────────┐   │  │
│  │  │ @FeignClient(name = "customer-service")          │   │  │
│  │  │ public interface CustomerServiceClient {         │   │  │
│  │  │   @GetMapping("/customers/{id}")                  │   │  │
│  │  │   CustomerResponseDto getCustomerById(Long id);  │   │  │
│  │  │ }                                                 │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  │                    │                                      │  │
│  │                    │ Eureka Service Discovery            │  │
│  │                    ▼                                      │  │
│  │  ┌──────────────────────────────────────────────────┐   │  │
│  │  │ 1. Eureka'dan customer-service instance bul      │   │  │
│  │  │ 2. HTTP GET http://customer-service:9017/...    │   │  │
│  │  │ 3. Load Balancing (birden fazla instance varsa) │   │  │
│  │  │ 4. Resilience4j:                                │   │  │
│  │  │    - Circuit Breaker (hata durumunda)            │   │  │
│  │  │    - Retry (3 kez dene)                          │   │  │
│  │  │    - Timeout (5 saniye)                          │   │  │
│  │  │    - Fallback (CustomerServiceClientFallback)    │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 2. KAFKA (Asenkron Mesajlaşma)                          │  │
│  │                                                          │  │
│  │  Account Service (Producer) → Kafka → Process Service  │  │
│  │  ┌──────────────────────────────────────────────────┐   │  │
│  │  │ Account Service:                                 │   │  │
│  │  │   kafkaTemplate.send("transaction-events", event)│   │  │
│  │  │   → Kafka Topic: transaction-events               │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  │                    │                                      │  │
│  │                    ▼                                      │  │
│  │  ┌──────────────────────────────────────────────────┐   │  │
│  │  │ Kafka Broker                                     │   │  │
│  │  │   Topic: transaction-events                       │   │  │
│  │  │   Partition: 0, 1, 2...                          │   │  │
│  │  │   Consumer Group: notification-group              │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  │                    │                                      │  │
│  │                    ▼                                      │  │
│  │  ┌──────────────────────────────────────────────────┐   │  │
│  │  │ Process Service (Consumer):                      │   │  │
│  │  │   @KafkaListener(topics = "transaction-events")  │   │  │
│  │  │   public void handleTransactionEvent(String msg) │   │  │
│  │  │   → Event'i işle                                 │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 3. EUREKA SERVICE DISCOVERY                              │  │
│  │                                                          │  │
│  │  Tüm Servisler → Eureka Server                          │  │
│  │  ┌──────────────────────────────────────────────────┐   │  │
│  │  │ 1. Servis başlarken Eureka'ya kayıt olur         │   │  │
│  │  │    eureka.client.service-url.defaultZone=...     │   │  │
│  │  │                                                  │   │  │
│  │  │ 2. Heartbeat gönderir (30 saniyede bir)         │   │  │
│  │  │    eureka.instance.lease-renewal-interval=30     │   │  │
│  │  │                                                  │   │  │
│  │  │ 3. Eureka, servis listesini tutar                │   │  │
│  │  │    - account-service: [instance1, instance2]     │   │  │
│  │  │    - customer-service: [instance1]               │   │  │
│  │  │                                                  │   │  │
│  │  │ 4. Feign Client veya Gateway, servis adını      │   │  │
│  │  │    kullanarak Eureka'dan instance bulur          │   │  │
│  │  │    @FeignClient(name = "account-service")        │   │  │
│  │  │    → Eureka: account-service instance'ları      │   │  │
│  │  │    → Load Balancing (Round Robin)               │   │  │
│  │  └──────────────────────────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Servis Başlatma Sırası

**ÖNEMLİ:** Servisler belirli bir sırayla başlatılmalıdır!

```
1. PostgreSQL, Redis, Kafka
   └─ Altyapı servisleri (Docker Compose ile)

2. Eureka Server (Port: 8761)
   └─ Service Discovery için gerekli
   └─ Diğer servislerin kayıt olabilmesi için

3. Config Server (Port: 8888)
   └─ Yapılandırma yönetimi için
   └─ Eureka'ya kayıt olur

4. Microservices (Sıra önemli değil)
   ├─ Customer Service (Port: 9017)
   ├─ Account Service (Port: 9016)
   └─ Process Service (Port: 9018)
   └─ Hepsi Eureka ve Config Server'a bağlanır

5. API Gateway (Port: 8095)
   └─ Son olarak başlatılır
   └─ Tüm servislerin hazır olması gerekir
```

---

## 📡 API Endpoints ve REST Metodları

### Customer Service Endpoints

**Base URL:** `http://localhost:8095/api/customers` (Gateway üzerinden)

| Method | Endpoint | Açıklama | Request Body | Response |
|--------|----------|----------|--------------|----------|
| `POST` | `/customers` | Yeni müşteri ekle | `CustomerRequestDto` | `CustomerResponseDto` (201) |
| `GET` | `/customers` | Tüm müşterileri listele | - | `List<CustomerResponseDto>` (200) |
| `GET` | `/customers/{customerId}` | Müşteri detayı | - | `CustomerResponseDto` (200) |
| `PUT` | `/customers/{customerId}` | Müşteri güncelle | `CustomerRequestDto` | `CustomerResponseDto` (200) |
| `DELETE` | `/customers/{customerId}` | Müşteri sil | - | `204 No Content` |

**Örnek Request:**
```json
POST /api/customers
{
  "name": "Ahmet Yılmaz",
  "email": "ahmet@example.com",
  "phone": "555-1234",
  "address": "İstanbul, Türkiye"
}
```

### Account Service Endpoints

**Base URL:** `http://localhost:8095/api/accounts` (Gateway üzerinden)

| Method | Endpoint | Açıklama | Request Body | Response |
|--------|----------|----------|--------------|----------|
| `POST` | `/accounts` | Yeni hesap aç | `AccountRequestDto` | `AccountResponseDto` (201) |
| `GET` | `/accounts` | Tüm hesapları listele | - | `List<AccountResponseDto>` (200) |
| `GET` | `/accounts/{accountNo}` | Hesap detayı | - | `AccountResponseDto` (200) |
| `GET` | `/accounts/customer/{customerId}` | Müşteri hesapları | - | `List<AccountResponseDto>` (200) |
| `DELETE` | `/accounts/{accountNo}` | Hesap kapat | - | `204 No Content` |
| `POST` | `/accounts/{accountNo}/deposit` | Para yatır | `TransactionRequestDto` | `AccountResponseDto` (200) |
| `POST` | `/accounts/{accountNo}/withdraw` | Para çek | `TransactionRequestDto` | `AccountResponseDto` (200) |
| `POST` | `/accounts/{accountNo}/interest` | Faiz işle | - | `AccountResponseDto` (200) |

**Örnek Request:**
```json
POST /api/accounts
{
  "customerId": 1,
  "accountType": "VADELI",
  "currencyType": "TRY",
  "balance": 1000.00
}
```

### Process Service Endpoints

**Base URL:** `http://localhost:8095/api/processes` (Gateway üzerinden)

| Method | Endpoint | Açıklama | Request Body | Response |
|--------|----------|----------|--------------|----------|
| `POST` | `/processes/deposit-money` | Para yatır | `ProcessRequestDto` | `ProcessResponseDto` (200) |
| `POST` | `/processes/withdraw-money` | Para çek | `ProcessRequestDto` | `ProcessResponseDto` (200) |
| `GET` | `/processes/amount/{accountNo}` | Bakiye görüntüle | - | `ProcessResponseDto` (200) |
| `POST` | `/processes/interest-earn/{accountNo}` | Faiz işle | - | `ProcessResponseDto` (200) |
| `GET` | `/processes/account-history/{accountNo}` | İşlem geçmişi | - | `List<ProcessResponseDto>` (200) |

**Örnek Request:**
```json
POST /api/processes/deposit-money
{
  "accountNo": "1234567890",
  "amount": 500.00,
  "description": "Para yatırma işlemi"
}
```

### API Gateway Authentication Endpoints

**Base URL:** `http://localhost:8095/api/auth`

| Method | Endpoint | Açıklama | Request Body | Response |
|--------|----------|----------|--------------|----------|
| `POST` | `/auth/login` | Kullanıcı girişi | `LoginRequest` | `LoginResponse` (JWT Token) |
| `GET` | `/auth/validate` | Token doğrulama | - | `TokenValidationResponse` (200) |

**Örnek Request:**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

**Örnek Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000
}
```

---

## 🎯 Önemli Kurallar ve Best Practices

### 1. **Service Discovery Kuralı**
- Tüm microservice'ler Eureka'ya kayıt olmalı
- Servis adları büyük/küçük harf duyarsız ama tutarlı olmalı
- `@EnableDiscoveryClient` anotasyonu kullanılmalı

### 2. **Configuration Management Kuralı**
- Tüm yapılandırmalar Config Server'dan gelmeli
- Her servis kendi yapılandırma dosyasına sahip olmalı
- Environment-specific yapılandırmalar kullanılmalı

### 3. **Feign Client Kuralı**
- Servisler arası iletişim için Feign Client kullanılmalı
- Her Feign Client için fallback tanımlanmalı
- Resilience4j ile Circuit Breaker, Retry, Timeout kullanılmalı

### 4. **API Gateway Kuralı**
- Tüm external istekler API Gateway üzerinden gelmeli
- Direct servis erişimi production'da kapatılmalı
- Authentication ve Rate Limiting aktif olmalı

### 5. **Error Handling Kuralı**
- Fallback mekanizmaları kullanılmalı
- Circuit Breaker ile servis down durumları handle edilmeli
- Retry mekanizması ile geçici hatalar handle edilmeli

### 6. **Database Kuralı**
- Her servis kendi database'ine sahip olmalı (Database per Service)
- Servisler arası veri paylaşımı API üzerinden olmalı
- Direct database erişimi yasak

### 7. **Event-Driven Kuralı**
- Asenkron işlemler için Kafka kullanılmalı
- Event'ler immutable olmalı
- Dead Letter Queue (DLQ) kullanılmalı

---

## 📝 Özet

### Bileşen Rolleri:
- **Eureka Server**: Servis keşfi ve yönetimi
- **Config Server**: Merkezi yapılandırma yönetimi
- **API Gateway**: Tek giriş noktası, routing, auth, rate limiting
- **Customer Service**: Müşteri yönetimi
- **Account Service**: Hesap yönetimi, cache, event publishing
- **Process Service**: İşlem yönetimi, event consuming

### İletişim Yöntemleri:
- **Feign Client**: Senkron HTTP iletişimi (servisler arası)
- **Kafka**: Asenkron mesajlaşma (event-driven)
- **Eureka**: Servis keşfi (otomatik)

### Önemli Teknolojiler:
- **Spring Cloud Gateway**: API Gateway
- **Spring Cloud Config**: Configuration Management
- **Netflix Eureka**: Service Discovery
- **OpenFeign**: Declarative HTTP Client
- **Resilience4j**: Fault Tolerance
- **Kafka**: Message Queue
- **Redis**: Caching & Rate Limiting

---

## 🔍 Daha Fazla Bilgi

- [HOW_TO_RUN.md](HOW_TO_RUN.md): Sistem çalıştırma kılavuzu
- [RESILIENCE_FAULT_TOLERANCE.md](RESILIENCE_FAULT_TOLERANCE.md): Resilience mekanizmaları
- [DOCKER_SETUP.md](DOCKER_SETUP.md): Docker kurulumu
- [EUREKA-SETUP.md](EUREKA-SETUP.md): Eureka Server detayları
- [CONFIG-SERVER-SETUP.md](CONFIG-SERVER-SETUP.md): Config Server detayları

