package io.gatling.demo;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

// 📜 Сценарий нагрузочного тестирования для Web Tours: покупка билета от входа до выхода
public class UC3_Buy_tickets extends Simulation {

    // 🌐 Настройка HTTP-протокола: базовый URL, заголовки, поведение браузера
    public static HttpProtocolBuilder httpProtocol = http
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
    public static Map<CharSequence, String> headers_0 = Map.ofEntries(
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1"),
            Map.entry("Upgrade-Insecure-Requests", "1")
    ); // Для главной страницы, выхода и переходов
    public static Map<CharSequence, String> headers_1 = Map.ofEntries(
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Upgrade-Insecure-Requests", "1")
    ); // Для навигационных фреймов
    public static Map<CharSequence, String> headers_2 = Map.ofEntries(
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1"),
            Map.entry("Upgrade-Insecure-Requests", "1")
    ); // Для POST-запроса логина
    public static Map<CharSequence, String> headers_8 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundaryc0b0b6ec6c43edd6b1420a5e10375a3e"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1"),
            Map.entry("Upgrade-Insecure-Requests", "1")
    ); // Для поиска рейса
    public static Map<CharSequence, String> headers_9 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundarycb59c73863c264eec9c7faa52bc0c2c4"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1"),
            Map.entry("Upgrade-Insecure-Requests", "1")
    ); // Для выбора рейса
    public static Map<CharSequence, String> headers_10 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary7e65d094c09310774c7b8aac56e3f0f"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1"),
            Map.entry("Upgrade-Insecure-Requests", "1")
    ); // Для оплаты билета
    public static Map<CharSequence, String> headers_14 = Map.ofEntries(
            Map.entry("Accept", "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5"),
            Map.entry("Priority", "u=5, i"),
            Map.entry("Sec-Fetch-Dest", "image"),
            Map.entry("Sec-Fetch-Mode", "no-cors"),
            Map.entry("Sec-Fetch-Site", "same-origin")
    ); // Для загрузки изображений
    // Описание: Наборы заголовков эмулируют поведение браузера для разных типов запросов (GET/POST, фреймы, формы, изображения).
    // Используются для точного соответствия реальным HTTP-запросам, отправляемым Web Tours.

    // 🗂️ Фидер: циклически раздаёт пользователей из users.csv
    public static final FeederBuilder<String> usersFeeder = csv("users.csv").circular();
    // Описание: Фидер загружает данные пользователей из users.csv (src/test/resources) и раздаёт их
    // виртуальным пользователям по кругу. Каждая запись содержит поля username, password, firstName,
    // lastName, address1, address2. Используется для авторизации и заполнения формы оплаты.

    // 🗂️ Вспомогательные данные
    public static final Random random = new Random();
    // Описание: Объект Random используется для генерации случайных данных (например, номера кредитной карты,
    // выбора рейса, дат). Инициализируется один раз для всех пользователей.

    // 🏠 Шаг 1: Загрузка главной страницы
    public static ChainBuilder homePage = group("Home_Page").on(
            exec(
                    http("home_page_0")
                            .get("/cgi-bin/welcome.pl?signOff=1") // Загрузка главной страницы с параметром выхода
                            .headers(headers_0)
                            .check(
                                    substring("Web Tours").exists(), // ✅ Проверка: страница Web Tours загружена
                                    bodyString().saveAs("tempResponse_0") // 💾 Сохранение ответа для отладки
                            )
            ),
            exec(
                    http("home_page_1")
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
    public static ChainBuilder login = group("Login").on(
            feed(usersFeeder), // 🗂️ Подстановка данных пользователя из users.csv
            exec(session -> {
                // ⚙️ Формирование полного имени пассажира для формы оплаты
                String pass1 = session.getString("firstName") + " " + session.getString("lastName");
                return session.set("pass1", pass1);
            }),
            exec(
                    http("login_0")
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
                                    bodyString().saveAs("tempResponse_login_0") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("login_1")
                            .get("/cgi-bin/nav.pl?page=menu&in=home") // Обновление навигационной панели
                            .headers(headers_1)
                            .check(
                                    substring("Web Tours Navigation Bar").exists(), // ✅ Проверка: панель обновлена
                                    bodyString().saveAs("tempResponse_login_1") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("login_2")
                            .get("/cgi-bin/login.pl?intro=true") // Загрузка страницы приветствия
                            .headers(headers_1)
                            .check(
                                    substring("Welcome").exists(), // ✅ Проверка: страница приветствия загружена
                                    bodyString().saveAs("tempResponse_login_2") // 💾 Сохранение ответа
                            )
            )
    );
    // Описание: Этот блок эмулирует процесс входа пользователя в систему.
    // - feed(usersFeeder): Динамически подставляет данные (username, password, firstName, lastName, address1, address2)
    //   из users.csv для каждого виртуального пользователя.
    // - exec(session -> ...): Создаёт переменную pass1 (например, "Jo Jo") путём конкатенации firstName и lastName
    //   для использования в форме оплаты.
    // - login_0: Отправляет POST-запрос с формой логина, используя userSession и данные из фидера.
    //   Проверяет успешность авторизации по тексту "User password was correct" и сохраняет ответ в tempResponse_login_0.
    // - login_1: Обновляет навигационную панель после входа, проверяя её наличие ("Web Tours Navigation Bar"),
    //   сохраняет ответ в tempResponse_login_1.
    // - login_2: Загружает страницу приветствия, подтверждая вход текстом "Welcome", сохраняет ответ в tempResponse_login_2.
    // - Извлечённые переменные:
    //   - pass1: Полное имя пассажира (например, "Jo Jo") для формы оплаты.
    //   - username, password, firstName, lastName, address1, address2: Из users.csv, используются в login и payment.
    // - Логирование: Отсутствует, но username и password логируются позже в logFlightSelection.

    // ✈️ Шаг 3: Переход к поиску рейсов
    public static ChainBuilder flights = group("Flights").on(
            exec(
                    http("flights_0")
                            .get("/cgi-bin/welcome.pl?page=search") // Переход на страницу поиска рейсов
                            .headers(headers_0)
                            .resources(
                                    http("flights_1")
                                            .get("/cgi-bin/nav.pl?page=menu&in=flights") // Обновление навигационной панели
                                            .headers(headers_1),
                                    http("flights_2")
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
    // Описание: Этот блок переходит на страницу поиска рейсов и динамически генерирует параметры поиска.
    // - Flights_0: Загружает страницу поиска (welcome.pl?page=search) с параллельной загрузкой ресурсов.
    // - Flights_1: Обновляет навигационную панель для раздела рейсов.
    // - Flights_2: Загружает форму поиска, извлекая список городов из выпадающего меню
    //   (<option value="...">) с помощью regex и сохраняя в cityList (например, ["Denver", "London", ...]).
    //   Проверяет наличие текста "Find Flight" для подтверждения загрузки формы.
    // - Логика в exec(session -> ...):
    //   - Проверяет наличие userSession, создавая запасной ID, если он отсутствует.
    //   - Использует Random с seed на основе userSession для воспроизводимости.
    //   - Проверяет cityList, используя запасной список городов, если он пуст или содержит менее 2 городов.
    //   - Выбирает города отправления (departCity) и прибытия (arrivalCity), избегая совпадений.
    //   - Генерирует даты вылета (departDate, через 1–10 дней) и возвращения (returnDate, через 1–5 дней после вылета).
    //   - Случайно выбирает предпочтения по местам (seatPref), классу (seatType) и числу пассажиров (numPassengers, 1–3).
    //   - Сохраняет все параметры в сессию для использования в следующих шагах.
    // - Извлечённые переменные:
    //   - cityList: Список городов (например, ["Denver", "London", ...]).
    //   - departCity, arrivalCity: Города отправления и прибытия (например, "Sydney", "Zurich").
    //   - departDate, returnDate: Даты в формате MM/dd/yyyy (например, "04/25/2025", "04/28/2025").
    //   - seatPref: Предпочтение места ("Aisle", "Window", "None").
    //   - seatType: Класс ("First", "Business", "Coach").
    //   - numPassengers: Число пассажиров (1–3).
    // - Логирование:
    //   - Выводит список городов: "Collected cities: [Denver, London, ...]".
    //   - Выводит число пассажиров: "Generated numPassengers: 2".

    // 🔍 Шаг 4: Поиск рейсов
    public static ChainBuilder findFlight = group("Find_Flight").on(
            exec(
                    http("find_flight_0")
                            .post("/cgi-bin/reservations.pl") // Отправка формы поиска рейса
                            .headers(headers_8)
                            .formParam("advanceDiscount", "0") // Без скидки
                            .formParam("depart", "#{departCity}") // Город отправления из сессии
                            .formParam("departDate", "#{departDate}") // Дата вылета
                            .formParam("arrive", "#{arrivalCity}") // Город прибытия
                            .formParam("returnDate", "#{returnDate}") // Дата возвращения
                            .formParam("numPassengers", "#{numPassengers}") // Количество пассажиров
                            .formParam("seatPref", "#{seatPref}") // Предпочтение по месту
                            .formParam("seatType", "#{seatType}") // Класс
                            .formParam("findFlights.x", "46") // Координаты кнопки
                            .formParam("findFlights.y", "11")
                            .check(
                                    regex("value=\"(\\d+;\\d+;\\d{2}/\\d{2}/\\d{4})\"").findAll().saveAs("outboundFlights"), // ✅ Динамическая проверка: извлечение доступных рейсов
                        //            regex("from ([^ ]+) to").saveAs("paymentDepartCity"), // ✅ Динамическая проверка: извлечение города отправления
                                    bodyString().saveAs("tempResponse_findFlight") // 💾 Сохранение ответа
                            )
            ),
            exec(session -> {
                // 📜 Логирование результатов поиска
                String response = session.getString("tempResponse_findFlight");
                List<String> outboundFlights = session.get("outboundFlights");
                System.out.println("Find Flight Response: " + response);
                System.out.println("Extracted outboundFlights: " + (outboundFlights != null ? outboundFlights : "null"));
                if (outboundFlights == null || outboundFlights.isEmpty()) {
                    System.err.println("Warning: No outbound flights extracted in findFlight");
                }
                return session;
            })
    );
    // Описание: Этот блок выполняет поиск рейсов на основе параметров из шага flights.
    // - Find_Flight_0: Отправляет POST-запрос с формой поиска, используя параметры из сессии (departCity,
    //   arrivalCity, departDate, returnDate, numPassengers, seatPref, seatType).
    //   Извлекает список доступных рейсов (outboundFlights) в формате "flightNumber;cost;date"
    //   (например, ["452;852;04/25/2025", "453;900;04/25/2025"]) с помощью regex.
    //   Сохраняет полный HTML-ответ в tempResponse_findFlight для отладки.
    // - Логика в exec(session -> ...):
    //   - Получает ответ сервера (tempResponse_findFlight) и список рейсов (outboundFlights).
    //   - Выводит полный ответ и извлечённые рейсы для отладки.
    //   - Логирует предупреждение, если рейсы не найдены.
    // - Извлечённые переменные:
    //   - outboundFlights: Список строк с данными рейсов (например, ["452;852;04/25/2025", ...]).
    // - Логирование:
    //   - Выводит полный ответ: "Find Flight Response: <HTML>".
    //   - Выводит список рейсов: "Extracted outboundFlights: [452;852;04/25/2025, ...]".
    //   - Предупреждение при отсутствии рейсов: "Warning: No outbound flights extracted in findFlight".

    // 🎫 Шаг 5: Выбор рейса
    public static ChainBuilder flightSelection = group("Flight_Selection").on(
            exec(session -> {
                // ⚙️ Выбор случайного рейса из доступных
                List<String> outboundFlights = session.get("outboundFlights");
                if (outboundFlights == null || outboundFlights.isEmpty()) {
                    System.err.println("Error: No outbound flights found in session, skipping flight selection");
                    return session.set("randomFlight", "");
                }
                String randomFlight = outboundFlights.get(new Random().nextInt(outboundFlights.size()));
                System.out.println("Selected randomFlight: " + randomFlight);
                return session.set("randomFlight", randomFlight);
            }),
            doIf(session -> {
                // 🔄 Условие: выполнять запрос, только если рейс выбран
                String randomFlight = session.getString("randomFlight");
                return randomFlight != null && !randomFlight.isEmpty();
            }).then(
                    exec(
                            http("flight_selection_0")
                                    .post("/cgi-bin/reservations.pl") // Отправка формы выбора рейса
                                    .headers(headers_9)
                                    .formParam("outboundFlight", "#{randomFlight}") // Выбранный рейс
                                    .formParam("numPassengers", "#{numPassengers}") // Количество пассажиров
                                    .formParam("advanceDiscount", "0") // Без скидки
                                    .formParam("seatType", "#{seatType}") // Класс
                                    .formParam("seatPref", "#{seatPref}") // Предпочтение по месту
                                    .formParam("reserveFlights.x", "43") // Координаты кнопки
                                    .formParam("reserveFlights.y", "11")
                                    .check(
                                            regex("value=\"(\\d+;\\d+;\\d{2}/\\d{2}/\\d{4})\"").saveAs("returnedFlight"), // ✅ Динамическая проверка: извлечение подтверждённого рейса
                                            substring("Flight Reservation").exists(), // ✅ Проверка: страница бронирования загружена
                                            bodyString().saveAs("tempResponse_flightSelection") // 💾 Сохранение ответа
                                    )
                    )
            )
    );
    // Описание: Этот блок выбирает случайный рейс и подтверждает его резервирование.
    // - Первый exec(session -> ...):
    //   - Получает список рейсов (outboundFlights) из сессии.
    //   - Если рейсы отсутствуют, логирует ошибку и устанавливает randomFlight в пустую строку.
    //   - Иначе выбирает случайный рейс (например, "452;852;04/25/2025") и сохраняет в randomFlight.
    //   - Логирует выбранный рейс: "Selected randomFlight: 452;852;04/25/2025".
    // - doIf: Проверяет, что randomFlight не пустой, чтобы избежать ненужного запроса.
    // - Flight_Selection_0: Отправляет POST-запрос с формой резервирования, используя randomFlight и параметры
    //   из сессии (numPassengers, seatType, seatPref).
    //   Извлекает подтверждённый рейс (returnedFlight) в том же формате (например, "452;852;04/25/2025").
    //   Проверяет наличие текста "Flight Reservation" для подтверждения перехода на страницу бронирования.
    //   Сохраняет ответ в tempResponse_flightSelection.
    // - Извлечённые переменные:
    //   - randomFlight: Выбранный рейс (например, "452;852;04/25/2025").
    //   - returnedFlight: Подтверждённый рейс (например, "452;852;04/25/2025").
    // - Логирование:
    //   - Выводит выбранный рейс: "Selected randomFlight: 452;852;04/25/2025".
    //   - Ошибка при отсутствии рейсов: "Error: No outbound flights found in session, skipping flight selection".

    // 💳 Шаг 6: Оплата билета
    public static ChainBuilder payment = group("Payment").on(
            doIf(session -> {
                // 🔄 Условие: выполнять запрос, только если рейс выбран
                String randomFlight = session.getString("randomFlight");
                return randomFlight != null && !randomFlight.isEmpty();
            }).then(
                    exec(session -> {
                        // ⚙️ Генерация данных кредитной карты
                        String creditCard = random.ints(16, 0, 10)
                                .mapToObj(String::valueOf)
                                .collect(Collectors.joining());
                        String month = String.format("%02d", random.nextInt(12) + 1);
                        String year = String.valueOf(random.nextInt(6) + 25);
                        String expDate = month + "/" + year;
                        return session
                                .set("creditCard", creditCard)
                                .set("expDate", expDate);
                    }),
                    exec(
                            http("payment_0")
                                    .post("/cgi-bin/reservations.pl") // Отправка формы оплаты
                                    .headers(headers_10)
                                    .formParam("firstName", "#{firstName}") // Имя из users.csv
                                    .formParam("lastName", "#{lastName}") // Фамилия из users.csv
                                    .formParam("address1", "#{address1}") // Адрес 1 из users.csv
                                    .formParam("address2", "#{address2}") // Адрес 2 из users.csv
                                    .formParam("pass1", "#{pass1}") // Полное имя из шага login
                                    .formParam("creditCard", "#{creditCard}") // Сгенерированный номер карты
                                    .formParam("expDate", "#{expDate}") // Срок действия карты
                                    .formParam("oldCCOption", "") // Пустое поле для старой карты
                                    .formParam("numPassengers", "#{numPassengers}") // Количество пассажиров
                                    .formParam("seatType", "#{seatType}") // Класс
                                    .formParam("seatPref", "#{seatPref}") // Предпочтение по месту
                                    .formParam("outboundFlight", "#{randomFlight}") // Выбранный рейс
                                    .formParam("advanceDiscount", "0") // Без скидки
                                    .formParam("returnFlight", "") // Нет обратного рейса
                                    .formParam("JSFormSubmit", "off")
                                    .formParam("buyFlights.x", "30") // Координаты кнопки
                                    .formParam("buyFlights.y", "12")
                                    .formParam(".cgifields", "saveCC") // Поле для сохранения карты
                                    .check(
                                            substring("Thank you for booking").exists(), // ✅ Проверка: покупка подтверждена
                                            regex("Flight (\\d+)").saveAs("paymentFlight"), // ✅ Динамическая проверка: извлечение номера рейса
                                            regex("from (.+?) to").saveAs("paymentDepartCity"), // ✅ Динамическая проверка: извлечение города отправления
                                            regex("to ([^<.]+)").saveAs("paymentArrivalCity"), // ✅ Динамическая проверка: извлечение города прибытия
                                            bodyString().saveAs("tempResponse_payment") // 💾 Сохранение ответа
                                    )
                    ),
                    exec(session -> {
                        // 📜 Логирование ответа сервера
                        String response = session.getString("tempResponse_payment");
               //         System.out.println("Payment Response: " + response);
                        return session;
                    })
            )
    );
    // Описание: Этот блок завершает покупку билета, отправляя данные пассажира и кредитной карты.
    // - doIf: Проверяет, что randomFlight не пустой, чтобы избежать ненужного запроса.
    // - Первый exec(session -> ...):
    //   - Генерирует случайный 16-значный номер кредитной карты (creditCard, например, "1234567890123456").
    //   - Генерирует срок действия карты (expDate, например, "04/27") с месяцем (01–12) и годом (25–30).
    //   - Сохраняет creditCard и expDate в сессию.
    // - Payment_0: Отправляет POST-запрос с формой оплаты, используя данные из users.csv (firstName,
    //   lastName, address1, address2), pass1 из шага login, сгенерированные creditCard и expDate,
    //   а также параметры из сессии (numPassengers, seatType, seatPref, randomFlight).
    //   Проверяет успешность покупки текстом "Thank you for booking".
    //   Извлекает:
    //     - Номер рейса (paymentFlight, например, "452") с помощью regex "Flight (\\d+)".
    //     - Город отправления (paymentDepartCity, например, "Sydney") с помощью regex "from ([^ ]+) to".
    //     - Город прибытия (paymentArrivalCity, например, "Zurich") с помощью regex "to ([^<.]+)".
    //   Сохраняет ответ в tempResponse_payment.
    // - Второй exec(session -> ...): Выводит полный ответ сервера для отладки.
    // - Извлечённые переменные:
    //   - creditCard: Номер карты (например, "1234567890123456").
    //   - expDate: Срок действия (например, "04/27").
    //   - paymentFlight: Номер рейса (например, "452").
    //   - paymentDepartCity: Город отправления (например, "Sydney").
    //   - paymentArrivalCity: Город прибытия (например, "Zurich").
    // - Логирование:
    //   - Выводит полный ответ: "Payment Response: <HTML>".

    // 🗓️ Шаг 7: Просмотр маршрута
    public static ChainBuilder itinerary = group("Itinerary").on(
            exec(
                    http("itinerary_0")
                            .get("/cgi-bin/welcome.pl?page=itinerary") // Переход на страницу маршрута
                            .headers(headers_0)
                            .check(
                                    substring("already logged").exists(), // ✅ Проверка: пользователь авторизован
                                    bodyString().saveAs("tempResponse_itinerary0") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("itinerary_1")
                            .get("/cgi-bin/nav.pl?page=menu&in=itinerary") // Обновление навигационной панели
                            .headers(headers_1)
                            .check(
                                    substring("Itinerary").exists(), // ✅ Проверка: панель маршрута загружена
                                    bodyString().saveAs("tempResponse_itinerary1") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("itinerary_2")
                            .get("/cgi-bin/itinerary.pl") // Загрузка списка бронирований
                            .headers(headers_1)
                            .check(
                                    substring("Flights List").exists(), // ✅ Проверка: список рейсов загружен
                                    bodyString().saveAs("tempResponse_itinerary2") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("itinerary_3")
                            .get("/WebTours/images/cancelreservation.gif") // Загрузка изображения кнопки отмены
                            .headers(headers_14)
                            .check(
                                    bodyBytes().saveAs("tempResponse_itinerary3") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("itinerary_4")
                            .get("/WebTours/images/cancelallreservations.gif") // Загрузка изображения полной отмены
                            .headers(headers_14)
                            .check(
                                    bodyBytes().saveAs("tempResponse_itinerary4") // 💾 Сохранение ответа
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

    // 🔓 Шаг 8: Выход из системы
    public static ChainBuilder logout = group("Logout").on(
            exec(
                    http("logout_0")
                            .get("/cgi-bin/welcome.pl?signOff=1") // Запрос на выход
                            .headers(headers_0)
                            .check(
                                    substring("Session ID has been created").exists(), // ✅ Проверка: возврат на главную страницу
                                    bodyString().saveAs("tempResponse_logout1") // 💾 Сохранение ответа
                            )
            ),
            exec(
                    http("logout_1")
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
    //   Проверяет текст "Web Tours" и сохраняет ответ в tempResponse_logout1.
    // - Logout_1: Обновляет навигационную панель, проверяя её наличие ("Web Tours Navigation Bar")
    //   и существование нового userSession в скрытом поле формы.
    //   Сохраняет ответ в tempResponse_logout2.
    // - Извлечённые переменные:
    //   - tempResponse_logout1, tempResponse_logout2: HTML-ответы.
    // - Логирование: Отсутствует.

    // 📜 Шаг 9: Логирование результатов выбора рейса
    public static ChainBuilder logFlightSelection = group("Log_Flight_Selection").on(
            exec(session -> {
                // ⚙️ Извлечение данных из сессии для логирования
                String username = session.getString("username");
                String password = session.getString("password");
                String randomFlight = session.getString("randomFlight");
                String returnedFlight = session.getString("returnedFlight");
                String paymentFlight = session.getString("paymentFlight");
                String paymentDepartCity = session.getString("paymentDepartCity");
                String paymentArrivalCity = session.getString("paymentArrivalCity");
                String departCity = session.getString("departCity");
                String arrivalCity = session.getString("arrivalCity");

                // 🔍 Извлечение номера рейса из randomFlight
                String randomFlightNumber = randomFlight != null && randomFlight.contains(";") ? randomFlight.split(";")[0] : randomFlight;

                // ✅ Проверка соответствия рейсов
                boolean flightMatch = randomFlight != null && !randomFlight.isEmpty() && randomFlight.equals(returnedFlight);
                boolean paymentFlightMatch = randomFlightNumber != null && paymentFlight != null && randomFlightNumber.equals(paymentFlight);
                boolean citiesMatch = paymentDepartCity != null && paymentArrivalCity != null &&
                        paymentDepartCity.equals(departCity) && paymentArrivalCity.equals(arrivalCity);

                // 📜 Логирование результатов
                System.out.println("User Credentials: username=" + username + ", password=" + password);
                System.out.println("Selected Flight: " + randomFlight);
                System.out.println("Returned Flight: " + returnedFlight);
                System.out.println("Flight Match: " + (flightMatch ? "SUCCESS" : "FAILURE"));
                System.out.println("Payment Flight: " + paymentFlight);
                System.out.println("Payment Flight Match: " + (paymentFlightMatch ? "SUCCESS" : "FAILURE"));
                System.out.println("Payment Depart City: " + paymentDepartCity);
                System.out.println("Payment Arrival City: " + paymentArrivalCity);
                System.out.println("Cities Match: " + (citiesMatch ? "SUCCESS" : "FAILURE"));

                return session;
            })
    );
    // Описание: Этот блок логирует результаты выбора и оплаты рейса, проверяя корректность данных.
    // - Логика в exec(session -> ...):
    //   - Извлекает из сессии данные пользователя (username, password), рейсы (randomFlight,
    //     returnedFlight, paymentFlight), города (paymentDepartCity, paymentArrivalCity,
    //     departCity, arrivalCity).
    //   - Извлекает номер рейса (randomFlightNumber) из randomFlight, отделяя первую часть до ";"
    //     (например, "452" из "452;852;04/25/2025").
    //   - Проверяет:
    //     - flightMatch: Совпадают ли randomFlight и returnedFlight (выбранный и подтверждённый рейсы).
    //     - paymentFlightMatch: Совпадает ли номер рейса (randomFlightNumber) с paymentFlight.
    //     - citiesMatch: Совпадают ли города (paymentDepartCity с departCity, paymentArrivalCity с arrivalCity).
    //   - Выводит подробный лог:
    //     - Учётные данные: "User Credentials: username=jojo, password=bean".
    //     - Выбранный рейс: "Selected Flight: 452;852;04/25/2025".
    //     - Подтверждённый рейс: "Returned Flight: 452;852;04/25/2025".
    //     - Результат проверки рейсов: "Flight Match: SUCCESS" или "FAILURE".
    //     - Номер рейса при оплате: "Payment Flight: 452".
    //     - Результат проверки номера рейса: "Payment Flight Match: SUCCESS" или "FAILURE".
    //     - Города при оплате: "Payment Depart City: Sydney", "Payment Arrival City: Zurich".
    //     - Результат проверки городов: "Cities Match: SUCCESS" или "FAILURE".
    // - Извлечённые переменные: Нет новых, используются существующие из сессии.
    // - Логирование: Подробный вывод всех данных и результатов проверок.

    // 📋 Сценарий: объединение всех шагов с паузами и пейсингом
    public static ScenarioBuilder scn = scenario("UC3_Buy_ticket")
            .pace(Duration.ofSeconds(30)) // ⏲️ Пейсинг: каждый цикл занимает ровно 30 секунд
            .exec(homePage) // Шаг 1: Главная страница
            .pause(3) // ⏳ Пауза 3 секунды для реалистичности
            .exec(login) // Шаг 2: Вход
            .pause(3) // ⏳ Пауза
            .exec(flights) // Шаг 3: Поиск рейсов
            .pause(3) // ⏳ Пауза
            .exec(findFlight) // Шаг 4: Выбор рейса
            .pause(3) // ⏳ Пауза
            .exec(flightSelection) // Шаг 5: Подтверждение рейса
            .pause(3) // ⏳ Пауза
            .exec(payment) // Шаг 6: Оплата
            .pause(3) // ⏳ Пауза
            .exec(itinerary) // Шаг 7: Просмотр маршрута
            .pause(3) // ⏳ Пауза
            .exec(logout) // Шаг 8: Выход
            .pause(3) // ⏳ Пауза
            .exec(logFlightSelection); // Шаг 9: Логирование

// 📝 Описание:
// - Сценарий: Симуляция покупки билета (UC3_Buy_ticket) с шагами от главной страницы до логирования.
// - Пейсинг: Каждый цикл сценария занимает ровно 30 секунд (pace), добавляя паузу, если выполнение завершилось раньше.
// - Паузы: Между шагами добавлены фиксированные паузы по 3 секунды для реалистичности поведения пользователей.


    // ⚙️ Конфигурация нагрузки: 5 пользователей за 5 секунду
    public static final int USER_COUNT = 10;
    public static final Duration TEST_DURATION = Duration.ofSeconds(10);

    {
        // 🚀 Инициализация сценария с настройкой HTTP-протокола
        setUp(
                scn.injectClosed(
                        constantConcurrentUsers(USER_COUNT).during(TEST_DURATION)
                )
        ).protocols(httpProtocol);
    }

// 📝 Описание:
// - Модель нагрузки: Закрытая (injectClosed), поддерживает фиксированное количество одновременно активных пользователей.
// - Интенсивность: 5 пользователей одновременно активны в течение 5 секунды (constantConcurrentUsers).
// - Длительность: Тест выполняется 5 секунду (during).
// - Протокол: Все запросы используют глобальные настройки HTTP (httpProtocol).

}




    // ⚙️ Настройка нагрузки: 1 пользователь в секунду, 1 секунда
//    public static static final int USER_COUNT = 10;
//    public static static final Duration TEST_DURATION = Duration.ofSeconds(1);

    //   {
        // 🚀 Запуск сценария с настройкой HTTP-протокола
    //       setUp(
    //              scn.injectOpen(
    //                  constantUsersPerSec(USER_COUNT).during(TEST_DURATION)
    //          )
    //    ).protocols(httpProtocol);
    // }
    // Описание: Определяет профиль нагрузки для теста.
    // - constantUsersPerSec(1): Запускает 1 нового пользователя каждую секунду.
    // - during(1): Продолжительность теста — 1 секунда, что создаёт 1 пользователя.
    // - injectOpen: Модель открытой нагрузки, где пользователи добавляются независимо от завершения предыдущих.
    // - protocols(httpProtocol): Применяет глобальные настройки HTTP ко всем запросам.
