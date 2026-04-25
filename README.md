# JWT Authentication & Microservices Demo

Bu proje, JWT tabanlı authentication, refresh token mimarisi, role-based authorization, RabbitMQ ile event-driven communication, microservice mimarisi ve React frontend konularını kapsayan uçtan uca bir demo uygulamasıdır.

## Proje Yapısı

- `jwt-odev/` → Auth Service (Spring Boot)
- `notification-service/` → RabbitMQ Consumer Microservice
- `auth-ui/` → React Frontend

## Kullanılan Teknolojiler

### Backend

- Java 17+
- Spring Boot
- Spring Security
- JWT
- PostgreSQL
- RabbitMQ
- Docker

### Frontend

- React
- Vite
- Fetch API

## Özellikler

### Authentication

- Kullanıcı kayıt işlemi
- Kullanıcı giriş işlemi
- JWT Access Token üretimi
- Refresh Token üretimi
- Refresh Token'ın veritabanında saklanması
- Refresh Token rotation
- Logout ile token revoke işlemi

### Authorization

- ROLE_USER
- ROLE_ADMIN
- Protected endpoint yapısı
- Admin endpoint için rol bazlı erişim kontrolü

### Event Driven Architecture

- Kullanıcı register olduğunda RabbitMQ'ya event gönderilir
- Notification Service bu eventi dinler
- Event alındığında console üzerinde mail gönderilmiş gibi log basılır

### Frontend

- Register formu
- Login formu
- Token yönetimi
- Protected endpoint testleri
- Admin endpoint testleri

## Docker Servisleri

### PostgreSQL

Windows CMD / PowerShell için tek satır komut:

```bash
docker run --name secure-shop-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=12345 -e POSTGRES_DB=secure_shop_auth -p 5432:5432 -d postgres:16
```

### RabbitMQ

Windows CMD / PowerShell için tek satır komut:

```bash
docker run --name secure-shop-rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
```

RabbitMQ Management Panel:

```text
http://localhost:15672
username: guest
password: guest
```

## Servisleri Çalıştırma

### 1. Auth Service

```bash
cd jwt-odev
./mvnw spring-boot:run
```

Windows için:

```bash
cd jwt-odev
mvnw spring-boot:run
```

Auth Service varsayılan port:

```text
http://localhost:8083
```

### 2. Notification Service

```bash
cd notification-service
./mvnw spring-boot:run
```

Windows için:

```bash
cd notification-service
mvnw spring-boot:run
```

Notification Service varsayılan port:

```text
http://localhost:8084
```

### 3. Frontend

```bash
cd auth-ui
npm install
npm run dev
```

Frontend varsayılan adres:

```text
http://localhost:5173
```

## Sistem Akışı

### Register Akışı

```text
React Frontend
      |
      v
Auth Service
      |
      v
PostgreSQL
      |
      v
RabbitMQ
      |
      v
Notification Service
```

Kullanıcı register olduğunda önce kullanıcı veritabanına kaydedilir. Daha sonra RabbitMQ'ya `USER_REGISTERED` mantığında bir event gönderilir. Notification Service bu eventi dinleyerek kullanıcıya hoş geldin maili gönderilmiş gibi console'a log basar.

### Login Akışı

```text
React Frontend
      |
      v
Auth Service
      |
      v
Access Token + Refresh Token
```

Kullanıcı login olduğunda sistem access token ve refresh token üretir. Access token API isteklerinde kullanılır. Refresh token ise access token süresi dolduğunda yeni token üretmek için kullanılır.

### Protected Endpoint Akışı

```text
Frontend
      |
      v
Authorization: Bearer AccessToken
      |
      v
JwtTokenFilter
      |
      v
Protected Endpoint
```

Frontend, access token'ı `Authorization` header içerisinde backend'e gönderir. Backend tarafında JWT filter token'ı doğrular ve kullanıcı yetkilerini SecurityContext'e ekler.

### Refresh Token Akışı

```text
Refresh Token
      |
      v
DB kontrolü
      |
      v
Eski token revoked=true
      |
      v
Yeni Access Token + Yeni Refresh Token
```

Refresh token DB'de kontrol edilir. Token geçerliyse eski refresh token iptal edilir ve yeni access token ile yeni refresh token üretilir.

## Endpointler

### Auth Service

| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/login/register` | Yeni kullanıcı oluşturur |
| POST | `/login` | Kullanıcı girişi yapar |
| POST | `/login/refresh-token` | Refresh token ile yeni token üretir |
| POST | `/login/logout` | Refresh token'ı revoke eder |
| GET | `/hello` | Token gerektiren protected endpoint |
| GET | `/admin` | Sadece ROLE_ADMIN yetkisine sahip kullanıcılar erişebilir |

## Test Senaryoları

### Register

Frontend üzerinden veya Postman ile kullanıcı oluşturulur.

Örnek request:

```json
{
  "username": "serdar",
  "email": "serdar@example.com",
  "password": "123"
}
```

Beklenen sonuç:

```text
User registered successfully
```

### Login

Örnek request:

```json
{
  "username": "serdar",
  "password": "123"
}
```

Beklenen response:

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

### Protected Endpoint

Access token ile `/hello` endpointine istek atılır.

Header:

```text
Authorization: Bearer ACCESS_TOKEN
```

### Admin Endpoint

Normal kullanıcı `/admin` endpointine erişemez ve `403 Forbidden` alır. ROLE_ADMIN yetkisine sahip kullanıcı `/admin` endpointine erişebilir.

## Admin Rolü Atama

Admin rolü veritabanı üzerinden atanabilir.

Önce roller kontrol edilir:

```sql
SELECT * FROM roles;
```

ROLE_ADMIN yoksa eklenir:

```sql
INSERT INTO roles(name) VALUES ('ROLE_ADMIN');
```

Kullanıcı ve rol ilişkisi eklenir:

```sql
INSERT INTO user_roles(user_id, role_id) VALUES (USER_ID, ROLE_ID);
```

Kullanıcıya admin rolü verildikten sonra tekrar login olunmalı ve yeni access token alınmalıdır.

## Veritabanı Yapısı

Projede temel olarak şu tablolar kullanılır:

- `users`
- `roles`
- `user_roles`
- `refresh_tokens`

### users

Kullanıcı bilgilerini tutar.

### roles

Sistemdeki rolleri tutar.

Örnek roller:

- ROLE_USER
- ROLE_ADMIN

### user_roles

User ve Role arasındaki many-to-many ilişkiyi tutar.

### refresh_tokens

Refresh token kayıtlarını tutar. Token'ın hangi kullanıcıya ait olduğu, expire tarihi ve revoked durumu burada saklanır.

## RabbitMQ Yapısı

Auth Service register işleminden sonra RabbitMQ'ya event gönderir.

Kullanılan yapılar:

```text
Exchange: user.exchange
Queue: user.register.queue
Routing Key: user.register
```

Notification Service `user.register.queue` kuyruğunu dinler.

## Güvenlik Yaklaşımı

Bu projede password bilgileri açık şekilde saklanmaz. Kullanıcı şifreleri BCrypt ile hashlenerek veritabanına kaydedilir.

JWT access token kısa ömürlüdür. Refresh token daha uzun ömürlüdür ve veritabanında saklanır.

Refresh token kullanıldığında eski token revoke edilir ve yeni refresh token üretilir. Bu yaklaşıma refresh token rotation denir.

Logout işleminde ilgili refresh token `revoked=true` yapılır.

## Frontend Token Yönetimi

Frontend tarafında access token ve refresh token localStorage üzerinde saklanır. Protected endpoint isteklerinde access token şu şekilde gönderilir:

```text
Authorization: Bearer ACCESS_TOKEN
```

## Notlar

- JWT doğrulama stateless çalışır.
- Refresh token DB üzerinden yönetilir.
- Role-based authorization uygulanmıştır.
- RabbitMQ ile servisler arası event-driven iletişim kurulmuştur.
- React frontend backend servisleri ile entegre çalışır.

## Geliştirilebilir Alanlar

- React Router ile sayfa ayrımı
- Axios interceptor ile otomatik token yenileme
- Gerçek email gönderimi
- Docker Compose ile tüm servisleri tek komutla ayağa kaldırma
- Redis ile token blacklist yönetimi
- Rate limiting
- Email verification
- Password reset flow
- Kubernetes deployment


