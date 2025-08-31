# 🎯 Фреймворк Spelet Playwright

Spring Boot + Playwright фреймворк для надёжного UI‑тестирования с богатыми Allure‑отчётами.

![Java](https://img.shields.io/badge/Java-21+-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?logo=springboot)
![Playwright](https://img.shields.io/badge/Playwright-1.48-45ba52?logo=playwright)
![Gradle](https://img.shields.io/badge/Gradle-8.14-blue?logo=gradle)
![JUnit 5](https://img.shields.io/badge/JUnit%205-5.10-25A162?logo=junit5)
![Allure](https://img.shields.io/badge/Allure-2.29-ff69b4?logo=allure)

---

## Фреймворк в действии
[Демонстрационная GIF-анимация будет добавлена позже]

## Философия и ключевые особенности
- **Конфигурация как код** — профили YAML управляют браузерами, устройствами и параллелизмом.
- **Самовосстанавливающиеся тесты** — кастомные аннотации `@RetryableTest` и `@RetryableParameterizedTest`.
- **Глубокая интеграция с Allure** — шаги, вложения и кастомные сьюты «из коробки».
- **Удобство для разработчиков** — типизированный API‑клиент на паттерне Builder и автоматическое маппирование параметров.

## Технологический стек
| Категория | Технология | Назначение |
| --- | --- | --- |
| Язык | Java 21 | Основной язык |
| Фреймворк | Spring Boot 3.5 | DI и конфигурация |
| Автоматизация браузера | Playwright 1.48 | Взаимодействие с UI |
| Сборка | Gradle 8.14 | Сборка и зависимости |
| Тестирование | JUnit 5 | Запуск тестов |
| Отчётность | Allure 2.29 | Богатые отчёты |
| Облачный грид | BrowserStack | Кросс‑браузерные/устройствные прогоны |
| HTTP | OpenFeign | Типизированные API‑клиенты |

## Быстрый старт
1. **Клонируйте репозиторий**
   ```bash
   git clone https://github.com/your-user/spelet-playwright-java.git
   cd spelet-playwright-java
   ```
2. **Установите браузеры Playwright**
   ```bash
   gradle playwrightInstall
   ```
3. **Запустите тесты**
   ```bash
   gradle test -Dspring.profiles.active=spelet -Denv.browser.headless=true
   ```
4. **Посмотрите отчёт Allure**
   ```bash
   gradle allureServe
   ```

## Гайд по использованию
### Запуск сценариев
- **Локальный профиль**
  ```bash
  gradle test -Dspring.profiles.active=spelet -Denv.browser.headless=true
  ```
- **BrowserStack: Chrome на Windows**
  ```bash
  gradle bsWin10Chrome -Dspring.profiles.active=spelet-bs -Denv.browser.headless=true
  ```
- **Демо-окружение**
  ```bash
  gradle test -Dspring.profiles.active=spelet-demo -Denv.browser.headless=true
  ```
- **Фильтр устройств**
  ```bash
  gradle test -Dspring.profiles.active=spelet -Dtest.devices="Desktop FullHD,MacBook Pro"
  ```
- **Запуск отдельных тестов**
  ```bash
  gradle test -Dspring.profiles.active=spelet --tests "*MultilingualNavigation*"
  ```

### Примеры фич
```java
@RetryableTest(repeats = 3, suspend = 1000)
void flakyLogic() {
    // ...
}
```
```java
@RetryableParameterizedTest(name = "[Device: {0}, Lang: {1}]")
void navigate(Device device, String lang) { /* ... */ }
```
```java
@Suite("Navigation and casino flow")
class MultilingualNavigationTest { /* ... */ }
```
```java
GamblingBrandsParams params = GamblingBrandsParams.builder()
        .platformNodeId("spelet.lv")
        .platformLocale("lv")
        .deviceType("desktop")
        .build();
```

## Архитектура в деталях
### YAML‑управляемый параллелизм и ретраи
Gradle читает активный Spring‑профиль и на лету настраивает JUnit:
```groovy
tasks.withType(Test) {
    useJUnitPlatform()
    systemProperties = System.getProperties() as Map<String, ?>
    jvmArgs '-Dfile.encoding=UTF-8', '--add-opens=java.base/java.lang=ALL-UNNAMED'
    dependsOn(playwrightInstall)

    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
        showStandardStreams = true
    }

    doFirst {
        def activeProfile = System.getProperty('spring.profiles.active')
        if (!activeProfile) {
            activeProfile = systemProperties['spring.profiles.active']
        }
        if (activeProfile == null || activeProfile.trim().isEmpty()) {
            throw new GradleException("ERROR: Spring profile is not set. Please run tests with -Dspring.profiles.active=<profile>")
        }
    }
}
```

### Матрица устройств и языков
`TestMatrixService` формирует комбинации девайсов и локалей для параметризованных тестов:
```java
public Stream<List<Object>> getTestMatrix() {
    List<Device> allDevices = config.getTestDevices().getPlatforms();
    if (allDevices == null || allDevices.isEmpty()) {
        throw new IllegalStateException("No test devices configured under env.test-devices.platforms");
    }
    List<String> languages = config.getBrowser().getLanguages();
    if (languages == null || languages.isEmpty()) {
        languages = List.of("lv", "ru", "en");
    }
    final List<String> finalLanguages = languages;

    String filter = System.getProperty("test.devices");
    List<Device> devices;
    if (filter == null || filter.isBlank()) {
        devices = allDevices;
    } else {
        Set<String> requested = Arrays.stream(filter.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Map<String, Device> deviceMap = allDevices.stream()
                .collect(Collectors.toMap(Device::getName, Function.identity()));
        List<String> missing = requested.stream()
                .filter(name -> !deviceMap.containsKey(name))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Device(s) not found in configuration: " + String.join(", ", missing));
        }
        devices = requested.stream().map(deviceMap::get).toList();
    }

    return devices.stream()
            .flatMap(device -> finalLanguages.stream()
                    .map(lang -> List.of(device, lang)));
}
```

### Генерический интерцептор параметров API
Кастомный интерцептор Feign сопоставляет аннотированные поля с query‑параметрами и заголовками:
```java
public class GenericParamsInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        if (!template.queries().containsKey("params")) {
            return;
        }

        Collection<?> paramsCollection = template.queries().get("params");
        if (paramsCollection == null || paramsCollection.isEmpty()) {
            clearParamsQuery(template);
            return;
        }

        Object params = paramsCollection.iterator().next();
        clearParamsQuery(template);

        for (Field field : params.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(params);
                if (value == null) {
                    continue;
                }
                RequestQueryParam queryAnn = field.getAnnotation(RequestQueryParam.class);
                if (queryAnn != null) {
                    template.query(queryAnn.value(), value.toString());
                }
                RequestHeaderParam headerAnn = field.getAnnotation(RequestHeaderParam.class);
                if (headerAnn != null) {
                    template.header(headerAnn.value(), value.toString());
                }
            } catch (IllegalAccessException ignored) {
                // поле недоступно
            }
        }
    }

    private void clearParamsQuery(RequestTemplate template) {
        Map<String, Collection<String>> queries = new LinkedHashMap<>(template.queries());
        queries.remove("params");
        template.queries(queries);
    }
}
```

### Кастомные сьюты Allure
`CustomSuiteExtension` переписывает label `suite` на основе аннотации `@Suite`:
```java
public class CustomSuiteExtension implements BeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Suite suite = resolveSuite(context);
        if (suite == null) {
            return;
        }
        Allure.getLifecycle().updateTestCase(tc -> tc.getLabels().removeIf(l -> "suite".equals(l.getName())));
        Allure.getLifecycle().updateTestCase(tc -> tc.getLabels().add(new Label().setName("suite").setValue(suite.value())));
    }

    private Suite resolveSuite(ExtensionContext context) {
        Optional<Suite> methodSuite = context.getTestMethod().map(m -> m.getAnnotation(Suite.class));
        if (methodSuite.isPresent()) {
            return methodSuite.get();
        }
        return context.getTestClass().map(c -> c.getAnnotation(Suite.class)).orElse(null);
    }
}
```

## Галерея отчётов
| Дашборд с кастомными сьютами | Детализация шагов | Карточка упавшего теста | Пример flaky-теста |
| --- | --- | --- | --- |
| ![Dashboard screenshot placeholder](docs/report_dashboard.png) | ![Steps screenshot placeholder](docs/report_steps.png) | ![Failed test screenshot placeholder](docs/report_failed.png) | ![Flaky test screenshot placeholder](docs/report_flaky.png) |
