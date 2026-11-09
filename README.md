Hotel Booking System

Микросервистное риложение для бронирования номеров в отелях на выбранные даты. 
Язык: Java
Фреймворк: Spring Boot

Использование:

Система состоит из четырёх микросервисов:

eureka-server-----------Регистрация и обнаружение микросервисов------------8761
api-gateway-------------Единая точка входа для HTTP-запросов---------------8080
booking-service---------Создание и управление бронированиями---------------8081
hotel-service-----------Управление отелями и номерами----------------------8082

Каждый микросервис является отдельным Maven-проектом и запускается независимо.
Сначала запускается eureka-server, затем booking-service, hotel-service, затем api-gateway.
Api-gateway предоставляет единую точку доступа к остальным микросервисам. Они размещаются на http://localhost:8080

Проверка состояния системы:

Eureka Dashboard:
http://localhost:8761

Gateway Health:
http://localhost:8080/actuator/health

Gateway Mappings:
http://localhost:8080/actuator/mappings

Создание записей:

Все запросы должны быть направлены на http://localhost:8080. Если в ответ возрващается ошибка - 
необходимо отправлять запрос на http://localhost:8* в соответствии с системой, в которую направляется запрос.

Создание отеля:

Обрабатывает система Hotel Service. Пример запроса:

POST http://localhost:8080/api/hotels

Content-Type: application/json

{
  "name": "Grand Hotel",
  "address": "ул. Пушкина, 1"
}

Создаие номера:

Обрабатывает система Hotel Service. Пример запроса:

POST http://localhost:8080/api/rooms

Content-Type: application/json

{
  "hotelId": 1,
  "number": "101"
}

Создание пользователя:

Обрабатывает система Booking Service. Пример запроса:

POST http://localhost:8080/api/user/register

Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}

Создание брони:

Обрабатывает система Booking Service. Пример запроса:

POST http://localhost:8080/api/booking

Content-Type: application/json
Authorization: Bearer <токен_пользователя>

{
  "autoSelect": true,
  "startDate": "2026-12-01",
  "endDate": "2026-12-05"
}

Система автоматически подберёт свободный номер, отдав предпочтение тем, которые давно не бронировали. 
При попытке забронировать уже занятый номер на пересекающиеся даты сервис возвращает 409 Conflict.

Получение списка отелей:

GET http://localhost:8080/api/hotels



Какие эндпойнты поддерживаются:

http://localhost:8761
http://localhost:8080/actuator/health
http://localhost:8080/actuator/mappings
http://localhost:8080/api/booking
http://localhost:8080/api/user/register
http://localhost:8080/api/rooms
http://localhost:8080/api/hotels



Как протестировать код:


Запустите сервисы, создайте отели, номера и пользователей и попробуйте забронировать места. 
Для тестирования конфликта можно использовать запросы вида:

POST http://localhost:8080/api/booking

Content-Type: application/json
Authorization: Bearer <токен_пользователя>

{
    "autoSelect": false,
    "roomId": 1,
    "startDate": "2026-12-01",
    "endDate": "2026-12-05"
}
http://localhost:8080/actuator/mappings
API Gateway используется как единая точка входа для клиентских запросов.
