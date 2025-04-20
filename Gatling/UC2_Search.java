package io.gatling.demo;

import java.time.Duration;
import java.util.*;
import io.gatling.core.Predef.*;
import io.gatling.http.Predef.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.Arrays;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

// 📜 Сценарий нагрузочного тестирования для Web Tours: вход, поиск рейса, выбор билета, выход
public class UC2_Search extends Simulation {

    // 🗂️ Фидер: циклически раздаёт 10 пользователей из users.csv (username, password)
    // Использует .circular() для бесконечного повторного использования записей
    private static final FeederBuilder<String> usersFeeder = csv("users.csv").circular();
    // Описание: Фидер загружает учётные данные из users.csv (src/test/resources) и раздаёт их
    // виртуальным пользователям по кругу. Это позволяет использовать 10 пользователей многократно
    // (до ~100 раз), избегая ошибки "Feeder is now empty". Каждая запись содержит поля username и password.

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
    ); // Для главной страницы и выхода
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
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary33c32868ece214ee22af976902a2b211"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    ); // Для поиска рейса
    private Map<CharSequence, String> headers_9 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary1d0b7362baab283a3d5c76f9fd1b6a8a"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    ); // Для выбора рейса
    // Описание: Наборы заголовков эмулируют поведение браузера для разных типов запросов (GET/POST, фреймы, формы).
    // Используются для точного соответствия реальным HTTP-запросам, отправляемым Web Tours.

    // 🏠 Шаг 1: Главная страница
    private ChainBuilder homePage = group("Home_Page").on(
            exec(
                    http("Home_Page_0")
                            .get("/cgi-bin/welcome.pl?signOff=1") // Загрузка главной страницы с параметром выхода
                            .headers(headers_0)
                            .check(substring("A Session ID has been created")) // ✅ Проверка: сессия создана
                            .check(substring("Web Tours")) // ✅ Проверка: страница Web Tours загружена
                            .check(bodyString().saveAs("tempResponse_0")) // 💾 Сохранение ответа для отладки
            )
                    .exec(
                            http("Home_Page_1")
                                    .get("/cgi-bin/nav.pl?in=home") // Загрузка навигационной панели
                                    .headers(headers_1)
                                    .check(regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession")) // ✅ Динамическая проверка: извлечение userSession
                                    .check(substring("Web Tours Navigation Bar")) // ✅ Проверка: навигационная панель загружена
                                    .check(bodyString().saveAs("tempResponse_1")) // 💾 Сохранение ответа для отладки
                    )
    );
    // Описание: Этот блок эмулирует открытие главной страницы Web Tours.
    // - Первый запрос (Home_Page_0) подтверждает создание сессии и загрузку страницы.
    // - Второй запрос (Home_Page_1) загружает панель навигации и динамически извлекает userSession (уникальный ID сессии)
    //   с помощью регулярного выражения. userSession сохраняется в сессии Gatling для использования в последующих шагах.
    // - Ответы сохраняются в tempResponse_0 и tempResponse_1 для возможной отладки.

    // 🔐 Шаг 2: Вход в систему
    private ChainBuilder loginStep = group("Login").on(
            exec(
                    feed(usersFeeder), // 🗂️ Подстановка логина и пароля из users.csv
                    http("login1")
                            .post("/cgi-bin/login.pl") // Отправка формы логина
                            .headers(headers_2)
                            .formParam("userSession", "#{userSession}") // Использование сохранённого userSession
                            .formParam("username", "#{username}") // Логин из фидера
                            .formParam("password", "#{password}") // Пароль из фидера
                            .formParam("login.x", "0")
                            .formParam("login.y", "0")
                            .formParam("JSFormSubmit", "off")
                            .check(substring("User password was correct")) // ✅ Проверка: логин успешен
                            .check(bodyString().saveAs("tempResponse_login1")) // 💾 Сохранение ответа
            )
                    .exec(
                            http("login2")
                                    .get("/cgi-bin/nav.pl?page=menu&in=home") // Обновление навигационной панели
                                    .headers(headers_1)
                                    .check(substring("Web Tours Navigation Bar")) // ✅ Проверка: панель обновлена
                                    .check(bodyString().saveAs("tempResponse_login2")) // 💾 Сохранение ответа
                    )
                    .exec(
                            http("login3")
                                    .get("/cgi-bin/login.pl?intro=true") // Загрузка страницы приветствия
                                    .headers(headers_1)
                                    .check(substring("Welcome")) // ✅ Проверка: страница приветствия загружена
                                    .check(bodyString().saveAs("tempResponse_login3")) // 💾 Сохранение ответа
                    )
    );
    // Описание: Этот блок эмулирует процесс входа пользователя в систему.
    // - feed(usersFeeder): Динамически подставляет username и password из users.csv для каждого пользователя.
    // - login1: Отправляет POST-запрос с формой логина, используя userSession из предыдущего шага и данные из фидера.
    //   Проверяет успешность логина по наличию текста "User password was correct".
    // - login2: Обновляет навигационную панель после входа, проверяя её наличие.
    // - login3: Загружает страницу приветствия, подтверждая успешный вход.
    // - Все ответы сохраняются для отладки (tempResponse_loginX).

    // ✈️ Шаг 3: Переход к поиску рейсов
    private ChainBuilder flights = group("Flights").on(
            exec(
                    http("Search Flights Page")
                            .get("/cgi-bin/welcome.pl?page=search") // Переход на страницу поиска рейсов
                            .headers(headers_0)
                            .resources(
                                    http("Nav Flights")
                                            .get("/cgi-bin/nav.pl?page=menu&in=flights") // Обновление навигационной панели
                                            .headers(headers_1),
                                    http("Reservations Welcome")
                                            .get("/cgi-bin/reservations.pl?page=welcome") // Загрузка формы поиска
                                            .headers(headers_1)
                                            .check(
                                                    regex("<option value=\"([^\"]+)\">").findAll().saveAs("cityList"), // ✅ Динамическая проверка: извлечение списка городов
                                                    substring("Find Flight").exists() // ✅ Проверка: форма поиска загружена
                                            )
                            )
            )
                    .exec(session -> {
                        // ⚙️ Динамическая генерация параметров рейса
                        String userSession = session.getString("userSession");
                        if (userSession == null) {
                            System.err.println("userSession is not defined or not found");
                            userSession = "defaultSession_" + System.nanoTime(); // Запасной ID сессии
                        }

                        // 🎲 Инициализация генератора случайных чисел на основе userSession
                        Random random = new Random(userSession.hashCode());

                        // 🏙️ Получение списка городов из сессии
                        List<String> cities = session.get("cityList");
                        if (cities == null || cities.size() < 2) {
                            System.out.println("Not enough cities, using fallback list");
                            cities = Arrays.asList("Denver", "London", "Paris", "New York", "Tokyo", "Los Angeles", "Frankfurt", "Sydney", "Portland", "Seattle");
                        }

                        // 📜 Логирование списка городов
                        System.out.println("Collected cities: " + cities);

                        // 🛫 Выбор города отправления
                        int departIndex = Math.abs(userSession.hashCode()) % cities.size();
                        String departCity = cities.get(departIndex);

                        // 🛬 Выбор города прибытия (не совпадает с городом отправления)
                        int arrivalIndex = (Math.abs(userSession.hashCode() + 1)) % cities.size();
                        if (arrivalIndex == departIndex && cities.size() > 1) {
                            arrivalIndex = (arrivalIndex + 1) % cities.size();
                        }
                        String arrivalCity = cities.get(arrivalIndex);

                        // 📅 Генерация дат вылета и возвращения
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, random.nextInt(10) + 1);
                        String departDate = dateFormat.format(calendar.getTime());
                        calendar.add(Calendar.DAY_OF_YEAR, random.nextInt(5) + 1);
                        String returnDate = dateFormat.format(calendar.getTime());

                        // 🪑 Выбор предпочтений по местам и классу
                        List<String> seatPreferences = Arrays.asList("Aisle", "Window", "None");
                        List<String> seatTypes = Arrays.asList("First", "Business", "Coach");
                        String seatPref = seatPreferences.get(random.nextInt(seatPreferences.size()));
                        String seatType = seatTypes.get(random.nextInt(seatTypes.size()));

                        // 👥 Случайное количество пассажиров (1–3)
                        int numPassengers = random.nextInt(3) + 1;
                        System.out.println("Generated numPassengers: " + numPassengers);

                        // 💾 Сохранение всех параметров в сессию
                        return session
                                .set("departCity", departCity)
                                .set("arrivalCity", arrivalCity)
                                .set("departDate", departDate)
                                .set("returnDate", returnDate)
                                .set("seatPref", seatPref)
                                .set("seatType", seatType)
                                .set("numPassengers", String.valueOf(numPassengers));
                    })
    );
    // Описание: Этот блок переходит на страницу поиска рейсов и динамически генерирует параметры.
    // - Первый запрос загружает страницу поиска и связанные ресурсы (навигационная панель и форма).
    // - Динамическая проверка: извлекает список городов (<option value="...">) с помощью regex и сохраняет в cityList.
    // - Логика в exec(session -> ...):
    //   - Проверяет наличие userSession, создавая запасной ID, если он отсутствует.
    //   - Использует Random с seed на основе userSession для воспроизводимости.
    //   - Выбирает города отправления и прибытия из cityList, избегая совпадений.
    //   - Генерирует даты вылета (через 1–10 дней) и возвращения (через 1–5 дней после вылета).
    //   - Случайно выбирает предпочтения по местам, классу и числу пассажиров (1–3).
    //   - Сохраняет все параметры в сессию для использования в следующем шаге.

    // 🔍 Шаг 4: Поиск рейса
    private ChainBuilder findFlight = group("Find_Flight").on(
            exec(
                    http("Find Flight Request")
                            .post("/cgi-bin/reservations.pl") // Отправка формы поиска рейса
                            .headers(headers_8)
                            .formParam("advanceDiscount", "0")
                            .formParam("depart", "#{departCity}") // Город отправления из сессии
                            .formParam("departDate", "#{departDate}") // Дата вылета
                            .formParam("arrive", "#{arrivalCity}") // Город прибытия
                            .formParam("returnDate", "#{returnDate}") // Дата возвращения
                            .formParam("numPassengers", "#{numPassengers}") // Количество пассажиров
                            .formParam("seatPref", "#{seatPref}") // Предпочтение по месту
                            .formParam("seatType", "#{seatType}") // Класс
                            .formParam("findFlights.x", "56")
                            .formParam("findFlights.y", "12")
                            .formParam(".cgifields", "roundtrip")
                            .formParam(".cgifields", "seatPref")
                            .formParam(".cgifields", "seatType")
                            .check(
                                    regex("name=\"outboundFlight\" value=\"([^\"]+)\">").findAll().saveAs("outboundFlight"), // ✅ Динамическая проверка: извлечение доступных рейсов
                                    substring("outboundFlight").exists() // ✅ Проверка: рейсы найдены
                            )
            )
                    .exec(session -> {
                        // ⚙️ Выбор случайного рейса из доступных
                        List<String> flights = session.get("outboundFlight");
                        String randomFlight;

                        if (flights != null && !flights.isEmpty()) {
                            Random random = new Random();
                            randomFlight = flights.get(random.nextInt(flights.size()));
                            System.out.println("Selected randomFlight: " + randomFlight);
                        } else {
                            randomFlight = "Not Found";
                            System.err.println("No outbound flights found");
                        }

                        // 💾 Сохранение выбранного рейса в сессию
                        return session.set("randomFlight", randomFlight);
                    })
    );
    // Описание: Этот блок выполняет поиск рейса на основе параметров из предыдущего шага.
    // - POST-запрос отправляет форму с параметрами из сессии (города, даты, места, класс, пассажиры).
    // - Динамическая проверка: извлекает все доступные рейсы (outboundFlight) с помощью regex и сохраняет их в список outboundFlight.
    // - Проверяет наличие рейсов в ответе (substring("outboundFlight")).
    // - Логика в exec(session -> ...):
    //   - Получает список рейсов из сессии.
    //   - Случайно выбирает один рейс (randomFlight) или устанавливает "Not Found", если рейсы отсутствуют.
    //   - Сохраняет randomFlight в сессию для следующего шага.
    // - Логирование выбранного рейса для отладки.

    // 🎟️ Шаг 5: Выбор рейса
    private ChainBuilder flightSelection = group("Flight_Selection").on(
            exec(
                    http("Flight Selection Request")
                            .post("/cgi-bin/reservations.pl") // Отправка формы выбора рейса
                            .headers(headers_9)
                            .formParam("outboundFlight", "#{randomFlight}") // Выбранный рейс
                            .formParam("numPassengers", "#{numPassengers}") // Количество пассажиров
                            .formParam("advanceDiscount", "0")
                            .formParam("seatType", "#{seatType}") // Класс
                            .formParam("seatPref", "#{seatPref}") // Предпочтение по месту
                            .formParam("reserveFlights.x", "55")
                            .formParam("reserveFlights.y", "6")
                            .check(
                                    substring("Flight Reservation").exists(), // ✅ Проверка: страница бронирования загружена
                                    substring("Payment Details").exists(), // ✅ Проверка: данные оплаты присутствуют
                                    regex("name=\"outboundFlight\" value=\"([^\"]+)\"").saveAs("returnedFlight"), // ✅ Динамическая проверка: извлечение подтверждённого рейса
                                    bodyString().saveAs("tempResponse_flightSelection") // 💾 Сохранение ответа
                            )
            )
                    .exec(session -> {
                        // 📜 Логирование результатов выбора рейса
                        String departCity = session.getString("departCity");
                        String arrivalCity = session.getString("arrivalCity");
                        String username = session.getString("username");
                        String password = session.getString("password");
                        String selectedFlight = session.getString("randomFlight");
                        String returnedFlight = session.getString("returnedFlight");
                        String response = session.getString("tempResponse_flightSelection");
                        boolean flightMatch = selectedFlight != null && selectedFlight.equals(returnedFlight);

                        System.out.println("Flight Route: " + departCity + " to " + arrivalCity);
                        System.out.println("User Credentials: username=" + username + ", password=" + password);
                        System.out.println("Selected Flight: " + selectedFlight);
                        System.out.println("Returned Flight from Server: " + returnedFlight);
                        System.out.println("Flight Match: " + (flightMatch ? "SUCCESS" : "FAILURE"));
                        System.out.println("Flight Selection Response: " + response);

                        return session;
                    })
    );
    // Описание: Этот блок подтверждает выбор рейса и логирует результаты.
    // - POST-запрос отправляет форму с выбранным рейсом (randomFlight) и параметрами из сессии.
    // - Динамические проверки:
    //   - Подтверждает загрузку страницы бронирования ("Flight Reservation").
    //   - Проверяет наличие данных оплаты ("Payment Details").
    //   - Извлекает подтверждённый рейс (returnedFlight) с помощью regex и сохраняет в сессию.
    // - Логика в exec(session -> ...):
    //   - Извлекает из сессии города, логин, пароль, выбранный и подтверждённый рейсы, и полный ответ сервера.
    //   - Сравнивает selectedFlight и returnedFlight, логируя результат ("SUCCESS" или "FAILURE").
    //   - Выводит подробный лог: маршрут, учётные данные, рейсы, результат проверки и ответ сервера.
    // - Ответ сохраняется в tempResponse_flightSelection для отладки.

    // 🚪 Шаг 6: Выход из системы
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
    // - GET-запрос завершает сессию, возвращая пользователя на главную страницу.
    // - Проверяет наличие текста "Web Tours" и опционально — текста о выборе логина/пароля.
    // - Сохраняет ответ в tempResponse_7 для отладки.

    // 📋 Сценарий: объединение всех шагов с паузами
    private ScenarioBuilder scn = scenario("UC2_Search")
            .exec(homePage) // Шаг 1: Главная страница
            .pause(3) // ⏳ Пауза 3 секунды для реалистичности
            .exec(loginStep) // Шаг 2: Вход
            .pause(3) // ⏳ Пауза
            .exec(flights) // Шаг 3: Поиск рейсов
            .pause(3) // ⏳ Пауза
            .exec(findFlight) // Шаг 4: Выбор рейса
            .pause(3) // ⏳ Пауза
            .exec(flightSelection) // Шаг 5: Подтверждение рейса
            .pause(3) // ⏳ Пауза
            .exec(logout); // Шаг 6: Выход
    // Описание: Определяет полный сценарий UC2_Search, объединяя все шаги в логическую последовательность.
    // Пауза в 3 секунды между шагами эмулирует реальное поведение пользователя, делая нагрузку более естественной.
    // Каждый шаг выполняется последовательно, передавая данные через сессию.

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
    // - Это обеспечивает ~100 использований 10 пользователей (10 циклов по 10 пользователей + 2 дополнительных).
    // - injectOpen: Модель открытой нагрузки, где пользователи добавляются независимо от завершения предыдущих.
    // - protocols(httpProtocol): Применяет глобальные настройки HTTP ко всем запросам.
}