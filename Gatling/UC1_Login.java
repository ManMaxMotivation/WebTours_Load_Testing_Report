package io.gatling.demo;

import java.time.Duration;
import java.util.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class UC1_Login extends Simulation {

    // 📦 Конфигурация HTTP-протокола
    // Описание: Настраивает базовые параметры HTTP-запросов, такие как базовый URL, заголовки и поведение браузера.
    // - baseUrl: Указывает адрес тестируемого приложения (http://localhost:1080).
    // - inferHtmlResources: Автоматически загружает связанные ресурсы (CSS, JS, изображения).
    // - Заголовки: Имитируют поведение браузера Firefox 137.0, включая поддержку сжатия (gzip, deflate),
    //   языковые предпочтения (ru-RU) и небезопасные запросы.
    private HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:1080")
            .inferHtmlResources()
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptEncodingHeader("gzip, deflate")
            .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
            .upgradeInsecureRequestsHeader("1")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0");

    // 🧾 Заголовки для HTTP-запросов
    // Описание: Определяют специфичные заголовки для различных типов запросов, чтобы имитировать поведение браузера.
    // - headers_0: Для запросов к главной странице (document, navigate, same-origin).
    // - headers_1: Для запросов к навигационным элементам (frame, navigate, same-origin).
    // - headers_2: Для POST-запросов (включает Origin для кросс-доменных запросов).
    private Map<CharSequence, String> headers_0 = Map.ofEntries(
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );

    private Map<CharSequence, String> headers_1 = Map.ofEntries(
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin")
    );

    private Map<CharSequence, String> headers_2 = Map.ofEntries(
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );

    // 🗂️ Фидер: циклически раздаёт пользователей из users.csv (username, password)
    // Использует .circular() для бесконечного повторного использования записей
    private static final FeederBuilder<String> usersFeeder = csv("users.csv").circular();
    // Описание: Фидер загружает учётные данные из users.csv (src/test/resources) и раздаёт их
    // виртуальным пользователям по кругу. Это позволяет использовать пользователей многократно,
    // избегая ошибки "Feeder is now empty". Каждая запись содержит поля username и password.

    // 🏠 Шаг загрузки главной страницы
    // Описание: Выполняет два запроса для загрузки главной страницы и навигационной панели.
    // - Home_Page_0: Загружает главную страницу с параметром signOff=1 для сброса сессии.
    // - Home_Page_1: Загружает навигационную панель и извлекает userSession для последующих шагов.
    private ChainBuilder homePage = group("Home_Page").on(
            exec(
                    http("Home_Page_0")
                            .get("/cgi-bin/welcome.pl?signOff=1")
                            .headers(headers_0)
                            .check(
                                    substring("Web Tours").exists(), // Проверяет, что страница содержит текст "Web Tours"
                                    bodyString().saveAs("tempResponse_0") // Сохраняет тело ответа для отладки
                            )
                    // Описание проверок:
                    // - substring("Web Tours").exists(): Убеждается, что главная страница загрузилась корректно.
                    // - bodyString().saveAs("tempResponse_0"): Сохраняет полный HTML-ответ для возможной отладки.
            ),
            exec(
                    http("Home_Page_1")
                            .get("/cgi-bin/nav.pl?in=home")
                            .headers(headers_1)
                            .check(
                                    regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession"), // Извлекает userSession
                                    substring("Web Tours Navigation Bar").exists(), // Проверяет наличие навигационной панели
                                    bodyString().saveAs("tempResponse_1") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - regex(...).saveAs("userSession"): Извлекает значение userSession из hidden-поля формы для использования в логине.
                    // - substring("Web Tours Navigation Bar").exists(): Подтверждает, что навигационная панель загрузилась.
                    // - bodyString().saveAs("tempResponse_1"): Сохраняет HTML-ответ для отладки.
            )
    );

    // 🔐 Шаг входа в систему
    // Описание: Выполняет авторизацию пользователя с использованием данных из usersFeeder.
    // - feed: Загружает username и password из CSV.
    // - login1: Отправляет POST-запрос с данными формы для логина.
    // - login2: Загружает навигационную панель после логина.
    // - login3: Загружает страницу приветствия.
    private ChainBuilder loginStep = group("Login").on(
            feed(usersFeeder), // Подставляет логин/пароль из CSV
            exec(
                    http("login1")
                            .post("/cgi-bin/login.pl")
                            .headers(headers_2)
                            .formParam("userSession", "#{userSession}")
                            .formParam("username", "#{username}")
                            .formParam("password", "#{password}")
                            .formParam("login.x", "0")
                            .formParam("login.y", "0")
                            .formParam("JSFormSubmit", "off")
                            .check(
                                    substring("User password was correct").exists(), // Проверяет успешность логина
                                    bodyString().saveAs("tempResponse_login1") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("User password was correct").exists(): Подтверждает, что логин прошел успешно.
                    // - bodyString().saveAs("tempResponse_login1"): Сохраняет HTML-ответ для отладки.
            ),
            exec(
                    http("login2")
                            .get("/cgi-bin/nav.pl?page=menu&in=home")
                            .headers(headers_1)
                            .check(
                                    substring("Web Tours Navigation Bar").exists(), // Проверяет наличие навигационной панели
                                    bodyString().saveAs("tempResponse_login2") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("Web Tours Navigation Bar").exists(): Убеждается, что навигационная панель обновилась после логина.
                    // - bodyString().saveAs("tempResponse_login2"): Сохраняет HTML-ответ для отладки.
            ),
            exec(
                    http("login3")
                            .get("/cgi-bin/login.pl?intro=true")
                            .headers(headers_1)
                            .check(
                                    substring("Welcome").exists(), // Проверяет наличие приветственного текста
                                    bodyString().saveAs("tempResponse_login3") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("Welcome").exists(): Подтверждает, что страница приветствия загрузилась.
                    // - bodyString().saveAs("tempResponse_login3"): Сохраняет HTML-ответ для отладки.
            )
    );

    // 🔓 Шаг выхода из системы
    // Описание: Выполняет логаут и возвращает пользователя на главную страницу.
    // - logout1: Отправляет запрос на завершение сессии.
    // - logout2: Загружает навигационную панель для подтверждения выхода.
    private ChainBuilder logout = group("Logout").on(
            exec(
                    http("logout1")
                            .get("/cgi-bin/welcome.pl?signOff=1")
                            .headers(headers_0)
                            .check(
                                    substring("Web Tours").exists(), // Проверяет, что главная страница загрузилась
                                    bodyString().saveAs("tempResponse_logout1") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("Web Tours").exists(): Убеждается, что пользователь вернулся на главную страницу.
                    // - bodyString().saveAs("tempResponse_logout1"): Сохраняет HTML-ответ для отладки.
            ),
            exec(
                    http("logout2")
                            .get("/cgi-bin/nav.pl?in=home")
                            .headers(headers_1)
                            .check(
                                    substring("Web Tours Navigation Bar").exists(), // Проверяет наличие навигационной панели
                                    regex("<input type=\"hidden\" name=\"userSession\" value=\"(.+?)\"").exists(), // Проверяет наличие нового userSession
                                    bodyString().saveAs("tempResponse_logout2") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("Web Tours Navigation Bar").exists(): Подтверждает, что навигационная панель загрузилась.
                    // - regex(...).exists(): Убеждается, что новая сессия создана после логаута.
                    // - bodyString().saveAs("tempResponse_logout2"): Сохраняет HTML-ответ для отладки.
            )
    );

    // 🧪 Сценарий объединяет шаги
    // Описание: Определяет последовательность действий виртуального пользователя:
    // 1. Загрузка главной страницы.
    // 2. Вход в систему с использованием данных из CSV.
    // 3. Выход из системы.
    // Паузы (3 секунды) между шагами имитируют реальное поведение пользователя.
    private ScenarioBuilder scn = scenario("UC1_Login")
            .exec(homePage)
            .pause(3)
            .exec(loginStep)
            .pause(3)
            .exec(logout);

    // ⚙️ Настройка нагрузки: 3 пользователя в секунду, 34 секунды (~100 использований)
    private static final int USER_COUNT = 1;
    private static final Duration TEST_DURATION = Duration.ofSeconds(1);

    {
        // 🚀 Запуск сценария с настройкой HTTP-протокола
        setUp(
                scn.injectOpen(
                        constantUsersPerSec(USER_COUNT).during(TEST_DURATION)
                )
        ).protocols(httpProtocol);
    }
    // Описание: Определяет профиль нагрузки для теста.
    // - constantUsersPerSec(3): Запускает 3 новых пользователя каждую секунду.
    // - during(34): Продолжительность теста — 34 секунды, что создаёт ~102 пользователя (3 × 34).
    // - Это обеспечивает ~100 использований пользователей из users.csv (циклическое использование с .circular()).
    // - injectOpen: Модель открытой нагрузки, где пользователи добавляются независимо от завершения предыдущих.
    // - protocols(httpProtocol): Применяет глобальные настройки HTTP ко всем запросам.
}