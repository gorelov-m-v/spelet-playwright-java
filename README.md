# ✨ Фокус на Developer Experience (DX): Кастомные аннотации как DSL

Фреймворк спроектирован так, чтобы написание тестов было простым, читаемым и безопасным. Вместо того чтобы заставлять разработчиков использовать сложные комбинации стандартных аннотаций, был создан собственный DSL (Domain-Specific Language). Он скрывает внутреннюю сложность фреймворка и библиотек, предоставляя чистый и декларативный API.

---

## 1. `@Suite` — Человекочитаемые отчеты

**Проблема:**  
Стандартная интеграция Allure с JUnit 5 использует полное имя тестового класса (например, [`tests.casino.MultilingualNavigationTest`](src/test/java/tests/MultilingualNavigationTest.java)) в качестве названия для "сьюта" (Test Suite). Это делает отчеты технически перегруженными и затрудняет их понимание для нетехнических специалистов.

**Решение:**  
Создана кастомная аннотация [`@Suite`](src/main/java/com/example/testsupport/framework/allure/Suite.java), которая работает в паре с JUnit-расширением [`CustomSuiteExtension`](src/main/java/com/example/testsupport/framework/allure/CustomSuiteExtension.java).

**Как это работает:**  
Перед запуском каждого теста расширение `CustomSuiteExtension` проверяет наличие аннотации `@Suite` у тестового класса. Если она есть, расширение программно заменяет стандартный `suite` label в Allure на значение из аннотации, делая отчеты бизнес-ориентированными.

**Как применять:**

```java
// Добавляем аннотацию над тестовым классом
@Suite("Навигация и базовый флоу казино")
class MultilingualNavigationTest extends BaseTest {
    // ...
}
```

*Результат в отчете: вместо `tests.casino.MultilingualNavigationTest` будет `Навигация и базовый флоу казино`.*

---

## 2. Retries — Gradle Test Retry

Перезапуски нестабильных тестов выполняет [Gradle Test Retry Plugin](https://github.com/gradle/test-retry-gradle-plugin).
Количество повторов, лимит фейлов и поведение при "позеленевших" тестах задаются в `gradle.properties`:

```
retry.maxRetries=2
retry.maxFailures=0
retry.failOnPassedAfterRetry=false
```

Плагин автоматически повторяет упавшие тесты без дополнительных аннотаций. Артефакты каждой попытки получают суффикс `_attempt-N-of-M`, что упрощает диагностику.

> Для ретраев отдельных аргументов параметризованного теста необходимо:
> - Использовать единую версию JUnit через `enforcedPlatform('org.junit:junit-bom:5.12.2')`.
> - Указывать `{index}` в `@ParameterizedTest(name = ...)`.
> - Обеспечить стабильные строковые представления аргументов (например, через `Named` или детерминированный `toString()`).

---

## 3. Аннотации для декларативного API-клиента

**Проблема:**  
Вызов API-методов с множеством необязательных параметров через стандартный Feign приводит к коду с большим количеством `null`, что нечитаемо и хрупко.

**Решение:**  
Реализован паттерн Builder в связке с универсальным Feign-интерцептором и кастомными аннотациями для описания параметров запроса.

Аннотации: [`@RequestQueryParam`](src/main/java/com/example/testsupport/framework/api/client/annotations/RequestQueryParam.java), [`@RequestHeaderParam`](src/main/java/com/example/testsupport/framework/api/client/annotations/RequestHeaderParam.java)
Перехватчик: [`GenericParamsInterceptor`](src/main/java/com/example/testsupport/framework/api/client/GenericParamsInterceptor.java)

**Как это работает (пошагово):**

### Шаг 1: Описываем параметры запроса с помощью DTO и аннотаций

Для каждого API-эндпоинта создается POJO-класс, поля которого представляют все возможные параметры и помечаются нашими аннотациями.

```java
@Getter
@Builder
public class GamblingBrandsParams {
    // Это поле будет преобразовано в HTTP-заголовок "Platform-Locale"
    @RequestHeaderParam("Platform-Locale")
    private String platformLocale;
    // Это поле станет query-параметром "?categoryAlias=..."
    @RequestQueryParam("categoryAlias")
    private String categoryAlias;
}
```

### Шаг 2: Создаем лаконичный Feign-интерфейс

Метод клиента теперь принимает всего один DTO-объект.

[`FrontApiClient.java`](src/test/java/com/example/testsupport/framework/api/client/FrontApiClient.java)

```java
@FeignClient(name = "frontApiClient", configuration = GenericParamsInterceptor.class)
public interface FrontApiClient {
    @GetMapping("/gambling/brands")
    GamblingBrandsResponse getGamblingBrands(@RequestParam("params") GamblingBrandsParams params);
}
```

### Шаг 3: Используем Builder в тестах

Вместо передачи `null` мы декларативно конструируем запрос.

**Было (хрупко и нечитаемо):**

```java
// Легко ошибиться, передавая null для всех необязательных параметров.
GamblingBrandsResponse response = frontApiClient.getGamblingBrands(
    null, "lv", null, null, "new");
```

**Стало (декларативно и безопасно):**

```java
// Создаем запрос, указывая параметры по имени. Код самодокументируемый.
var params = GamblingBrandsParams.builder()
        .platformLocale("lv")
        .categoryAlias("new")
        .build();
GamblingBrandsResponse response = frontApiClient.getGamblingBrands(params);
```

### Шаг 4: Магия "под капотом"

[`GenericParamsInterceptor`](src/main/java/com/example/testsupport/framework/api/client/GenericParamsInterceptor.java) перехватывает запрос и, используя рефлексию, динамически добавляет в него только те параметры и заголовки, которые были заданы в билдере.

---

## 4. 📞 Декларативный API-клиент на OpenFeign

Для взаимодействия с API и подготовки состояний перед UI-тестами в фреймворке реализован мощный и удобный HTTP-клиент на базе Spring Cloud OpenFeign. Этот подход позволяет описывать API как простые Java-интерфейсы, полностью абстрагируясь от низкоуровневой реализации HTTP-запросов.

### 1. Конфигурация клиента

Вся настройка клиента централизована и управляется Spring.

**Включение Feign:** В главной тестовой конфигурации `TestConfig.java` активируется сканер Feign-клиентов:

```java
@Configuration
@EnableFeignClients(basePackages = "com/example/testsupport/framework/api/client")
public class TestConfig { /* ... */ }
```

**Настройка HTTP-слоя и логирования:** В классе `AllureFeignLoggerConfig.java` определяется HTTP-клиент (OkHttp) и регистрируется кастомный логгер, который детально записывает каждый запрос и ответ в Allure-отчет.

```java
@Configuration
public class AllureFeignLoggerConfig {
    @Bean
    public Client feignClient() {
        // Используем надежный OkHttp с таймаутами и пулом соединений
        return new OkHttpClient(...);
    }

    @Bean
    public FeignBuilderCustomizer allureFeignLoggerCustomizer(...) {
        // Подключаем логгер, который аттачит запросы/ответы к Allure
        return builder -> builder.logger(new AllureFeignLogger(...));
    }
}
```

### 2. Описание API-эндпоинта

Создание нового API-вызова сводится к описанию его в нескольких декларативных классах, без написания императивной логики.

**Шаг 1: Описываем DTO для ответа**

Это простые record или POJO, которые точно повторяют структуру JSON-ответа от сервера.

[`GamblingBrandsResponse.java`](src/test/java/com/example/testsupport/framework/api/dto/gambling/GamblingBrandsResponse.java) и
[`Brand.java`](src/test/java/com/example/testsupport/framework/api/dto/gambling/Brand.java)

```java
public record GamblingBrandsResponse(List<Brand> brands) {}

public record Brand(
        String id,
        String name,
        String alias,
        String icon,
        String logo,
        String colorLogo
) {}
```

**Шаг 2: Описываем параметры запроса с помощью DTO и аннотаций**

Для каждого API-эндпоинта создается POJO-класс, поля которого представляют все возможные параметры и помечаются кастомными аннотациями `@RequestQueryParam` и `@RequestHeaderParam`.

[`GamblingBrandsParams.java`](src/test/java/com/example/testsupport/framework/api/client/params/GamblingBrandsParams.java)

```java
@Getter
@Builder
public class GamblingBrandsParams {
    @RequestHeaderParam("Platform-Locale")
    private String platformLocale;

    @RequestQueryParam("categoryAlias")
    private String categoryAlias;
}
```

**Шаг 3: Описываем метод в интерфейсе клиента**

В главном интерфейсе `FrontApiClient.java` добавляется новый метод. Он принимает всего один DTO-объект с параметрами. Вся магия по сборке реального HTTP-запроса инкапсулирована в универсальном перехватчике `GenericParamsInterceptor`.

[`FrontApiClient.java`](src/test/java/com/example/testsupport/framework/api/client/FrontApiClient.java)

```java
@FeignClient(name = "...", configuration = GenericParamsInterceptor.class)
public interface FrontApiClient {
    @GetMapping("/_front_api/api/v1/gambling/brands")
    GamblingBrandsResponse getGamblingBrands(@RequestParam("params") GamblingBrandsParams params);
}
```

### 3. Использование в тесте

Благодаря паттерну Builder, вызов API в тесте становится чистым, декларативным и типобезопасным.

[`MultilingualNavigationTest.java`](src/test/java/tests/MultilingualNavigationTest.java)

```java
@Autowired
private FrontApiClient frontApiClient;

@Test
void apiIntegrationTest() {
    step("Получаем список брендов через API", () -> {
        // 1. Декларативно собираем запрос, указывая только нужные параметры
        var params = GamblingBrandsParams.builder()
                .platformLocale("lv")
                .categoryAlias("new")
                .build();

        // 2. Вызываем метод клиента
        GamblingBrandsResponse response = frontApiClient.getGamblingBrands(params);

        // 3. Проверяем результат
        Assertions.assertFalse(response.brands().isEmpty());
    });
}
```

*Результат: В Allure-отчете для этого шага будет автоматически создан аттачмент с полным текстом HTTP-запроса и ответа, что делает отладку тривиальной.*


---

## 🎨 Кодстайл: DTO и неизменяемость (Records vs Lombok)

Фреймворк придерживается прагматичного подхода к созданию DTO и выбирает инструмент под задачу. Ниже описаны основные правила, примеры и аргументация.

### Когда использовать `record`

**Правило:** применяйте `record` для неизменяемых контейнеров данных, описывающих ответы API или другие структуры, состояние которых не должно меняться после создания.

**Почему это хорошо:**

- **Гарантия неизменяемости.** Все поля `record` финализированы, отсутствуют сеттеры. Это делает объекты потокобезопасными и предсказуемыми.
- **Автоматические `equals`/`hashCode`/`toString`.** Компилятор генерирует корректные реализации, исключая ошибки и шум в коде.
- **Ясный контракт.** Объявление `public record Brand(...)` прямо сигнализирует, что класс является простым переносчиком данных.
- **Поддержка JSON-библиотек.** Jackson и другие популярные мапперы умеют работать с record без дополнительной конфигурации.

✅ **Хороший пример:** [`Brand.java`](src/test/java/com/example/testsupport/framework/api/dto/gambling/Brand.java)

```java
// DTO, описывающий бренд из API-ответа. Его состояние не должно меняться.
public record Brand(
        String id,
        String name,
        String alias,
        String icon,
        String logo,
        String colorLogo
) {}
```

### Когда использовать класс с Lombok

**Правило:** используйте обычный класс с Lombok (`@Getter`, `@Builder`, при необходимости `@EqualsAndHashCode`) для объектов, которые требуется гибко создавать и у которых не все поля обязательны.

**Почему это хорошо:**

- **Паттерн Builder.** Позволяет декларативно собирать объект, указывая только нужные поля, и избегать длинных конструкторов с множеством `null`.
- **Возможность расширения.** Класс можно дополнить дополнительной логикой, методами и, при необходимости, наследованием.
- **Явные имена параметров.** В builder-е каждое поле вызывается по имени, что повышает читаемость тестов.

✅ **Хороший пример:** [`GamblingBrandsParams.java`](src/test/java/com/example/testsupport/framework/api/client/params/GamblingBrandsParams.java)

```java
@Getter
@Builder
public class GamblingBrandsParams {
    @RequestHeaderParam("Platform-Locale")
    private String platformLocale;

    @RequestQueryParam("categoryAlias")
    private String categoryAlias;
}

// Использование в тесте
var params = GamblingBrandsParams.builder()
        .platformLocale("lv")
        .categoryAlias("new")
        .build();
```

### Итоговые рекомендации

- Не смешивайте `record` и Lombok в одном и том же DTO.
- Для объектов, которые описывают ответ сервера, отдавайте предпочтение `record`.
- Для объектов, предназначенных для формирования запросов, используйте Lombok с паттерном Builder.
- Держите Feign-интерфейсы минималистичными и без бизнес-логики.
- Конфигурацию и перехватчики выносите в отдельные классы, чтобы тесты оставались чистыми.

### Управление состоянием в тестах: Паттерн `TestContext`

**Проблема:**
В длинных E2E-сценариях, состоящих из множества шагов, возникает необходимость передавать состояние (например, Page Objects или полученные данные) между этими шагами. Использование переменных, объявленных на уровне тестового метода, "загрязняет" тест деталями реализации и создает проблемы при работе с лямбда-выражениями в `Allure.step`.

**Решение:**
Фреймворк использует легковесный и мощный паттерн **`TestContext`**. Для каждого сложного теста внутри самого тестового метода объявляется локальный `inner class`, который служит изолированным, типобезопасным контейнером для состояния этого конкретного теста.

**Преимущества этого подхода:**

-   **Инкапсуляция и чистота:** Все "технические" переменные для передачи состояния инкапсулированы внутри одного объекта `ctx`. Сам тест остается чистым и сфокусированным на бизнес-логике.
-   **Гибкость и отсутствие "шума":** Для каждого теста определяется **только тот набор полей, который ему необходим**. Нет лишних `null`-полей от других сценариев.
-   **Потокобезопасность по определению:** Контекст создается как локальная переменная метода и не может быть доступен из других параллельных тестов, что гарантирует полную изоляцию.

**Как это выглядит в коде:**

```java
@Test
void fullCasinoFlowTest(Device device, String languageCode) {
    // 1. Контекст объявляется локально, только для этого теста.
    // Он содержит только те поля, которые нужны в этом сценарии.
    final class TestContext {
        CasinoPage casinoPage;
        FilterDrawerComponent filterDrawer;
        AuthModalComponent authModal;
    }
    final TestContext ctx = new TestContext();

    step("WHEN: Пользователь открывает главную страницу и переходит в казино", () -> {
        MainPage mainPage = mainPageProvider.getObject();
        mainPage.open().verifyIsLoaded();
        // 2. Сохраняем состояние в контекст
        ctx.casinoPage = mainPage.navigateToCasino().verifyIsLoaded();
    });

    step("AND: Фильтрует игры по провайдеру", () -> {
        // 3. Используем состояние из контекста для выполнения следующего шага
        ctx.filterDrawer = ctx.casinoPage.openFilters();
        ctx.filterDrawer.selectProvider("Play'n Go");
        ctx.casinoPage = ctx.filterDrawer.clickShow();
    });

    step("THEN: При попытке запуска игры открывается модальное окно авторизации", () -> {
        ctx.authModal = ctx.casinoPage.clickPlay("Book of Dead").verifyIsLoaded();
    });
}
```

Этот паттерн рекомендуется использовать для всех тестов, содержащих более 2-3 взаимосвязанных шагов.

