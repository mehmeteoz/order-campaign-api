# Order & Campaign API

Bu proje, bir e-ticaret/pazaryeri senaryosu için sipariş ve kampanya yönetimini kapsayan bir REST API uygulamasıdır. Verilen Case Study isterlerine uygun olarak geliştirilmiş olup, genişlemeye açık bir mimari kurgulanmıştır.

*(Not: Geliştirme süreci için Java & Spring Boot tercih edilmiştir.)*

## 🚀 Öne Çıkan Özellikler ve Mimari Kararlar

* **Strategy Design Pattern:** Kampanya değerlendirme mantığı (`CampaignEngine`), "if-else" bloklarına boğulmadan genişletilebilir bir şekilde kurgulanmıştır. Yeni bir kampanya türü eklemek için `CampaignStrategy` interface'ini implement eden yeni bir sınıf yazmak yeterlidir.
* **DTO (Data Transfer Object) Kullanımı:** Entity sınıfları API yanıtlarından soyutlanmış, böylelikle Jackson (JSON) sonsuz döngü (infinite recursion) problemleri engellenmiş ve veri sızıntısının (data leakage) önüne geçilmiştir.
* **Global Hata Yönetimi (ControllerAdvice):** Hatalar (`InsufficientStockException`, `ProductNotFoundException` vb.) merkezi bir noktada yakalanıp, istemciye anlamlı HTTP durum kodları (400, 404) ve JSON hata formatında sunulmaktadır.
* **Veri Önbellekleme (Caching):** Kampanyalar sık değişmeyen veriler olduğu için Spring Cache (`@Cacheable`) kullanılarak veritabanı maliyeti düşürülmüştür.
* **Otomatik Veri Ekimi (Data Seeding):** `CommandLineRunner` sayesinde uygulama ayağa kalkarken `products.json`, `categories.json` ve `authors.json` verileri okunarak otomatik olarak veritabanına aktarılır.
* **Soft Delete:** Kampanyaların silinmek yerine `isActive` bayrağı ile pasife çekilebilmesi sağlanmıştır.

## 🛠️ Kullanılan Teknolojiler

* Java / Spring Boot 3
* Spring Data JPA
* PostgreSQL / MySQL (Docker ile)
* Jackson (JSON Parse İşlemleri)
* Docker & Docker Compose

## 🔐 Authentication (Kimlik Doğrulama)
API endpoint'leri yetkisiz erişime karşı korunmaktadır. Tüm isteklere `x-api-key` başlığı (header) eklenmesi zorunludur.

* **Header Key:** `x-api-key`
* **Header Value:** `VerySecretApiKey123` (veya application.properties dosyasında tanımlanan değer)

---

## 🏗️ Kurulum ve Çalıştırma

Projeyi yerel ortamınızda ayağa kaldırmak için **Docker** kullanabilirsiniz. Harici bir veritabanı veya Java kurulumuna ihtiyaç yoktur.

1. Proje dizinine gidin:
```bash
cd order_api
```

2. Docker Compose ile projeyi ayağa kaldırın:
```bash
docker-compose up -d --build
```
Uygulama hazır olduğunda `http://localhost:8080` portu üzerinden istek kabul etmeye başlayacaktır. Uygulama ayağa kalkarken tablolar otomatik oluşturulacak ve JSON dosyalarındaki başlangıç verileri veritabanına aktarılacaktır.

*(İsteğe bağlı)* Kampanyaları test etmek için `src/main/resources/campaigns.json` dosyası eklenmiş ve kampanyaların da otomatik veritabanına yazılması sağlanmıştır.

---

## 📡 API Endpoint'leri

### 1. Sipariş Oluşturma (POST)
**Endpoint:** `POST /api/v1/orders`

**Request Headers:**
* `Content-Type: application/json`
* `x-api-key: VerySecretApiKey123`

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

### 2. Sipariş Detayı Getirme (GET)
**Endpoint:** `GET /api/v1/orders/{id}`

**Request Headers:**
* `x-api-key: VerySecretApiKey123`

**Response Örneği:**
```json
{
  "id": 1,
  "items": [
    {
      "productId": 1,
      "name": "Örnek Ürün",
      "quantity": 2,
      "unitPrice": 100.00
    }
  ],
  "totalAmount": 200.00,
  "appliedCampaignId": 2,
  "discountAmount": 20.00,
  "shippingCost": 0.00,
  "finalAmount": 180.00,
  "createdAt": "2026-08-16T15:30:00.000Z"
}
```
