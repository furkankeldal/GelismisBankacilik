Postman de atılabilecek örnek istekler

Müşteri işlemleri
POST /api/customers - Yeni müşteri ekleme
GET /api/customers - Tüm müşterileri listeleme
GET /api/customers/{id} - ID'ye göre müşteri getirme
PUT /api/customers/{id} - Müşteri bilgilerini güncelleme
DELETE /api/customers/{id} - Müşteri silme

Hesap İşlemleri
POST /api/accounts - Yeni hesap açma (vadesiz/vadeli)
GET /api/accounts - Tüm hesapları listeleme
GET /api/accounts/{accountNo} - Hesap numarasına göre hesap getirme
GET /api/accounts/customer/{customerId} - Müşteriye ait tüm hesapları listeleme
DELETE /api/accounts/{accountNo} - Hesap kapatma

Bankacılık İşlemleri
POST /api/process/deposit-money - Para yatırma
POST /api/process/withdraw-money - Para çekme
GET /api/process/amount/{accountNo} - Bakiye görüntüleme
POST /api/process/interest-earn/{accountNo} - Vadeli hesaba faiz işleme
GET /api/process/account-history/{accountNo} - Hesap özeti
