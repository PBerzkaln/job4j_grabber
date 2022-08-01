# Проект - Grabber
[![Build Status](https://www.travis-ci.com/ViyChel/job4j_grabber.svg?branch=master)](https://www.travis-ci.com/ViyChel/job4j_grabber)

### Описание
Проект представляет собой парсер вакансий. Парсинг вакансий осуществляется с сайта: https://www.sql.ru/.
Распарсенные вакансии сохраняются в БД PostgreSQL.

Система запускается по расписанию, (данная функция реализована с помощью планировщика заданий Quartz), т. е. по истечению каждого периода запуска парсятся 5 страницы сайта вакансий и с помощью JDBC складируются в БД.<br>
Период запуска указывается в настройках - в файле app.properties (параметр time).
Доступ к интерфейсу сайта осуществляется через REST API с помощью библиотеки Jsoup (парсинг HTML).

### Технологии
- Java 13
- JDBC
- PostgreSQL
- Jsoup
- Планировщик заданий Quartz
- Sockets
- Maven
- JaCoCo
- Travis CI
- SLF4J


__Результат парсинга в БД__<br>
![img](img/list.png)

__Структура таблицы БД__<br>
![img](img/db.png)


#### Контакты

[![Linkedin](https://img.shields.io/badge/-linkedin-283e4a?style=flat&logo=linkedin&logoColor=white)](https://)&nbsp;
[![Telegram](https://img.shields.io/badge/-telegram-grey?style=flat&logo=telegram&logoColor=white)](https://)&nbsp;
[![Email](https://img.shields.io/badge/@%20email-005FED?style=flat&logo=mail&logoColor=white)](mailto:)&nbsp;