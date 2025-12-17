# Swagger/OpenAPI Documentation

Bu dokümantasyon, projede kullanılan Swagger/OpenAPI yapılandırmasını ve kullanımını açıklar.

## Genel Bakış

Tüm microservices'lerde Swagger/OpenAPI 3.0 kullanılarak API dokümantasyonu sağlanmaktadır. SpringDoc OpenAPI kütüphanesi kullanılmaktadır.

## Erişim URL'leri

Her servis için Swagger UI'ye aşağıdaki URL'lerden erişilebilir:

- **API Gateway**: http://localhost:8095/swagger-ui.html
- **Customer Service**: http://localhost:9017/swagger-ui.html
- **Account Service**: http://localhost:9016/swagger-ui.html
- **Process Service**: http://localhost:9018/swagger-ui.html

## API Docs (JSON)

OpenAPI JSON formatındaki dokümantasyona aşağıdaki URL'lerden erişilebilir:

- **API Gateway**: http://localhost:8095/api-docs
- **Customer Service**: http://localhost:9017/api-docs
- **Account Service**: http://localhost:9016/api-docs
- **Process Service**: http://localhost:9018/api-docs

## Yapılandırma

### Global Yapılandırma

**Dosya**: `config-repo/application.yml`

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
```

## Controller Annotations

Tüm controller'larda aşağıdaki annotations kullanılmaktadır:

### @Tag

Controller seviyesinde API grubunu tanımlar:

```java
@Tag(name = "Customer Management", description = "Müşteri yönetimi API endpoints")
@RestController
@RequestMapping("/customers")
public class CustomerController {
    // ...
}
```

### @Operation

Endpoint seviyesinde işlem açıklaması:

```java
@PostMapping
@Operation(summary = "Yeni müşteri ekle", description = "Sistemde yeni bir müşteri kaydı oluşturur")
public ResponseEntity<CustomerResponseDto> addCustomer(@Valid @RequestBody CustomerRequestDto dto) {
    // ...
}
```

### @ApiResponses ve @ApiResponse

Endpoint'in dönebileceği HTTP response kodlarını tanımlar:

```java
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Müşteri başarıyla oluşturuldu",
            content = @Content(schema = @Schema(implementation = CustomerResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Geçersiz istek verisi"),
    @ApiResponse(responseCode = "409", description = "Müşteri zaten mevcut")
})
```

### @Parameter

Path variable ve query parameter'ları dokümante eder:

```java
@GetMapping("/{customerId}")
@Operation(summary = "Müşteri bilgisi getir")
public ResponseEntity<CustomerResponseDto> getByCustomerId(
        @Parameter(description = "Müşteri ID", required = true) @PathVariable Long customerId) {
    // ...
}
```

## API Endpoints

### Customer Service

- `POST /customers` - Yeni müşteri ekle
- `GET /customers` - Tüm müşterileri listele
- `GET /customers/{customerId}` - Müşteri bilgisi getir
- `PUT /customers/{customerId}` - Müşteri bilgilerini güncelle
- `DELETE /customers/{customerId}` - Müşteri sil

### Account Service

- `POST /accounts` - Yeni hesap aç
- `GET /accounts` - Tüm hesapları listele
- `GET /accounts/{accountNo}` - Hesap bilgisi getir
- `GET /accounts/customer/{customerId}` - Müşteri hesaplarını listele
- `DELETE /accounts/{accountNo}` - Hesap kapat
- `POST /accounts/{accountNo}/deposit` - Para yatır
- `POST /accounts/{accountNo}/withdraw` - Para çek
- `POST /accounts/{accountNo}/interest` - Faiz işle

### Process Service

- `POST /processes/deposit-money` - Para yatır (Process Service üzerinden)
- `POST /processes/withdraw-money` - Para çek (Process Service üzerinden)
- `GET /processes/amount/{accountNo}` - Bakiye görüntüle
- `POST /processes/interest-earn/{accountNo}` - Faiz kazan
- `GET /processes/account-history/{accountNo}` - Hesap özeti

### API Gateway - Authentication

- `POST /api/auth/login` - Kullanıcı girişi
- `GET /api/auth/validate` - Token doğrulama

## Swagger UI Özellikleri

1. **Try It Out**: Her endpoint'i doğrudan Swagger UI'den test edebilirsiniz
2. **Request/Response Examples**: Örnek request ve response'lar gösterilir
3. **Schema Definitions**: Tüm DTO'ların şemaları görüntülenir
4. **Authentication**: JWT token ile authentication test edilebilir

## Authentication

API Gateway'deki protected endpoint'leri test etmek için:

1. `/api/auth/login` endpoint'inden token alın
2. Swagger UI'de "Authorize" butonuna tıklayın
3. Token'ı `Bearer {token}` formatında girin

## Best Practices

1. **Açıklayıcı Açıklamalar**: Her endpoint için açıklayıcı summary ve description ekleyin
2. **Response Codes**: Tüm olası HTTP response kodlarını dokümante edin
3. **Parameter Descriptions**: Tüm parametreler için açıklama ekleyin
4. **Schema Examples**: DTO'lar için örnek değerler sağlayın

## Özelleştirme

Swagger UI'yi özelleştirmek için `application.yml` dosyasında aşağıdaki ayarları yapabilirsiniz:

```yaml
springdoc:
  swagger-ui:
    operations-sorter: method  # method, alpha
    tags-sorter: alpha  # alpha
    try-it-out-enabled: true
    filter: true  # Endpoint filtreleme
    display-request-duration: true  # İstek süresini göster
```

## Daha Fazla Bilgi

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)


