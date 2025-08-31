# ✨ Фокус на Developer Experience (DX): Кастомные аннотации как DSL

Фреймворк спроектирован так, чтобы написание тестов было простым, читаемым и безопасным. Вместо того чтобы заставлять разработчиков использовать сложные комбинации стандартных аннотаций, был создан собственный DSL (Domain-Specific Language). Он скрывает внутреннюю сложность фреймворка и библиотек, предоставляя чистый и декларативный API.

---

## 1. `@Suite` — Человекочитаемые отчеты

**Проблема:**  
Стандартная интеграция Allure с JUnit 5 использует полное имя тестового класса (например, `tests.casino.MultilingualNavigationTest`) в качестве названия для "сьюта" (Test Suite). Это делает отчеты технически перегруженными и затрудняет их понимание для нетехнических специалистов.

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

## 2. `@RetryableTest` & `@RetryableParameterizedTest` — Встроенная отказоустойчивость

**Проблема:**  
E2E-тесты подвержены нестабильности (flakiness) из-за проблем с сетью или средой. Стандартные аннотации JUnit 5 не предоставляют механизма перезапуска, а использование сторонних библиотек требует сложной и многословной конфигурации.

**Решение:**
Созданы две аннотации-агрегаторы, которые инкапсулируют всю логику перезапуска:
- [`@RetryableTest`](src/main/java/com/example/testsupport/framework/junit/retries/RetryableTest.java) — для обычных тестов (работает в паре с [`RetryableTestExtension`](src/main/java/com/example/testsupport/framework/junit/retries/RetryableTestExtension.java)).
- [`@RetryableParameterizedTest`](src/main/java/com/example/testsupport/framework/junit/retries/RetryableParameterizedTest.java) — для параметризованных тестов (работает в паре с [`RetryableParameterizedTestExtension`](src/main/java/com/example/testsupport/framework/junit/retries/RetryableParameterizedTestExtension.java)).

**Как это работает:**  
Эти аннотации регистрируют кастомные JUnit-расширения, которые перехватывают исключения при падении теста. Если лимит попыток, заданный в YAML-конфигурации, не исчерпан, расширение генерирует для JUnit новый контекст запуска, эффективно заставляя его повторить упавший тест.

**Как применять:**

**До (многословно):**

```java
@TestTemplate
@ArgumentsSource(DeviceProvider.class)
@ExtendWith(RetryableParameterizedTestExtension.class)
void navigationTest(Device device, String languageCode) {
    // ...
}
```

**После (одна аннотация):**

```java
@RetryableParameterizedTest(name = "[Устройство: {0}, Язык: {1}]")
void navigationTest(Device device, String languageCode) {
    // ...
}
```

*Это делает любой тест отказоустойчивым одной строкой, повышая стабильность CI/CD.*

---

## 3. Аннотации для декларативного API-клиента

**Проблема:**  
Вызов API-методов с множеством необязательных параметров через стандартный Feign приводит к коду с большим количеством `null`, что нечитаемо и хрупко.

**Решение:**  
Реализован паттерн Builder в связке с универсальным Feign-интерцептором и кастомными аннотациями для описания параметров запроса.

Аннотации: `@RequestQueryParam`, `@RequestHeaderParam`  
Перехватчик: `GenericParamsInterceptor`

**Как это работает (пошагово):**

### Шаг 1: Описываем параметры запроса с помощью DTO и аннотаций

Для каждого API-эндпоинта создается POJO-класс, поля которого представляют все возможные параметры и помечаются нашими аннотациями.

```java
// Файл: .../params/GamblingBrandsParams.java
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

```java
// Файл: .../api/client/FrontApiClient.java
@FeignClient(name = "...", configuration = GenericParamsInterceptor.class)
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

`GenericParamsInterceptor` перехватывает запрос и, используя рефлексию, динамически добавляет в него только те параметры и заголовки, которые были заданы в билдере.
