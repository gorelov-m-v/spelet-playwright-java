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

## 2. Стратегия обработки нестабильных тестов

Кастомные аннотации `@RetryableTest` и `@RetryableParameterizedTest` были удалены из проекта из-за сложности поддержки.
На текущем этапе команда сосредоточена на написании стабильных тестов и анализе причин нестабильности.
При необходимости перезапуска упавших тестов можно воспользоваться встроенным расширением
`RetryableExtension`. Оно читает свойство `test.retry` из системного свойства
(`-Dtest.retry=<n>`) либо из конфигурации активного профиля (`application-<profile>.yml`) и
повторяет каждый тест указанное число раз. В этом случае аннотация `@Retryable` становится
необязательной. Нет необходимости выставлять `System.setProperty` внутри самих тестов —
значение можно передать при запуске или описать в YAML-файлах активного профиля (профиль
задаётся переменной окружения `SPRING_PROFILES_ACTIVE` или системным свойством `-Dspring.profiles.active`). Также можно рассмотреть сторонние решения, например
[JUnit Pioneer](https://junit-pioneer.org/) с аннотацией `@RetryingTest`.

---

## 3. Декларативный API-клиент на OpenFeign

**Проблема:**
Вызов API-методов с множеством необязательных параметров через стандартный Feign приводит к перегруженным сигнатурам и обилию `null`.

**Решение:**
Использовать Lombok Builder для формирования запросов и встроенные возможности OpenFeign:
- `@SpringQueryMap` разворачивает поля объекта в query-параметры.
- `@RequestHeader` явно описывает HTTP-заголовки.

### Шаг 1: DTO для query-параметров

```java
@Getter
@Builder
public class GamblingBrandsParams {
    private String deviceType;
    private Boolean showRestricted;
    private String categoryAlias;
}
```

### Шаг 2: Описание интерфейса клиента

```java
@FeignClient(name = "frontApiClient", url = "${env.api.base-url}")
public interface FrontApiClient {
    @GetMapping("/_front_api/api/v1/gambling/brands")
    GamblingBrandsResponse getGamblingBrands(
            @RequestHeader("Platform-Locale") String platformLocale,
            @SpringQueryMap GamblingBrandsParams params);
}
```

### Шаг 3: Использование в тесте

```java
var params = GamblingBrandsParams.builder()
        .categoryAlias("new")
        .build();

GamblingBrandsResponse response = frontApiClient.getGamblingBrands("lv", params);
```

*Такой подход использует только стандартный функционал Feign и не требует дополнительных интерцепторов.*

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
    private String deviceType;
    private Boolean showRestricted;
    private String categoryAlias;
}

// Использование в тесте
var params = GamblingBrandsParams.builder()
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

