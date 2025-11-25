

<p align="center">

  <img width="128" height="128" src="https://github.com/MironovNikita/BestBank/blob/main/images/logo.png">

</p>

# 🏦 Микросервисное приложение Best Bank
Это микросервисное веб-приложение — банк, реализованный на Java 21 с использованием Spring Framework версии 6.1 и выше (со Spring Boot), при написании которого использовались следующие фреймворки: WebFlux, Security, Cloud, R2DBC и др. Проект управляется с помощью системы сборки Gradle. Применение вышеуказанных фреймворков позволило реализовать полностью неблокирующее взаимодействие с базами данных и построить высокопроизводительное масштабируемое веб-приложение, предусматривающее аутентификацию и ограничение доступа к API (логин/пароль). Также в проекте предусмотрено развёртывание в контейнерах Docker с применением Liquibase-скриптов для БД.

## 📝 Описание
Приложение состоит из семи "внутренних" модулей и 1 "внешнего":
- 🛠️ config-service;
- 🔍 discovery-service;
- 📝 accounts-service;
- 💵 cash-service;
- 💸 transfers-service;
- 📩 notification-service;
- 🖥️ front-ui.
-  keycloak
а также "внешнего" вспомогательного модуля:
- 🔐 keycloak - модуль аутентификации между микросервисами (OAuth2 протокол разделяет роли клиента, владельца ресурса, сервера авторизации и сервера ресурсов).

**Сonfig-service**.
Сервис, являющийся поставщиком конфигураций для остальных микросервисов. Ознакомиться с конфигурациями можно [**тут**](https://github.com/MironovNikita/BestBank/blob/main/config-service/config). В приложении настроен на порту 8888.

**Discover-service**.
Это ключевой микросервис, реализующий паттерн Service Discovery в микросервисной архитектуре.
- основные функции и роль discovery-service:
- обеспечивает автоматическую регистрацию всех микросервисов при их запуске;
- позволяет динамически отслеживать доступность и статус сервисов (активен/неактивен);
- предоставляет централизованный реестр, где каждый сервис может найти актуальные адреса и порты других сервисов без жёстких настроек;
- поддерживает динамическое масштабирование — добавление и удаление экземпляров сервисов отражается в реестре мгновенно;
- улучшает отказоустойчивость, позволяя клиентам и балансировщикам нагрузки направлять запросы только на живые экземпляры.
В приложении настроен на порту 8761 с использованием Eureka Server.

**Accounts-service**
Сервис, являющийся основным для приложения. В нём содержится база данных по аккаунтам. Также он отвечает за логику проверки credentials, поступающих от front-ui при логине, проверка баланса, и т.д. Любое изменение с аккаунтом также находится в его зоне ответственности. В приложении настроен на порту 8081.

**Cash-service**
Сервис, отвечающий за увеличение/уменьшение баланса пользователя за счёт наличных: снять/положить. Имеет свою собственную базу данных, где хранится история, какой пользователь, когда и какую операцию с наличностью совершал. Напрямую доступа к БД accounts-service не имеет, обращается к ней через данный сервис. В приложении настроен на порту 8083.

**Transfers-service**
Сервис, отвечающий за перевод средств другим пользователям со своего счёта. По аналогии с cash-service имеет собственную БД, в которой ведётся "история", кто, когда, кому и сколько переводил. Также не имеет прямого доступа к БД accounts-service. Обращается за нужными данными через сервис. В приложении настроен на порту 8082.

**Notification-service**
Сервис, отвечающий за отправку уведомлений. В данном приложении настроена отправка уведомлений через электронную почту. В качестве сервера smtp используется Яндекс. Подробнее можно посмотреть в конфиге. Уведомления предусмотрены для:
- приветствия пользователя при регистрации;
- при изменении данных аккаунта;
- при изменении пароля;
- при операциях с наличными;
- при переводе средств.
В приложении настроен на порту 8084.

**Front-ui**
Сервис, являющийся "лицом" приложения. Имеет три страницы:
- страница регистрации;
- страница логина;
- главная страница - личный кабинет, где пользователь может совершать какие-либо операции. В приложении настроен на порту 8085.

Благодаря keycloak реализовано межсервисное взаимодействие по OAuth2. Для этого был реализован realm, ознакомиться [**тут**](https://github.com/MironovNikita/BestBank/blob/main/common/src/main/java/com/bank/keycloak/bank-app.json). Запускается на порту 8080.

Стоит упомянуть, что в приложении есть также **common-модуль**, который содержит в себе общие для микросервисов DTO и другие классы.

Для хранения данных используется БД PostgreSQL 17 версии. Для интеграционного тестирования применяется технология TestContainers, которая позволяет для тестирования поднять БД аналогичную основной.
Приложение покрыто unit и интеграционными тестами с использованием JUnit 5 и Spring TestContext Framework, с применением кэширования контекстов. Также применяются контрактные тесты, основанные на Spring CLoud Contract.

Примеры таблиц баз данных выглядят так:

База данных аккаунтов:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/accountsTable.png">

</p>

База данных операций с наличными:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/cashTable.png">

</p>

База данных переводов:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/transfersTable.png">

</p>

### ⚠️ Важно ⚠️
Все таблицы в БД приложения создаются и заполняются посредством Liquibase-скриптов. Ознакомиться с ними можно:
- [**accounts-service**](https://github.com/MironovNikita/BestBank/blob/main/accounts-service/src/main/resources/db/liquibase);
- [**cash-service**](https://github.com/MironovNikita/BestBank/blob/main/cash-service/src/main/resources/db/liquibase);
- [**transfers-service**](https://github.com/MironovNikita/BestBank/blob/main/transfers-service/src/main/resources/db/liquibase).

### 🚀 Запуск программы

1) Установить БД [**PostgreSQL**](https://www.postgresql.org/download/);
2) Установить Gradle;
3) Скачать проект;
4) В консоли Gradle выполнить команду **`gradle clean build`**;
5) Запустить Docker Desktop;
6) Через командную строку запустить Keycloak: 
```
docker run -d -p 8080:8080 --name keycloak \
-e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
-e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
quay.io/keycloak/keycloak:26.1.3 start-dev
```
7) Добавить в него realm из common-модуля.
8) Запустить все микросервисы через IDEA:
config-service -> discovery-service -> (accounts-service, cash-service, transfers-service, notification-service) -> front-ui.
8) Перейти на **http://localhost:8085/best-bank/**;
9) Пользоваться :)

Логи модулей:
```java
//Для Config-Service
2025-11-25T12:20:30.810+03:00  INFO 21080 --- [config-service] [           main] com.bank.ConfigServiceApp                : Started ConfigServiceApp in 2.262 seconds (process running for 2.772)
2025-11-25T12:20:31.238+03:00  INFO 21080 --- [config-service] [3)-172.23.176.1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-11-25T12:20:31.238+03:00  INFO 21080 --- [config-service] [3)-172.23.176.1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-11-25T12:20:31.240+03:00  INFO 21080 --- [config-service] [3)-172.23.176.1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms
2025-11-25T12:20:44.291+03:00  INFO 21080 --- [config-service] [nio-8888-exec-1] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: Config resource 'file [config-service\config\discovery-service.properties]' via location 'file:config-service/config/'
2025-11-25T12:21:01.307+03:00  INFO 21080 --- [config-service] [nio-8888-exec-4] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: Config resource 'file [config-service\config\accounts-service.properties]' via location 'file:config-service/config/'
2025-11-25T12:21:07.555+03:00  INFO 21080 --- [config-service] [nio-8888-exec-9] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: Config resource 'file [config-service\config\cash-service.properties]' via location 'file:config-service/config/'
2025-11-25T12:21:12.869+03:00  INFO 21080 --- [config-service] [nio-8888-exec-5] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: Config resource 'file [config-service\config\transfers-service.properties]' via location 'file:config-service/config/'
2025-11-25T12:21:16.064+03:00  INFO 21080 --- [config-service] [nio-8888-exec-2] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: Config resource 'file [config-service\config\notification-service.properties]' via location 'file:config-service/config/'
2025-11-25T12:21:19.408+03:00  INFO 21080 --- [config-service] [nio-8888-exec-7] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: Config resource 'file [config-service\config\front-ui.properties]' via location 'file:config-service/config/'

//Discover-service
2025-11-25 - 12:20:47.755 (+03:00)  INFO 19028 ---> [main] com.bank.DiscoveryServiceApp : Started DiscoveryServiceApp in 4.538 seconds (process running for 5.081)
2025-11-25 - 12:20:48.123 (+03:00)  INFO 19028 ---> [RMI TCP Connection(1)-172.23.176.1] o.a.c.c.C.[Tomcat].[localhost].[/] : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-11-25 - 12:20:48.124 (+03:00)  INFO 19028 ---> [RMI TCP Connection(1)-172.23.176.1] o.s.web.servlet.DispatcherServlet : Initializing Servlet 'dispatcherServlet'
2025-11-25 - 12:20:48.125 (+03:00)  INFO 19028 ---> [RMI TCP Connection(1)-172.23.176.1] o.s.web.servlet.DispatcherServlet : Completed initialization in 1 ms
2025-11-25 - 12:21:06.336 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-2] c.n.e.r.AbstractInstanceRegistry : Registered instance ACCOUNTS-SERVICE/---.mshome.net:accounts-service:8081 with status UP (replication=false)
2025-11-25 - 12:21:06.971 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-3] c.n.e.r.AbstractInstanceRegistry : Registered instance ACCOUNTS-SERVICE/---.mshome.net:accounts-service:8081 with status UP (replication=true)
2025-11-25 - 12:21:12.862 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-5] c.n.e.r.AbstractInstanceRegistry : Registered instance CASH-SERVICE/---.mshome.net:cash-service:8083 with status UP (replication=false)
2025-11-25 - 12:21:13.394 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-6] c.n.e.r.AbstractInstanceRegistry : Registered instance CASH-SERVICE/---.mshome.net:cash-service:8083 with status UP (replication=true)
2025-11-25 - 12:21:18.081 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-8] c.n.e.r.AbstractInstanceRegistry : Registered instance TRANSFERS-SERVICE/---.mshome.net:transfers-service:8082 with status UP (replication=false)
2025-11-25 - 12:21:18.612 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-9] c.n.e.r.AbstractInstanceRegistry : Registered instance TRANSFERS-SERVICE/---.mshome.net:transfers-service:8082 with status UP (replication=true)
2025-11-25 - 12:21:18.988 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-1] c.n.e.r.AbstractInstanceRegistry : Registered instance NOTIFICATION-SERVICE/---.mshome.net:notification-service:8084 with status UP (replication=false)
2025-11-25 - 12:21:19.503 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-2] c.n.e.r.AbstractInstanceRegistry : Registered instance NOTIFICATION-SERVICE/---.mshome.net:notification-service:8084 with status UP (replication=true)
2025-11-25 - 12:21:22.347 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-4] c.n.e.r.AbstractInstanceRegistry : Registered instance FRONT-UI/---.mshome.net:front-ui:8085 with status UP (replication=false)
2025-11-25 - 12:21:22.866 (+03:00)  INFO 19028 ---> [http-nio-8761-exec-5] c.n.e.r.AbstractInstanceRegistry : Registered instance FRONT-UI/---.mshome.net:front-ui:8085 with status UP (replication=true)


//Accounts-service
2025-11-25 - 12:21:06.265 (+03:00)  INFO 17808 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_ACCOUNTS-SERVICE/---.mshome.net:accounts-service:8081: registering service...
2025-11-25 - 12:21:06.341 (+03:00)  INFO 17808 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_ACCOUNTS-SERVICE/---.mshome.net:accounts-service:8081 - registration status: 204
2025-11-25 - 12:21:06.399 (+03:00)  INFO 17808 ---> [main] o.s.b.w.e.netty.NettyWebServer : Netty started on port 8081 (http)
2025-11-25 - 12:21:06.400 (+03:00)  INFO 17808 ---> [main] o.s.c.n.e.s.EurekaAutoServiceRegistration : Updating port to 8081
2025-11-25 - 12:21:06.414 (+03:00)  INFO 17808 ---> [main] com.bank.AccountsServiceApp : Started AccountsServiceApp in 5.572 seconds (process running for 6.16)
2025-11-25 - 12:21:36.262 (+03:00)  INFO 17808 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Disable delta property : false
2025-11-25 - 12:21:36.262 (+03:00)  INFO 17808 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Single vip registry refresh property : null
2025-11-25 - 12:21:36.262 (+03:00)  INFO 17808 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Force full registry fetch : false
2025-11-25 - 12:21:36.262 (+03:00)  INFO 17808 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application is null : false
2025-11-25 - 12:21:36.262 (+03:00)  INFO 17808 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Registered Applications size is zero : true
2025-11-25 - 12:21:36.262 (+03:00)  INFO 17808 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application version is -1: false
2025-11-25 - 12:21:36.262 (+03:00)  INFO 17808 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Getting all instance registry info from the eureka server
2025-11-25 - 12:21:36.278 (+03:00)  INFO 17808 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : The response status is 200
2025-11-25 - 12:26:05.910 (+03:00)  INFO 17808 ---> [AsyncResolver-bootstrap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver : Resolving eureka endpoints via configuration

//Cash-service
2025-11-25 - 12:21:12.833 (+03:00)  INFO 18640 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_CASH-SERVICE/---.mshome.net:cash-service:8083: registering service...
2025-11-25 - 12:21:12.864 (+03:00)  INFO 18640 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_CASH-SERVICE/---.mshome.net:cash-service:8083 - registration status: 204
2025-11-25 - 12:21:12.924 (+03:00)  INFO 18640 ---> [main] o.s.b.w.e.netty.NettyWebServer : Netty started on port 8083 (http)
2025-11-25 - 12:21:12.925 (+03:00)  INFO 18640 ---> [main] o.s.c.n.e.s.EurekaAutoServiceRegistration : Updating port to 8083
2025-11-25 - 12:21:12.938 (+03:00)  INFO 18640 ---> [main] com.bank.CashServiceApp : Started CashServiceApp in 5.9 seconds (process running for 6.616)
2025-11-25 - 12:21:42.836 (+03:00)  INFO 18640 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Disable delta property : false
2025-11-25 - 12:21:42.836 (+03:00)  INFO 18640 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Single vip registry refresh property : null
2025-11-25 - 12:21:42.836 (+03:00)  INFO 18640 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Force full registry fetch : false
2025-11-25 - 12:21:42.836 (+03:00)  INFO 18640 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application is null : false
2025-11-25 - 12:21:42.836 (+03:00)  INFO 18640 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Registered Applications size is zero : true
2025-11-25 - 12:21:42.836 (+03:00)  INFO 18640 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application version is -1: false
2025-11-25 - 12:21:42.836 (+03:00)  INFO 18640 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Getting all instance registry info from the eureka server
2025-11-25 - 12:21:42.862 (+03:00)  INFO 18640 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : The response status is 200
2025-11-25 - 12:26:12.678 (+03:00)  INFO 18640 ---> [AsyncResolver-bootstrap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver : Resolving eureka endpoints via configuration

//Transfers-service
2025-11-25 - 12:21:18.036 (+03:00)  INFO 25884 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_TRANSFERS-SERVICE/---.mshome.net:transfers-service:8082: registering service...
2025-11-25 - 12:21:18.083 (+03:00)  INFO 25884 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_TRANSFERS-SERVICE/---.mshome.net:transfers-service:8082 - registration status: 204
2025-11-25 - 12:21:18.152 (+03:00)  INFO 25884 ---> [main] o.s.b.w.e.netty.NettyWebServer : Netty started on port 8082 (http)
2025-11-25 - 12:21:18.153 (+03:00)  INFO 25884 ---> [main] o.s.c.n.e.s.EurekaAutoServiceRegistration : Updating port to 8082
2025-11-25 - 12:21:18.170 (+03:00)  INFO 25884 ---> [main] com.bank.TransfersServiceApp : Started TransfersServiceApp in 5.793 seconds (process running for 6.351)
2025-11-25 - 12:21:48.032 (+03:00)  INFO 25884 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Disable delta property : false
2025-11-25 - 12:21:48.032 (+03:00)  INFO 25884 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Single vip registry refresh property : null
2025-11-25 - 12:21:48.032 (+03:00)  INFO 25884 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Force full registry fetch : false
2025-11-25 - 12:21:48.032 (+03:00)  INFO 25884 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application is null : false
2025-11-25 - 12:21:48.032 (+03:00)  INFO 25884 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Registered Applications size is zero : true
2025-11-25 - 12:21:48.032 (+03:00)  INFO 25884 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application version is -1: false
2025-11-25 - 12:21:48.032 (+03:00)  INFO 25884 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Getting all instance registry info from the eureka server
2025-11-25 - 12:21:48.058 (+03:00)  INFO 25884 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : The response status is 200
2025-11-25 - 12:26:17.828 (+03:00)  INFO 25884 ---> [AsyncResolver-bootstrap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver : Resolving eureka endpoints via configuration

//Notification-service
2025-11-25 - 12:21:18.950 (+03:00)  INFO 14948 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_NOTIFICATION-SERVICE/---.mshome.net:notification-service:8084: registering service...
2025-11-25 - 12:21:18.990 (+03:00)  INFO 14948 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_NOTIFICATION-SERVICE/---.mshome.net:notification-service:8084 - registration status: 204
2025-11-25 - 12:21:19.054 (+03:00)  INFO 14948 ---> [main] o.s.b.w.e.netty.NettyWebServer : Netty started on port 8084 (http)
2025-11-25 - 12:21:19.055 (+03:00)  INFO 14948 ---> [main] o.s.c.n.e.s.EurekaAutoServiceRegistration : Updating port to 8084
2025-11-25 - 12:21:19.069 (+03:00)  INFO 14948 ---> [main] com.bank.NotificationServiceApp : Started NotificationServiceApp in 3.481 seconds (process running for 4.052)
2025-11-25 - 12:21:48.945 (+03:00)  INFO 14948 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Disable delta property : false
2025-11-25 - 12:21:48.945 (+03:00)  INFO 14948 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Single vip registry refresh property : null
2025-11-25 - 12:21:48.945 (+03:00)  INFO 14948 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Force full registry fetch : false
2025-11-25 - 12:21:48.945 (+03:00)  INFO 14948 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application is null : false
2025-11-25 - 12:21:48.945 (+03:00)  INFO 14948 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Registered Applications size is zero : true
2025-11-25 - 12:21:48.945 (+03:00)  INFO 14948 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application version is -1: false
2025-11-25 - 12:21:48.945 (+03:00)  INFO 14948 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Getting all instance registry info from the eureka server
2025-11-25 - 12:21:48.972 (+03:00)  INFO 14948 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : The response status is 200
2025-11-25 - 12:26:18.616 (+03:00)  INFO 14948 ---> [AsyncResolver-bootstrap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver : Resolving eureka endpoints via configuration


//Front-UI
2025-11-25 - 12:21:22.318 (+03:00)  INFO 23784 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_FRONT-UI/---.mshome.net:front-ui:8085: registering service...
2025-11-25 - 12:21:22.348 (+03:00)  INFO 23784 ---> [DiscoveryClient-InstanceInfoReplicator-%d] c.netflix.discovery.DiscoveryClient : DiscoveryClient_FRONT-UI/---.mshome.net:front-ui:8085 - registration status: 204
2025-11-25 - 12:21:22.409 (+03:00)  INFO 23784 ---> [main] o.s.b.w.e.netty.NettyWebServer : Netty started on port 8085 (http)
2025-11-25 - 12:21:22.410 (+03:00)  INFO 23784 ---> [main] o.s.c.n.e.s.EurekaAutoServiceRegistration : Updating port to 8085
2025-11-25 - 12:21:22.423 (+03:00)  INFO 23784 ---> [main] com.bank.FrontUIApp : Started FrontUIApp in 3.459 seconds (process running for 4.158)
2025-11-25 - 12:21:52.326 (+03:00)  INFO 23784 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Disable delta property : false
2025-11-25 - 12:21:52.327 (+03:00)  INFO 23784 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Single vip registry refresh property : null
2025-11-25 - 12:21:52.327 (+03:00)  INFO 23784 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Force full registry fetch : false
2025-11-25 - 12:21:52.327 (+03:00)  INFO 23784 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application is null : false
2025-11-25 - 12:21:52.327 (+03:00)  INFO 23784 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Registered Applications size is zero : true
2025-11-25 - 12:21:52.327 (+03:00)  INFO 23784 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Application version is -1: false
2025-11-25 - 12:21:52.327 (+03:00)  INFO 23784 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : Getting all instance registry info from the eureka server
2025-11-25 - 12:21:52.353 (+03:00)  INFO 23784 ---> [DiscoveryClient-CacheRefreshExecutor-%d] c.netflix.discovery.DiscoveryClient : The response status is 200
2025-11-25 - 12:26:22.122 (+03:00)  INFO 23784 ---> [AsyncResolver-bootstrap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver : Resolving eureka endpoints via configuration
```

Для корректной работы с keycloak-контейнером была предусмотрена конфигурация [**realm**](https://github.com/MironovNikita/BestBank/blob/main/common/src/main/java/com/bank/keycloak/bank-app.json), которая устанавливает клиентов для корректного взаимодействия по OAuth2.0.

Так как в приложение была добавлена авторизация, то теперь, чтобы полноценно воспользоваться функционалом, необходимо зарегистрироваться. Каждая учётная запись подлежит ограничениям, ознакомиться с которыми можно в [**RegisterAccountRequest**](https://github.com/MironovNikita/BestBank/blob/main/common/src/main/java/com/bank/dto/account/RegisterAccountRequest.java).

При входе в приложение нас встречает страница авторизации:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/login.png">

</p>

Если введём неверные данные, программа нас не пропустит:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/loginFailed.png">

</p>

Можем перейти к регистрации:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/register.png">

</p>

### ❗️ Пользовательские данные
Стоит отдельно отметить, что часть пользовательских данных шифруется для хранения в БД. Это реализовано с помощью классов **PasswordEncoder** - для пароля (не предусматривает расшифровки обратно) и [**SecureBase64Converter**](https://github.com/MironovNikita/BestBank/blob/main/common/src/main/java/com/bank/security/SecureBase64Converter.java), который отвечает за шифрование email и номера телефона пользователя.

После регистрации можем смело заходить по нашим данным в приложение:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/registerSuccess.png">

</p>

Теперь мы можем воспользоваться полным функционалом.
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/main.png">

</p>

Изменим свои персональные данные: 
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/mainPersonal.png">

</p>

Снимем все деньги со счёта:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/mainCash.png">

</p>

Теперь попробуем сделать кому-нибудь перевод:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/mainTransfer.png">

</p>

Отрицательные числа не принимаем. Схитрить не получится :)
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/mainMinus.png">

</p>

Положим деньги на счёт:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/cashPlus.png">

</p>

Теперь сделаем перевод:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/transferPlus.png">

</p>

## 🔔 Уведомления

Уведомления, которые получает пользователь:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/notifications.png">

</p>

Внутри письмо лаконичного содержания:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/email.png">

</p>

## 🗒️ Логирование 🔍
В приложении также предусмотрено логирование. Логи пишутся непосредственно в консоль. Ниже приведён пример логов для ряда сервисов:
```java
//Accounts-service
2025-11-25 - 13:34:03.799 (+03:00)  INFO 17808 ---> [reactor-tcp-nio-4] com.bank.service.AccountServiceImpl : Успешное создание аккаунта с ID: 39
2025-11-25 - 13:34:04.816 (+03:00)  INFO 17808 ---> [reactor-http-nio-2] c.b.service.NotificationServiceImpl : Успешная отправка уведомления на почту: nikit1739@mail.ru
2025-11-25 - 13:35:44.205 (+03:00)  INFO 17808 ---> [reactor-tcp-nio-4] com.bank.service.AccountServiceImpl : Успешная проверка credentials для пользователя с email nikit1739@yandex.ru
2025-11-25 - 13:35:44.242 (+03:00)  INFO 17808 ---> [reactor-tcp-nio-3] com.bank.service.AccountServiceImpl : Был запрошен баланс для аккаунта с ID 38
2025-11-25 - 13:36:06.015 (+03:00)  INFO 17808 ---> [AsyncResolver-bootstrap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver : Resolving eureka endpoints via configuration
2025-11-25 - 13:37:15.835 (+03:00)  INFO 17808 ---> [reactor-tcp-nio-3] com.bank.service.AccountServiceImpl : Данные пользователя с ID 38 были успешно обновлены.
2025-11-25 - 13:37:15.849 (+03:00)  INFO 17808 ---> [reactor-tcp-nio-3] com.bank.service.AccountServiceImpl : Был запрошен баланс для аккаунта с ID 38
2025-11-25 - 13:37:16.695 (+03:00)  INFO 17808 ---> [reactor-http-nio-2] c.b.service.NotificationServiceImpl : Успешная отправка уведомления на почту: nikit1739@yandex.ru

//Cash-service
2025-11-25 - 13:38:24.622 (+03:00)  INFO 18640 ---> [reactor-tcp-nio-11] com.bank.service.CashServiceImpl : Операция с наличными для пользователя 38 выполнена.
2025-11-25 - 13:38:25.546 (+03:00)  INFO 18640 ---> [reactor-http-nio-2] c.b.s.NotificationsServiceClientImpl : Успешная отправка уведомления на почту: nikit1739@yandex.ru
2025-11-25 - 13:39:59.920 (+03:00) ERROR 18640 ---> [reactor-http-nio-2] c.b.c.e.GlobalExceptionHandler : Возникло ConstraintViolationException: Validation failed for argument at index 0 in method: public reactor.core.publisher.Mono<java.lang.Void> com.bank.controller.CashController.operateCash(com.bank.dto.cash.CashOperationDto), with 1 error(s): [Field error in object 'cashOperationDto' on field 'amount': rejected value [-5000]; codes [Positive.cashOperationDto.amount,Positive.amount,Positive.java.lang.Long,Positive]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [cashOperationDto.amount,amount]; arguments []; default message [amount]]; default message [Сумма не может быть отрицательной или 0]] 

//Transfers-service
2025-11-25 - 13:39:25.486 (+03:00) ERROR 25884 ---> [reactor-http-nio-2] c.b.s.AccountsServiceClientImpl : 4xx ошибка при переводе средств: Недостаточно средств или счёт не найден!
2025-11-25 - 13:39:25.488 (+03:00) ERROR 25884 ---> [reactor-http-nio-2] c.bank.service.TransfersServiceImpl : Ошибка перевода с ID 38 на ID 10: Недостаточно средств или счёт не найден!
2025-11-25 - 13:42:15.671 (+03:00)  INFO 25884 ---> [reactor-http-nio-2] c.b.s.NotificationsServiceClientImpl : Уведомление отправлено на nikit1739@yandex.ru
2025-11-25 - 13:42:15.675 (+03:00)  INFO 25884 ---> [reactor-tcp-nio-4] c.bank.service.TransfersServiceImpl : Перевод с ID 38 на ID 9 успешно сохранён.

//Front-UI
2025-11-25 - 13:31:28.666 (+03:00) ERROR 23784 ---> [reactor-http-nio-3] c.b.controller.main.MainController : 4хх ошибка при обращении (логин) к accounts-service: Ошибка авторизации. Введён неверный логин/пароль.
2025-11-25 - 13:36:22.216 (+03:00)  INFO 23784 ---> [AsyncResolver-bootstrap-executor-%d] c.n.d.s.r.aws.ConfigClusterResolver : Resolving eureka endpoints via configuration
2025-11-25 - 13:39:25.508 (+03:00) ERROR 23784 ---> [reactor-http-nio-3] c.b.c.transfers.TransfersController : 4хх ошибка при обращении (перевод средств) к accounts-service: Недостаточно средств или счёт не найден!
2025-11-25 - 13:39:59.928 (+03:00) ERROR 23784 ---> [reactor-http-nio-3] c.b.controller.cash.CashController : 4хх ошибка при обращении (операции с наличными) к cash-service: amount:Сумма не может быть отрицательной или 0
```

## ✅ Тестирование 🐞
Как говорилось ранее, для интеграционного тестирования предусмотрен TestContainer для БД (accounts-service). С его настройками можно ознакомиться [**здесь**](https://github.com/MironovNikita/BestBank/blob/main/accounts-service/src/test/java/com/bank/testDB).

В некоторых тестах предусмотрено заполнение данных непосредственно для определённого теста. За это отвечает утилитный класс [**DataInserter**](https://github.com/MironovNikita/BestBank/blob/main/accounts-service/src/test/java/com/bank/DataInserter.java) Пример одного из методов представлен ниже:
```java
public static Mono<Void> insertIntoAccountsTable(DatabaseClient client, Account account) {
        return client.sql(
                        "INSERT INTO accounts(id, email, password, name, surname, birthdate, phone, balance)" +
                                "VALUES(:id, :email, :password, :name, :surname, :birthdate, :phone, :balance)"
                )
                .bind("id", account.getId())
                .bind("email", account.getEmail())
                .bind("password", account.getPassword())
                .bind("name", account.getName())
                .bind("surname", account.getSurname())
                .bind("birthdate", account.getBirthdate())
                .bind("phone", account.getPhone())
                .bind("balance", account.getBalance())
                .then();
    }
```

Также предусмотрены контрактные тесты Spring Cloud Contract.
Они реализованы для:
- accounts-service: [**тут**](https://github.com/MironovNikita/BestBank/blob/main/accounts-service/src/contractTest/resources/contracts);
- cash-service: [**тут**](https://github.com/MironovNikita/BestBank/blob/main/cash-service/src/contractTest/resources/contracts);
- transfers-service: [**тут**](https://github.com/MironovNikita/BestBank/tree/develop/transfers-service/src/contractTest/resources/contracts);
- notification-service: [**тут**](https://github.com/MironovNikita/BestBank/tree/develop/notification-service/src/contractTest/resources/contracts).

Пример одного из groovy-скриптов:
```groovy
Contract.make {
    description("Should register account")
    request {
        method POST()
        url("/accounts/register") {}
        headers {
            contentType(applicationJson())
        }
        body(
                email: "test@test.ru",
                password: "Password1111",
                name: "Test",
                surname: "Test",
                birthdate: "1990-01-01",
                phone: "89996665522"
        )
    }
    response {
        status OK()
    }
}
```

Для их корректной работы необходимо сгенерировать stubs. После написания указанных выше скриптов и создания [**общего**](https://github.com/MironovNikita/BestBank/blob/main/accounts-service/src/test/java/com/bank/contract/BaseContractTestClass.java) класса необходимо ввести команду для сборки стабов:
```
./gradlew clean build publishToMavenLocal -p accounts-service -x test
./gradlew clean build publishToMavenLocal -p cash-service -x test
./gradlew clean build publishToMavenLocal -p transfers-service -x test
./gradlew clean build publishToMavenLocal -p notification-service -x test
```
Каждая команда соберёт стабы для API микросервиса с сохранением в локальный репозиторий:
<p align="center">

  <img src="https://github.com/MironovNikita/EasyBuy/blob/main/images/stubs.png">

</p>

Результат сборки проекта:
```java
//Для модуля accounts-service
--------------------------------------------------
Тестов всего: 30
Успешно:      30
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

ContractVerifierTest > validate_notRegistered() PASSED

ContractVerifierTest > validate_getBalance() PASSED

ContractVerifierTest > validate_editPassword() PASSED

ContractVerifierTest > validate_getOtherAccounts() PASSED

ContractVerifierTest > validate_editAccount() PASSED

ContractVerifierTest > validate_registered() PASSED

ContractVerifierTest > validate_transfer() PASSED
--------------------------------------------------
Тестов всего: 9
Успешно:      9
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

//Для модуля cash-service:
--------------------------------------------------
Тестов всего: 7
Успешно:      7
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

ContractVerifierTest > validate_operateCash() PASSED
--------------------------------------------------
Тестов всего: 1
Успешно:      1
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

//Для модуля transfers-service:
--------------------------------------------------
Тестов всего: 6
Успешно:      6
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

ContractVerifierTest > validate_operateTransfer() PASSED
--------------------------------------------------
Тестов всего: 1
Успешно:      1
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

//Для модуля notification-service:
--------------------------------------------------
Тестов всего: 3
Успешно:      3
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

ContractVerifierTest > validate_sendEmail() PASSED
--------------------------------------------------
Тестов всего: 1
Успешно:      1
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

//Для модуля front-ui (контрактные):
--------------------------------------------------
Тестов всего: 7
Успешно:      7
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

BUILD SUCCESSFUL in 1m 44s
93 actionable tasks: 93 executed
14:02:28: Execution finished 'clean build'.
```
