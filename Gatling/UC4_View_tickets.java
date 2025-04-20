package io.gatling.demo;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

// 📜 Сценарий нагрузочного тестирования для Web Tours: просмотр маршрута
public class UC4_View_tickets extends Simulation {

    // 🌐 Настройка HTTP-протокола: базовый URL, заголовки, поведение браузера
    private HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:1080") // Базовый URL приложения Web Tours
            .inferHtmlResources() // Автоматическая загрузка связанных ресурсов (CSS, JS, изображения)
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Форматы ответа
            .acceptEncodingHeader("gzip, deflate, br") // Поддержка сжатия
            .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3") // Языковые предпочтения
            .upgradeInsecureRequestsHeader("1") // Разрешить небезопасные запросы
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0"); // Эмуляция браузера Firefox
    // Описание: Определяет глобальные настройки HTTP-запросов, эмулируя поведение современного браузера.
    // Используется для всех запросов в сценарии, обеспечивая единообразие и совместимость с Web Tours.

    // 📋 Заголовки для различных типов запросов
    private Map<CharSequence, String> headers_0 = Map.ofEntries(
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    ); // Для главной страницы, выхода и переходов

    private Map<CharSequence, String> headers_1 = Map.ofEntries(
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin")
    ); // Для навигационных фреймов

    private Map<CharSequence, String> headers_2 = Map.ofEntries(
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    ); // Для POST-запроса логина

    private Map<CharSequence, String> headers_14 = Map.ofEntries(
            Map.entry("Accept", "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5"),
            Map.entry("Priority", "u=5, i"),
            Map.entry("Sec-Fetch-Dest", "image"),
            Map.entry("Sec-Fetch-Mode", "no-cors"),
            Map.entry("Sec-Fetch-Site", "same-origin")
    ); // Для загрузки изображений
    // Описание: Наборы заголовков эмулируют поведение браузера для разных типов запросов (GET/POST, фреймы, изображения).
    // Используются для точного соответствия реальным HTTP-запросам, отправляемым Web Tours.

    // 🗂️ Фидер: циклически раздаёт пользователей из users.csv
    private static final FeederBuilder<String> usersFeeder = csv("users.csv").circular();
    // Описание: Фидер загружает данные пользователей из users.csv (src/test/resources) и раздаёт их
    // виртуальным пользователям по кругу. Каждая запись содержит поля username, password, firstName,
    // lastName, address1, address2. Используется для авторизации.

    // 🗂️ Вспомогательные данные
    private static final Random random = new Random();
    // Описание: Объект Random используется для генерации случайных данных (например, выбора пользователя).
    // Инициализируется один раз для всех пользователей.

    // 🏠 Шаг 1: Загрузка главной страницы
    private ChainBuilder homePage = group("Home_Page").on(
            exec(
                    http("Home_Page_0")
                            .get("/cgi-bin/welcome.pl?signOff=1") // Загрузка главной страницы с параметром выхода
                            .headers(headers_0)
                            .check(
                                    substring("Web Tours").exists(), // ✅ Проверка: страница Web Tours загружена
                                    bodyString().saveAs("tempResponse_0") // 💾 Сохранение ответа для отладки
                            )
            ),
            exec(
                    http("Home_Page_1")
                            .get("/cgi-bin/nav.pl?in=home") // Загрузка навигационной панели
                            .headers(headers_1)
                            .check(
                                    regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession"), // ✅ Динамическая проверка: извлечение userSession
                                    substring("Web Tours Navigation Bar").exists(), // ✅ Проверка: навигационная панель загружена
                                    bodyString().saveAs("tempResponse_1") // 💾 Сохранение ответа для отладки
                            )
            )
    );
    // Описание: Этот блок эмулирует открытие главной страницы Web Tours.
    // - Home_Page_0: Завершает предыдущую сессию (signOff=1) и загружает главную страницу.
    //   Проверяет наличие текста "Web Tours" и сохраняет ответ в tempResponse_0.
    // - Home_Page_1: Загружает навигационную панель и извлекает уникальный идентификатор сессии (userSession)
    //   с помощью regex из скрытого поля формы (<input name="userSession" value="...">).
    //   Проверяет наличие текста "Web Tours Navigation Bar" и сохраняет ответ в tempResponse_1.
    // - Извлечённый userSession (например, "JS12345") используется во всех последующих запросах.
    // - Логирование: Отсутствует, так как нет явных System.out.println.

    // 🔐 Шаг 2: Вход в систему
    private ChainBuilder login = group("Login").on(
            feed(usersFeeder), // 🗂️ Подстановка данных пользователя из users.csv
            exec(session -> {
                // ⚙️ Формирование полного имени пассажира для формы оплаты
                String pass1 = session.getString("firstName") + " " + session.getString("lastName");
                return session.set("pass1", pass1);
            }),
            exec(
                    http("Login_0")
                            .post("/cgi-bin/login.pl") // Отправка формы логина
                            .headers(headers_2)
                            .formParam("userSession", "#{userSession}") // Идентификатор сессии из шага homePage
                            .formParam("username", "#{username}") // Логин из users.csv
                            .formParam("password", "#{password}") // Пароль из users.csv
                            .formParam("login.x", "39") // Координаты кнопки логина
                            .formParam("login.y", "6")
                            .formParam("JSFormSubmit", "off")
                            .check(
                                    substring("User password was correct").exists(), // ✅ Проверка: авторизация успешна
                                    bodyString().saveAs("tempResponse_login1") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("Login_1")
                            .get("/cgi-bin/nav.pl?page=menu&in=home") // Обновление навигационной панели
                            .headers(headers_1)
                            .check(
                                    substring("Web Tours Navigation Bar").exists(), // ✅ Проверка: панель обновлена
                                    bodyString().saveAs("tempResponse_login2") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("Login_2")
                            .get("/cgi-bin/login.pl?intro=true") // Загрузка страницы приветствия
                            .headers(headers_1)
                            .check(
                                    substring("Welcome").exists(), // ✅ Проверка: страница приветствия загружена
                                    bodyString().saveAs("tempResponse_login3") // 💾 Сохранение ответа
                            )
            )
    );
    // Описание: Этот блок эмулирует процесс входа пользователя в систему.
    // - feed(usersFeeder): Динамически подставляет данные (username, password, firstName, lastName, address1, address2)
    //   из users.csv для каждого виртуального пользователя.
    // - exec(session -> ...): Создаёт переменную pass1 (например, "Jo Jo") путём конкатенации firstName и lastName.
    //   Переменная pass1 не используется в UC4, но сохранена для совместимости с другими сценариями.
    // - Login_0: Отправляет POST-запрос с формой логина, используя userSession и данные из фидера.
    //   Проверяет успешность авторизации по тексту "User password was correct" и сохраняет ответ в tempResponse_login1.
    // - Login_1: Обновляет навигационную панель после входа, проверяя её наличие ("Web Tours Navigation Bar"),
    //   сохраняет ответ в tempResponse_login2.
    // - Login_2: Загружает страницу приветствия, подтверждая вход текстом "Welcome", сохраняет ответ в tempResponse_login3.
    // - Извлечённые переменные:
    //   - pass1: Полное имя пассажира (например, "Jo Jo"), не используется в UC4.
    //   - username, password, firstName, lastName, address1, address2: Из users.csv, используются в login.
    // - Логирование: Отсутствует, так как нет явных System.out.println.

    // 🗓️ Шаг 3: Просмотр маршрута
    private ChainBuilder itinerary = group("Itinerary").on(
            exec(
                    http("Itinerary_0")
                            .get("/cgi-bin/welcome.pl?page=itinerary") // Переход на страницу маршрута
                            .headers(headers_0)
                            .check(
                                    substring("already logged").exists(), // ✅ Проверка: пользователь авторизован
                                    bodyString().saveAs("tempResponse_itinerary1") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("Itinerary_1")
                            .get("/cgi-bin/nav.pl?page=menu&in=itinerary") // Обновление навигационной панели
                            .headers(headers_1)
                            .check(
                                    substring("Itinerary").exists(), // ✅ Проверка: панель маршрута загружена
                                    bodyString().saveAs("tempResponse_itinerary2") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("Itinerary_2")
                            .get("/cgi-bin/itinerary.pl") // Загрузка списка бронирований
                            .headers(headers_1)
                            .check(
                                    substring("Flights List").exists(), // ✅ Проверка: список рейсов загружен
                                    bodyString().saveAs("tempResponse_itinerary3") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("Itinerary_3")
                            .get("/WebTours/images/cancelreservation.gif") // Загрузка изображения кнопки отмены
                            .headers(headers_14)
                            .check(
                                    bodyBytes().saveAs("tempResponse_itinerary4") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("Itinerary_4")
                            .get("/WebTours/images/cancelallreservations.gif") // Загрузка изображения полной отмены
                            .headers(headers_14)
                            .check(
                                    bodyBytes().saveAs("tempResponse_itinerary5") // 💾 Сохранение ответа
                            )
            )
    );
    // Описание: Этот блок эмулирует просмотр забронированного маршрута.
    // - Itinerary_0: Переходит на страницу маршрута, проверяя, что пользователь авторизован
    //   (текст "already logged"), сохраняет ответ в tempResponse_itinerary1.
    // - Itinerary_1: Обновляет навигационную панель для раздела маршрута, проверяя текст "Itinerary",
    //   сохраняет ответ в tempResponse_itinerary2.
    // - Itinerary_2: Загружает список бронирований, проверяя текст "Flights List",
    //   сохраняет ответ в tempResponse_itinerary3.
    // - Itinerary_3, Itinerary_4: Загружают изображения кнопок отмены бронирования,
    //   сохраняют их в tempResponse_itinerary4 и tempResponse_itinerary5 как байты.
    // - Извлечённые переменные:
    //   - tempResponse_itinerary1, tempResponse_itinerary2, tempResponse_itinerary3: HTML-ответы.
    //   - tempResponse_itinerary4, tempResponse_itinerary5: Байтовые данные изображений.
    // - Логирование: Отсутствует.

    // 🔓 Шаг 4: Выход из системы
    private ChainBuilder logout = group("Logout").on(
            exec(
                    http("Logout_0")
                            .get("/cgi-bin/welcome.pl?signOff=1") // Запрос на выход
                            .headers(headers_0)
                            .check(
                                    substring("Session ID has been created").exists(), // ✅ Проверка: возврат на главную страницу
                                    bodyString().saveAs("tempResponse_logout1") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("Logout_1")
                            .get("/cgi-bin/nav.pl?in=home") // Обновление навигационной панели
                            .headers(headers_1)
                            .check(
                                    substring("Web Tours Navigation Bar").exists(), // ✅ Проверка: панель загружена
                                    regex("<input type=\"hidden\" name=\"userSession\" value=\"(.+?)\"").exists(), // ✅ Проверка: новая сессия создана
                                    bodyString().saveAs("tempResponse_logout2") // 💾 Сохранение ответа
                            )
            )
    );
    // Описание: Этот блок эмулирует выход пользователя из системы.
    // - Logout_0: Завершает сессию (signOff=1), возвращая на главную страницу.
    //   Проверяет текст "Session ID has been created" и сохраняет ответ в tempResponse_logout1.
    // - Logout_1: Обновляет навигационную панель, проверяя её наличие ("Web Tours Navigation Bar")
    //   и существование нового userSession в скрытом поле формы.
    //   Сохраняет ответ в tempResponse_logout2.
    // - Извлечённые переменные:
    //   - tempResponse_logout1, tempResponse_logout2: HTML-ответы.
    // - Логирование: Отсутствует.

    // 📋 Сценарий: объединение всех шагов с паузами
    private ScenarioBuilder scn = scenario("UC4_View_tickets")
            .exec(homePage) // Шаг 1: Главная страница
            .pause(3) // ⏳ Пауза 3 секунды для реалистичности
            .exec(login) // Шаг 2: Вход
            .pause(3) // ⏳ Пауза
            .exec(itinerary) // Шаг 3: Просмотр маршрута
            .pause(3) // ⏳ Пауза
            .exec(logout); // Шаг 4: Выход
    // Описание: Определяет полный сценарий UC4_View_tickets, объединяя все шаги в логическую последовательность.
    // Пауза в 3 секунды между шагами эмулирует реальное поведение пользователя, делая нагрузку более естественной.
    // Каждый шаг выполняется последовательно, передавая данные через сессию.

    // ⚙️ Настройка нагрузки: 1 пользователь в секунду, 1 секунда
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
    // - constantUsersPerSec(1): Запускает 1 нового пользователя каждую секунду.
    // - during(1): Продолжительность теста — 1 секунда, что создаёт 1 пользователя.
    // - injectOpen: Модель открытой нагрузки, где пользователи добавляются независимо от завершения предыдущих.
    // - protocols(httpProtocol): Применяет глобальные настройки HTTP ко всем запросам.
}