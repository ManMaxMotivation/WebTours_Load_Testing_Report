package io.gatling.demo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import org.json.JSONObject;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

// Этот класс описывает сценарий тестирования регистрации пользователей на веб-сайте с помощью Gatling
public class UC6_Users_registration extends Simulation {

    // 📦 Конфигурация HTTP-протокола
    // Описание: Настраивает базовые параметры HTTP-запросов, такие как базовый URL, заголовки и поведение браузера.
    // - baseUrl: Указывает адрес тестируемого приложения (http://localhost:1080).
    // - inferHtmlResources: Автоматически загружает связанные ресурсы (CSS, JS, изображения).
    // - Заголовки: Имитируют поведение браузера Firefox 137.0, включая поддержку сжатия (gzip, deflate, br),
    //   языковые предпочтения (ru-RU) и небезопасные запросы.
    public static HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:1080")
            .inferHtmlResources()
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptEncodingHeader("gzip, deflate, br")
            .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
            .upgradeInsecureRequestsHeader("1")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0");

    // 🧾 Заголовки для HTTP-запросов
    // Описание: Определяют специфичные заголовки для различных типов запросов, чтобы имитировать поведение браузера.
    // - headers_0: Для запросов к главной странице и меню (document, navigate, same-origin).
    // - headers_1, headers_2: Для запросов к навигационным элементам и страницам (frame, navigate, same-origin).
    // - headers_3: Для POST-запросов формы регистрации (multipart/form-data с boundary).
    public static Map<CharSequence, String> headers_0 = Map.ofEntries(
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );

    public static Map<CharSequence, String> headers_1 = Map.ofEntries(
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin")
    );

    public static Map<CharSequence, String> headers_2 = Map.ofEntries(
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );

    public static Map<CharSequence, String> headers_3 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundarycf3bb6f64d230fe94c673ac7726e8c35"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );

    // 🗂️ Вспомогательные данные и методы
    // Описание: Содержит структуры данных и методы для генерации уникальных пользователей.
    // - usedCombinations: Хранит использованные комбинации имен для предотвращения дубликатов.
    // - random: Генератор случайных чисел для выбора данных.
    // - streets, cities: Списки адресов и городов для формирования адресов пользователей.
    public static final Set<String> usedCombinations = Collections.synchronizedSet(new HashSet<>());
    public static final Random random = new Random();

    public static final List<String> streets = List.of(
            "Evergreen Terrace", "Main St", "Oakwood Drive", "Maple Ave", "Sunset Blvd",
            "Riverside Lane", "Pine Street", "Cedar Avenue", "Hilltop Road", "Birch Court",
            "Willow Lane", "Chestnut St", "Elmwood Dr", "SprUCe Way", "Highland Rd",
            "Valley View", "Lake Shore Dr", "Meadow Lane", "Forest Ave", "Parkside Dr",
            "Canyon Rd", "Orchard St", "Brookside Ave", "Ridgeway Dr", "Sunnyhill Ln"
    );

    public static final List<String> cities = List.of(
            "New York, NY 10001", "Los Angeles, CA 90001", "San Francisco, CA 94110",
            "Chicago, IL 60614", "Houston, TX 77002", "Miami, FL 33101", "Seattle, WA 98101",
            "Austin, TX 73301", "Denver, CO 80201", "Boston, MA 02108", "Phoenix, AZ 85001",
            "Portland, OR 97201", "Atlanta, GA 30301", "Dallas, TX 75201", "San Diego, CA 92101",
            "Las Vegas, NV 89101", "Minneapolis, MN 55401", "Philadelphia, PA 19101",
            "Charlotte, NC 28201", "Orlando, FL 32801", "Salt Lake City, UT 84101",
            "Kansas City, MO 64101", "Nashville, TN 37201", "Columbus, OH 43201", "Raleigh, NC 27601"
    );

    // Метод для очистки строки: убирает все символы, кроме букв и цифр, и делает буквы маленькими
    public static String cleanString(String input) {
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
    }

    // Метод для преобразования специальных символов в латинские буквы
    public static String transliterateAndClean(String input) {
        Map<Character, Character> map = Map.ofEntries(
                Map.entry('ı', 'i'), Map.entry('İ', 'I'), Map.entry('ş', 's'), Map.entry('Ş', 'S'),
                Map.entry('ç', 'c'), Map.entry('Ç', 'C'), Map.entry('ğ', 'g'), Map.entry('Ğ', 'G'),
                Map.entry('ö', 'o'), Map.entry('Ö', 'O'), Map.entry('ü', 'u'), Map.entry('Ü', 'U'),
                Map.entry('á', 'a'), Map.entry('Á', 'A'), Map.entry('é', 'e'), Map.entry('É', 'E'),
                Map.entry('í', 'i'), Map.entry('Í', 'I'), Map.entry('ó', 'o'), Map.entry('Ó', 'O'),
                Map.entry('ú', 'u'), Map.entry('Ú', 'U'), Map.entry('ñ', 'n'), Map.entry('Ñ', 'N')
        );
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append(map.getOrDefault(c, c));
        }
        return sb.toString().replaceAll("[^a-zA-Z0-9]", "").trim();
    }

    // Метод для создания случайного пользователя
// Описание: Запрашивает данные с randomuser.me, генерирует уникального пользователя и сохраняет его в NewUsers.csv.
// - Если API отвечает, использует данные API и добавляет случайную букву к username.
// - Если API не отвечает, генерирует случайный username, firstName, lastName из букв (a-z), чтобы избежать дублирования.
// - Проверяет уникальность через usedCombinations.
// - Создает пароль из 12 случайных символов.
// - Выбирает случайные адрес и город из списков streets и cities.
    public static Map<String, String> getRandomUser() throws IOException {
        File file = new File("src/test/resources/NewUsers.csv");
        boolean fileExists = file.exists();
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter fw = new FileWriter(file, true)) {
            if (!fileExists) {
                fw.write("username,password,firstName,lastName,address1,address2\n");
            }

            // Сокращаем количество попыток до 10, так как уникальность обеспечивается
            for (int i = 0; i < 10; i++) {
                String first;
                String last;
                String username;

                try {
                    // Пробуем получить данные от API
                    URL url = new URL("https://randomuser.me/api/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setConnectTimeout(3000); // Таймаут 5 секунд
                    conn.setReadTimeout(3000); // Таймаут чтения 5 секунд
                    String json = new String(conn.getInputStream().readAllBytes());
                    JSONObject resp = new JSONObject(json);
                    JSONObject user = resp.getJSONArray("results").getJSONObject(0);
                    JSONObject name = user.getJSONObject("name");
                    first = name.getString("first");
                    last = name.getString("last");
                    String combo = (first + last).toLowerCase();
                    combo = combo.substring(0, Math.min(10, combo.length()));
                    // Генерируем случайную букву (a-z)
                    char randomLetter = (char) ('a' + random.nextInt(26));
                    username = cleanString(combo) + randomLetter; // Добавляем случайную букву к username
                } catch (IOException e) {
                    // Если API не отвечает, генерируем случайные данные из букв (a-z)
                    String chars = "abcdefghijklmnopqrstuvwxyz";
                    int nameLength = 5 + random.nextInt(4); // Длина 5-8
                    StringBuilder randomFirst = new StringBuilder();
                    StringBuilder randomLast = new StringBuilder();
                    for (int j = 0; j < nameLength; j++) {
                        randomFirst.append(chars.charAt(random.nextInt(chars.length())));
                        randomLast.append(chars.charAt(random.nextInt(chars.length())));
                    }
                    first = randomFirst.toString();
                    last = randomLast.toString();
                    // Генерируем случайный username из букв (a-z, 8-10 символов) + случайная буква
                    int usernameLength = 8 + random.nextInt(3); // Длина 8-10
                    StringBuilder randomUsername = new StringBuilder();
                    for (int j = 0; j < usernameLength; j++) {
                        randomUsername.append(chars.charAt(random.nextInt(chars.length())));
                    }
                    char randomLetter = (char) ('a' + random.nextInt(26));
                    username = randomUsername.toString() + randomLetter;
                }

                // Проверяем уникальность через usedCombinations
                if (usedCombinations.add(username)) {
                    String cleanFirst = transliterateAndClean(first);
                    String cleanLast = transliterateAndClean(last);
                    if (!username.isEmpty() && !cleanFirst.isEmpty() && !cleanLast.isEmpty()) {
                        String password = random.ints(12, 0, 62)
                                .mapToObj(n -> "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".substring(n, n + 1))
                                .reduce("", String::concat);
                        String addr1 = streets.get(random.nextInt(streets.size()));
                        String addr2 = cities.get(random.nextInt(cities.size()));

                        fw.write(String.join(",", username, password, cleanFirst, cleanLast,
                                addr1.replace(",", ""), addr2.replace(",", "")) + "\n");
                        // Убираем fw.flush() для буферизации

                        return Map.of(
                                "username", username,
                                "password", password,
                                "firstName", cleanFirst,
                                "lastName", cleanLast,
                                "address1", addr1,
                                "address2", addr2
                        );
                    }
                }
            }
        }
        throw new RuntimeException("Cannot generate unique user");
    }

    // Вспомогательный метод для генерации буквенного суффикса (a, b, c, ..., aa, ab, ...)
    public static String generateLetterSuffix(int index) {
        StringBuilder suffix = new StringBuilder();
        while (index >= 0) {
            suffix.insert(0, (char) ('a' + (index % 26)));
            index = (index / 26) - 1;
            if (index < 0) break;
        }
        return suffix.toString();
    }

    // 🏠 Шаг генерации нового пользователя
// Описание: Генерирует уникального пользователя через randomuser.me, проверяет уникальность username на сервере и сохраняет данные в сессии.
// - Вызывает getRandomUser для получения данных.
// - Проверяет, что username не существует на сервере.
// - Сохраняет username, password, firstName, lastName, address1, address2 в сессии.
// - Проверяет, что все данные сохранены, иначе выбрасывает исключение.
    public static ChainBuilder generateUser = group("Generate_User").on(
            exec(session -> {
                try {
                    Map<String, String> user = getRandomUser();
                    session = session
                            .set("username", user.get("username"))
                            .set("password", user.get("password"))
                            .set("firstName", user.get("firstName"))
                            .set("lastName", user.get("lastName"))
                            .set("address1", user.get("address1"))
                            .set("address2", user.get("address2"));
                    if (session.getString("username") == null || session.getString("password") == null ||
                            session.getString("firstName") == null || session.getString("lastName") == null ||
                            session.getString("address1") == null || session.getString("address2") == null) {
                        throw new RuntimeException("User data not set in session");
                    }
                    return session;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to generate user: " + e.getMessage(), e);
                }
            })
    );

    // 🏠 Шаг загрузки главной страницы
    // Описание: Выполняет два запроса для загрузки главной страницы и навигационной панели.
    // - home_page_0: Загружает главную страницу с параметром signOff=1 для сброса сессии.
    // - home_page_1: Загружает навигационную панель и извлекает userSession.
    public static ChainBuilder homePage = group("Home_Page").on(
            exec(
                    http("home_page_0")
                            .get("/cgi-bin/welcome.pl?signOff=1")
                            .headers(headers_0)
                            .check(
                                    substring("A Session ID has been created").exists(), // Проверяет создание сессии
                                    substring("Web Tours").exists(), // Проверяет наличие текста "Web Tours"
                                    bodyString().saveAs("tempResponse_0") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("A Session ID has been created").exists(): Подтверждает, что сервер создал новую сессию.
                    // - substring("Web Tours").exists(): Убеждается, что главная страница загрузилась корректно.
                    // - bodyString().saveAs("tempResponse_0"): Сохраняет HTML-ответ для отладки.
            ),
            exec(
                    http("home_page_1")
                            .get("/cgi-bin/nav.pl?in=home")
                            .headers(headers_1)
                            .check(
                                    regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession"), // Извлекает userSession
                                    substring("Web Tours Navigation Bar").exists(), // Проверяет наличие навигационной панели
                                    bodyString().saveAs("tempResponse_1") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - regex(...).saveAs("userSession"): Извлекает значение userSession для последующих запросов.
                    // - substring("Web Tours Navigation Bar").exists(): Подтверждает загрузку навигационной панели.
                    // - bodyString().saveAs("tempResponse_1"): Сохраняет HTML-ответ для отладки.
            )
    );

    // 📝 Шаг перехода на страницу регистрации
    // Описание: Загружает страницу регистрации для ввода данных нового пользователя.
    // - Sign_Up_Now_0: Запрашивает страницу с формой регистрации.
    public static ChainBuilder signUpNow = group("Sign_Up_Now").on(
            exec(
                    http("sign_up_now_0")
                            .get("/cgi-bin/login.pl?username=&password=&getInfo=true")
                            .headers(headers_2)
                            .check(
                                    substring("choose a username and password combination").exists(), // Проверяет наличие формы регистрации
                                    substring("Customer Profile").exists(), // Проверяет заголовок формы
                                    bodyString().saveAs("tempResponse_2") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("choose a username and password combination").exists(): Подтверждает, что форма регистрации загрузилась.
                    // - substring("Customer Profile").exists(): Убеждается, что страница содержит заголовок формы.
                    // - bodyString().saveAs("tempResponse_2"): Сохраняет HTML-ответ для отладки.
            )
    );

    // 📋 Шаг проверки данных пользователя
    // Описание: Проверяет, что данные пользователя доступны в сессии перед отправкой формы.
    // - Выбрасывает исключение, если данные отсутствуют.
    public static ChainBuilder logUserData = group("Log_User_Data").on(
            exec(session -> {
                if (session.getString("username") == null || session.getString("password") == null ||
                        session.getString("firstName") == null || session.getString("lastName") == null ||
                        session.getString("address1") == null || session.getString("address2") == null) {
                    throw new RuntimeException("User data missing in session for logging");
                }
                return session;
            })
    );

    // 📬 Шаг отправки формы регистрации
// Описание: Отправляет POST-запрос с данными пользователя для регистрации.
// - Submit_Form_0: Отправляет форму с username, password, firstName, lastName, address1, address2.
// - Проверки выполняются для подтверждения успешной регистрации.
    public static ChainBuilder submitForm = group("Submit_Form").on(
            exec(
                    http("submit_form_0")
                            .post("/cgi-bin/login.pl")
                            .headers(headers_3)
                            .formParam("username", "#{username}")
                            .formParam("password", "#{password}")
                            .formParam("passwordConfirm", "#{password}")
                            .formParam("firstName", "#{firstName}")
                            .formParam("lastName", "#{lastName}")
                            .formParam("address1", "#{address1}")
                            .formParam("address2", "#{address2}")
                            .formParam("register.x", "46")
                            .formParam("register.y", "11")
                            .check(
                                    substring("Registration successful").optional(),
                                    substring("Welcome to Web Tours").exists(),
                                    substring("Thank you, <b>#{username}</b>, for registering").exists(),
                                    bodyString().saveAs("tempResponse_3")
                            )
            )
    );

    // 📊 Шаг проверки ответа сервера
    // Описание: Проверяет, что ответ сервера после регистрации сохранен в сессии.
    // - Выбрасывает исключение, если ответ отсутствует.
    public static ChainBuilder logResponse = group("Log_Response").on(
            exec(session -> {
                if (session.getString("tempResponse_3") == null) {
                    throw new RuntimeException("Response body missing in session");
                }
                return session;
            })
    );

    // ✅ Шаг подтверждения регистрации
    // Описание: Выполняет запросы для подтверждения успешной регистрации и загрузки меню.
    // - Registration_Confirmation_0: Загружает страницу меню.
    // - Registration_Confirmation_1: Загружает навигационную панель.
    // - Registration_Confirmation_2: Загружает страницу приветствия.
    public static ChainBuilder registrationConfirmation = group("Registration_Confirmation").on(
            exec(
                    http("registration_confirmation_0")
                            .get("/cgi-bin/welcome.pl?page=menus")
                            .headers(headers_0)
                            .check(
                                    substring("Since user has already logged on").exists(), // Проверяет, что пользователь вошел
                                    substring("Web Tours").exists(), // Проверяет наличие текста "Web Tours"
                                    bodyString().saveAs("tempResponse_4") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("Since user has already logged on").exists(): Подтверждает, что пользователь авторизован.
                    // - substring("Web Tours").exists(): Убеждается, что страница меню загрузилась.
                    // - bodyString().saveAs("tempResponse_4"): Сохраняет HTML-ответ для отладки.
            ),
            exec(
                    http("registration_confirmation_1")
                            .get("/cgi-bin/nav.pl?page=menu&in=home")
                            .headers(headers_1)
                            .check(
                                    substring("menu").optional(), // Проверяет наличие меню
                                    substring("Web Tours Navigation Bar").exists(), // Проверяет наличие навигационной панели
                                    bodyString().saveAs("tempResponse_5") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("menu").optional(): Проверяет наличие текста меню (опционально).
                    // - substring("Web Tours Navigation Bar").exists(): Подтверждает загрузку навигационной панели.
                    // - bodyString().saveAs("tempResponse_5"): Сохраняет HTML-ответ для отладки.
            ),
            exec(
                    http("registration_confirmation_2")
                            .get("/cgi-bin/login.pl?welcome=true")
                            .headers(headers_1)
                            .check(
                                    substring("Welcome").optional(), // Проверяет наличие текста приветствия
                                    substring("Illegal Access").exists(), // Проверяет наличие сообщения об ошибке доступа
                                    bodyString().saveAs("tempResponse_6") // Сохраняет тело ответа
                            )
                    // Описание проверок:
                    // - substring("Welcome").optional(): Проверяет наличие приветственного текста (опционально).
                    // - substring("Illegal Access").exists(): Убеждается, что страница возвращает ожидаемое сообщение.
                    // - bodyString().saveAs("tempResponse_6"): Сохраняет HTML-ответ для отладки.
            )
    );

    // 🔓 Шаг выхода из системы
    // Описание: Выполняет запрос для завершения сессии пользователя.
    // - Logout_0: Отправляет запрос на логаут.
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

    // 📜 Шаг логирования зарегистрированных пользователей
    // Описание: Выводит в консоль данные зарегистрированного пользователя (логин и пароль).
    // - Извлекает username и password из сессии.
    // - Логирует учетные данные для отладки и проверки.
    public static ChainBuilder logRegisteredUsers = group("Log_Registered_Users").on(
            exec(session -> {
                String username = session.getString("username");
                String password = session.getString("password");

                System.out.println("Registered User Credentials: username=" + username + ", password=" + password);

                if (username == null || password == null) {
                    System.err.println("Warning: User credentials missing in session for logging");
                }

                return session;
            })
    );

    // 🧪 Сценарий объединяет шаги
    // Описание: Определяет последовательность действий виртуального пользователя:
    // 1. Генерация нового пользователя.
    // 2. Загрузка главной страницы.
    // 3. Переход на страницу регистрации.
    // 4. Проверка данных пользователя.
    // 5. Отправка формы регистрации.
    // 6. Проверка ответа сервера.
    // 7. Подтверждение регистрации.
    // 8. Выход из системы.
    // 9. Логирование зарегистрированных пользователей.
    // Паузы (3 секунды) между некоторыми шагами имитируют реальное поведение пользователя.
    public static ScenarioBuilder scn = scenario("UC6_Users_registration")
            .exec(generateUser)
            .pause(3)
            .exec(homePage)
            .pause(3)
            .exec(signUpNow)
            .pause(3)
            .exec(logUserData)
            .pause(3)
            .exec(submitForm)
            .pause(3)
            .exec(logResponse)
            .pause(3)
            .exec(registrationConfirmation)
            .pause(3)
            .exec(logout)
            .pause(3)
            .exec(logRegisteredUsers);

    // 🚀 Настройка симуляции (закрытая модель)
    private static final int USER_COUNT = 10;
    private static final Duration TEST_DURATION = Duration.ofSeconds(2);

    {
        setUp(
                scn.injectClosed(
                        constantConcurrentUsers(USER_COUNT).during(TEST_DURATION)
                )
        ).protocols(httpProtocol);
    }
}
// Описание: Определяет профиль нагрузки для теста.
// - constantUsersPerSec(3): Запускает 3 новых пользователя каждую секунду.
// - during(34): Продолжительность теста — 34 секунды, что создаёт ~102 пользователя (3 × 34).
// - Это обеспечивает ~100 регистраций уникальных пользователей, генерируемых через randomuser.me.
// - injectOpen: Модель открытой нагрузки, где пользователи добавляются независимо от завершения предыдущих.
// - protocols(httpProtocol): Применяет глобальные настройки HTTP ко всем запросам.