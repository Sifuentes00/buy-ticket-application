# Cinema Application

## Описание
**Cinema Application** — это сложное веб-приложение, разработанное на **Spring Boot**, которое позволяет управлять данными о фильмах. Пользователи могут получать информацию о фильмах через **REST API**, используя как параметры запроса, так и параметры пути.

## Требования
- **Java 17**
- **Spring Boot**
- **Spring MVC**
- **JUnit 5**
- **MockMvc**
- **Maven**

## Использование

Вы можете получить информацию о фильме, отправив GET-запросы по следующим адресам:

1. С использованием параметров запроса:
http://localhost:8080/api/query?title=Inception&director=Nolan&releaseYear=2010&genre=Sci-Fi
2. С использованием параметров пути:
http://localhost:8080/api/path/Inception/Nolan/2010/Sci-Fi

Также подключена база данных MySQL и реализованы CRUD операции

## Пример ответа GET-запроса
json
{
"title": "Inception",
"director": "Nolan",
"releaseYear": 2010,
"genre": "Sci-Fi"
}

## Установка и запуск
1. Клонируйте репозиторий:
git clone https://github.com/Sifuentes00/Cinema.git
2. Перейдите в директорию проекта:
cd Sifuentes00/Cinema
3. Соберите проект с помощью Maven:
mvn clean install
4. Запустите приложение:
mvn spring-boot:run

