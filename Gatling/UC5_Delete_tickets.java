package io.gatling.demo;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

// 📜 Сценарий нагрузочного тестирования для Web Tours: удаление билетов
public class UC5_Delete_tickets extends Simulation {

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

    private Map<CharSequence, String> headers_8 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----#{boundary}"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    ); // Для POST-запроса удаления билетов

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
    // Описание: Объект Random используется для генерации случайных данных (например, выбора билетов для удаления).
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
                // ⚙️ Формирование полного имени пассажира (не используется в UC5, сохранено для совместимости)
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
    //   Переменная pass1 не используется в UC5, но сохранена для совместимости с другими сценариями.
    // - Login_0: Отправляет POST-запрос с формой логина, используя userSession и данные из фидера.
    //   Проверяет успешность авторизации по тексту "User password was correct" и сохраняет ответ в tempResponse_login1.
    // - Login_1: Обновляет навигационную панель после входа, проверяя её наличие ("Web Tours Navigation Bar"),
    //   сохраняет ответ в tempResponse_login2.
    // - Login_2: Загружает страницу приветствия, подтверждая вход текстом "Welcome", сохраняет ответ в tempResponse_login3.
    // - Извлечённые переменные:
    //   - pass1: Полное имя пассажира (например, "Jo Jo"), не используется в UC5.
    //   - username, password, firstName, lastName, address1, address2: Из users.csv, используются в login.
    // - Логирование: Отсутствует, но username и password логируются позже в logItinerary.

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
                                    substring("\"flightID\" value").exists(), // ✅ Проверка: наличие поля flightID в ответе
                                    regex("name=\"flightID\" value=\"(.+?)\"").findAll().saveAs("flightIDs"), // ✅ Динамическая проверка: извлечение всех flightID
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
    // - Itinerary_2: Загружает список бронирований, проверяя текст "Flights List".
    //   Дополнительно:
    //     - Проверяет наличие поля flightID в ответе (текст "\"flightID\" value").
    //     - Извлекает все идентификаторы рейсов (flightID) с помощью regex "name=\"flightID\" value=\"(.+?)\"".
    //     - Сохраняет их в переменную flightIDs как список (например, ["123456", "789012"]).
    //   Сохраняет ответ в tempResponse_itinerary3.
    // - Itinerary_3, Itinerary_4: Загружают изображения кнопок отмены бронирования,
    //   сохраняют их в tempResponse_itinerary4 и tempResponse_itinerary5 как байты.
    // - Извлечённые переменные:
    //   - flightIDs: Список идентификаторов рейсов (например, ["123456", "789012"]).
    //   - tempResponse_itinerary1, tempResponse_itinerary2, tempResponse_itinerary3: HTML-ответы.
    //   - tempResponse_itinerary4, tempResponse_itinerary5: Байтовые данные изображений.
    // - Логирование: Отсутствует, но flightIDs логируются позже в logItinerary.

    // 🗑️ Шаг 4: Удаление билетов
    private ChainBuilder removal_itinerary = group("Removal_Itinerary").on(
            exec(session -> {
                // ⚙️ Генерация уникальной границы для multipart/form-data
                String boundary = "geckoformboundary" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

                // 🗂️ Получение списка flightIDs
                List<String> flightIDs = session.get("flightIDs");
                int flightCount = flightIDs != null ? flightIDs.size() : 0;

                // 🎲 Определение количества билетов для удаления
                int ticketsToRemove = flightCount > 5 ? 2 : 1;

                // 🎲 Случайный выбор индексов для удаления
                Set<Integer> indicesToRemove = new HashSet<>();
                while (indicesToRemove.size() < ticketsToRemove && flightCount > 0) {
                    indicesToRemove.add(random.nextInt(flightCount));
                }

                // 💾 Сохранение выбранных flightID для проверки
                List<String> removedFlightIDs = new ArrayList<>();
                for (Integer index : indicesToRemove) {
                    removedFlightIDs.add(flightIDs.get(index));
                }

                // 📝 Формирование тела запроса
                StringBuilder requestBody = new StringBuilder();
                for (int i = 0; i < flightCount; i++) {
                    requestBody.append("------").append(boundary).append("\r\n")
                            .append("Content-Disposition: form-data; name=\"").append(i + 1).append("\"\r\n\r\n");
                    if (indicesToRemove.contains(i)) {
                        requestBody.append("on\r\n");
                    } else {
                        requestBody.append("\r\n");
                    }
                }
                for (int i = 0; i < flightCount; i++) {
                    requestBody.append("------").append(boundary).append("\r\n")
                            .append("Content-Disposition: form-data; name=\"flightID\"\r\n\r\n")
                            .append(flightIDs.get(i)).append("\r\n");
                }
                requestBody.append("------").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"removeFlights.x\"\r\n\r\n")
                        .append(random.nextInt(50) + 20).append("\r\n")
                        .append("------").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"removeFlights.y\"\r\n\r\n")
                        .append(random.nextInt(20)).append("\r\n");
                for (int i = 0; i < flightCount; i++) {
                    requestBody.append("------").append(boundary).append("\r\n")
                            .append("Content-Disposition: form-data; name=\".cgifields\"\r\n\r\n")
                            .append(i + 1).append("\r\n");
                }
                requestBody.append("------").append(boundary).append("--\r\n");

                // 💾 Сохранение тела запроса, границы и удалённых flightID в сессию
                return session.set("removeFlightsBody", requestBody.toString())
                        .set("boundary", boundary)
                        .set("removedFlightIDs", removedFlightIDs);
            }),
            exec(
                    http("Removal_Itinerary_0")
                            .post("/cgi-bin/itinerary.pl") // Удаление выбранных билетов
                            .headers(headers_8)
                            .body(StringBody("#{removeFlightsBody}"))
                            .check(
                                    substring("Itinerary").exists(), // ✅ Проверка: страница маршрута обновлена
                                    regex("name=\"flightID\" value=\"(.+?)\"").findAll().saveAs("remainingFlightIDs"), // ✅ Динамическая проверка: извлечение оставшихся flightID
                                    bodyString().saveAs("tempResponse_removal") // 💾 Сохранение ответа
                            )
            )
    );
    // Описание: Этот блок эмулирует удаление билетов из маршрута.
    // - exec(session -> ...):
    //     - Генерирует уникальную границу (boundary) для multipart/form-data.
    //     - Получает список flightIDs из сессии и определяет количество билетов для удаления (1, если ≤ 5; 2, если > 5).
    //     - Случайно выбирает индексы билетов для удаления (indicesToRemove).
    //     - Сохраняет выбранные flightID (removedFlightIDs) для последующей проверки.
    //     - Формирует тело запроса в формате multipart/form-data:
    //       - Чекбоксы (name="1", value="on") для удаляемых билетов.
    //       - Все flightID (name="flightID", value="...").
    //       - Координаты кнопки (removeFlights.x, removeFlights.y) со случайными значениями.
    //       - Поля .cgifields для всех билетов.
    //     - Сохраняет тело запроса (removeFlightsBody), границу (boundary) и removedFlightIDs в сессию.
    // - Removal_Itinerary_0: Отправляет POST-запрос для удаления билетов.
    //   Проверяет:
    //     - Наличие текста "Itinerary", подтверждая обновление страницы маршрута.
    //     - Извлекает оставшиеся flightID (remainingFlightIDs) для проверки удаления.
    //   Сохраняет ответ в tempResponse_removal.
    // - Извлечённые переменные:
    //   - removeFlightsBody: Тело запроса в формате multipart/form-data.
    //   - boundary: Уникальная граница для Content-Type.
    //   - removedFlightIDs: Список удалённых flightID (например, ["123456", "789012"]).
    //   - remainingFlightIDs: Список оставшихся flightID после удаления.
    //   - tempResponse_removal: HTML-ответ.
    // - Логирование: Отсутствует, но removedFlightIDs и remainingFlightIDs логируются в logItinerary.

    // 🔓 Шаг 5: Выход из системы
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

    // 📜 Шаг 6: Логирование результатов удаления билетов
    private ChainBuilder logItinerary = group("Log_Itinerary").on(
            exec(session -> {
                // ⚙️ Извлечение данных из сессии для логирования
                String username = session.getString("username");
                String password = session.getString("password");
                List<String> flightIDs = session.get("flightIDs");
                List<String> removedFlightIDs = session.get("removedFlightIDs");
                List<String> remainingFlightIDs = session.get("remainingFlightIDs");

                // ✅ Проверка успешности удаления
                boolean deletionSuccessful = true;
                if (removedFlightIDs != null && remainingFlightIDs != null) {
                    for (String removedID : removedFlightIDs) {
                        if (remainingFlightIDs.contains(removedID)) {
                            deletionSuccessful = false;
                            break;
                        }
                    }
                } else {
                    deletionSuccessful = false;
                }

                // 📜 Логирование результатов
                System.out.println("User Credentials: username=" + username + ", password=" + password);
                System.out.println("Total Flights: " + (flightIDs != null ? flightIDs.size() : 0));
                System.out.println("Removed Flight IDs: " + (removedFlightIDs != null ? removedFlightIDs : "None"));
                System.out.println("Remaining Flight IDs: " + (remainingFlightIDs != null ? remainingFlightIDs : "None"));
                System.out.println("Deletion Successful: " + (deletionSuccessful ? "SUCCESS" : "FAILURE"));

                return session;
            })
    );
    // Описание: Этот блок логирует результаты удаления билетов, проверяя корректность операции.
    // - Логика в exec(session -> ...):
    //   - Извлекает из сессии данные пользователя (username, password), списки flightIDs,
    //     removedFlightIDs и remainingFlightIDs.
    //   - Проверяет успешность удаления: ни один removedFlightID не должен присутствовать в remainingFlightIDs.
    //   - Выводит подробный лог:
    //     - Учётные данные: "User Credentials: username=jojo, password=bean".
    //     - Общее количество рейсов: "Total Flights: 5".
    //     - Удалённые flightID: "Removed Flight IDs: [123456, 789012]" или "None".
    //     - Оставшиеся flightID: "Remaining Flight IDs: [345678]" или "None".
    //     - Результат проверки: "Deletion Successful: SUCCESS" или "FAILURE".
    // - Извлечённые переменные: Нет новых, используются существующие из сессии.
    // - Логирование: Подробный вывод всех данных и результатов проверок.

    // 📋 Сценарий: объединение всех шагов с паузами
    private ScenarioBuilder scn = scenario("UC5_Delete_tickets")
            .exec(homePage) // Шаг 1: Главная страница
            .pause(3) // ⏳ Пауза 3 секунды для реалистичности
            .exec(login) // Шаг 2: Вход
            .pause(3) // ⏳ Пауза
            .exec(itinerary) // Шаг 3: Просмотр маршрута
            .pause(3) // ⏳ Пауза
            .exec(removal_itinerary) // Шаг 4: Удаление билетов
            .pause(3) // ⏳ Пауза
            .exec(logout) // Шаг 5: Выход
            .pause(3) // ⏳ Пауза
            .exec(logItinerary); // Шаг 6: Логирование
    // Описание: Определяет полный сценарий UC5_Delete_tickets, объединяя все шаги в логическую последовательность.
    // Пауза в 3 секунды между шагами эмулирует реальное поведение пользователя, делая нагрузку более естественной.
    // Каждый шаг выполняется последовательно, передавая данные через сессию.

    // ⚙️ Настройка нагрузки: 1 пользователь сразу
    private static final int USER_COUNT = 1;

    {
        // 🚀 Запуск сценария с настройкой HTTP-протокола
        setUp(
                scn.injectOpen(
                        atOnceUsers(USER_COUNT)
                )
        ).protocols(httpProtocol);
    }
    // Описание: Определяет профиль нагрузки для теста.
    // - atOnceUsers(1): Запускает 1 пользователя сразу.
    // - injectOpen: Модель открытой нагрузки, где пользователи добавляются независимо от завершения предыдущих.
    // - protocols(httpProtocol): Применяет глобальные настройки HTTP ко всем запросам.
}