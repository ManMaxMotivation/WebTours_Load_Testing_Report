package io.gatling.demo;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Arrays;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class UC2_Search extends Simulation {

    // 🗂️ Фидер
    public static final FeederBuilder<String> usersFeeder = csv("users.csv").circular();

    // 🌐 HTTP-протокол
    public static HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:1080")
            .inferHtmlResources()
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptEncodingHeader("gzip, deflate, br")
            .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
            .upgradeInsecureRequestsHeader("1")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0");

    // 📋 Заголовки
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
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );
    public static Map<CharSequence, String> headers_8 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary33c32868ece214ee22af976902a2b211"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );
    public static Map<CharSequence, String> headers_9 = Map.ofEntries(
            Map.entry("Content-Type", "multipart/form-data; boundary=----geckoformboundary1d0b7362baab283a3d5c76f9fd1b6a8a"),
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );

    // 🏠 Шаги
    public static ChainBuilder homePage = group("Home_Page").on(
            exec(
                    http("home_page_0")
                            .get("/cgi-bin/welcome.pl?signOff=1")
                            .headers(headers_0)
                            .check(substring("A Session ID has been created"))
                            .check(substring("Web Tours"))
                            .check(bodyString().saveAs("tempResponse_0"))
            )
                    .exec(
                            http("home_page_1")
                                    .get("/cgi-bin/nav.pl?in=home")
                                    .headers(headers_1)
                                    .check(regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession"))
                                    .check(substring("Web Tours Navigation Bar"))
                                    .check(bodyString().saveAs("tempResponse_1"))
                    )
    );

    public static ChainBuilder loginStep = group("Login").on(
            exec(
                    feed(usersFeeder),
                    http("login_0")
                            .post("/cgi-bin/login.pl")
                            .headers(headers_2)
                            .formParam("userSession", "#{userSession}")
                            .formParam("username", "#{username}")
                            .formParam("password", "#{password}")
                            .formParam("login.x", "0")
                            .formParam("login.y", "0")
                            .formParam("JSFormSubmit", "off")
                            .check(substring("User password was correct"))
                            .check(bodyString().saveAs("tempResponse_login_0"))
            )
                    .exec(
                            http("login_1")
                                    .get("/cgi-bin/nav.pl?page=menu&in=home")
                                    .headers(headers_1)
                                    .check(substring("Web Tours Navigation Bar"))
                                    .check(bodyString().saveAs("tempResponse_login_1"))
                    )
                    .exec(
                            http("login_2")
                                    .get("/cgi-bin/login.pl?intro=true")
                                    .headers(headers_1)
                                    .check(substring("Welcome"))
                                    .check(bodyString().saveAs("tempResponse_login_2"))
                    )
    );

    public static ChainBuilder flights = group("Flights").on(
            exec(
                    http("flights_0")
                            .get("/cgi-bin/welcome.pl?page=search")
                            .headers(headers_0)
                            .resources(
                                    http("flights_1")
                                            .get("/cgi-bin/nav.pl?page=menu&in=flights")
                                            .headers(headers_1),
                                    http("flights_2")
                                            .get("/cgi-bin/reservations.pl?page=welcome")
                                            .headers(headers_1)
                                            .check(
                                                    regex("<option value=\"([^\"]+)\">").findAll().saveAs("cityList"),
                                                    substring("Find Flight").exists()
                                            )
                            )
            )
                    .exec(session -> {
                        String userSession = session.getString("userSession");
                        if (userSession == null) {
                            System.err.println("userSession is not defined or not found");
                            userSession = "defaultSession_" + System.nanoTime();
                        }
                        Random random = new Random(userSession.hashCode());
                        List<String> cities = session.get("cityList");
                        if (cities == null || cities.size() < 2) {
                            System.out.println("Not enough cities, using fallback list");
                            cities = Arrays.asList("Denver", "London", "Paris", "New York", "Tokyo", "Los Angeles", "Frankfurt", "Sydney", "Portland", "Seattle");
                        }
     //                   System.out.println("Collected cities: " + cities);
                        int departIndex = Math.abs(userSession.hashCode()) % cities.size();
                        String departCity = cities.get(departIndex);
                        int arrivalIndex = (Math.abs(userSession.hashCode() + 1)) % cities.size();
                        if (arrivalIndex == departIndex && cities.size() > 1) {
                            arrivalIndex = (arrivalIndex + 1) % cities.size();
                        }
                        String arrivalCity = cities.get(arrivalIndex);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, random.nextInt(10) + 1);
                        String departDate = dateFormat.format(calendar.getTime());
                        calendar.add(Calendar.DAY_OF_YEAR, random.nextInt(5) + 1);
                        String returnDate = dateFormat.format(calendar.getTime());
                        List<String> seatPreferences = Arrays.asList("Aisle", "Window", "None");
                        List<String> seatTypes = Arrays.asList("First", "Business", "Coach");
                        String seatPref = seatPreferences.get(random.nextInt(seatPreferences.size()));
                        String seatType = seatTypes.get(random.nextInt(seatTypes.size()));
                        int numPassengers = random.nextInt(3) + 1;
      //                  System.out.println("Generated numPassengers: " + numPassengers);
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

    public static ChainBuilder findFlight = group("Find_Flight").on(
            exec(
                    http("find_flight_0")
                            .post("/cgi-bin/reservations.pl")
                            .headers(headers_8)
                            .formParam("advanceDiscount", "0")
                            .formParam("depart", "#{departCity}")
                            .formParam("departDate", "#{departDate}")
                            .formParam("arrive", "#{arrivalCity}")
                            .formParam("returnDate", "#{returnDate}")
                            .formParam("numPassengers", "#{numPassengers}")
                            .formParam("seatPref", "#{seatPref}")
                            .formParam("seatType", "#{seatType}")
                            .formParam("findFlights.x", "56")
                            .formParam("findFlights.y", "12")
                            .formParam(".cgifields", "roundtrip")
                            .formParam(".cgifields", "seatPref")
                            .formParam(".cgifields", "seatType")
                            .check(
                                    regex("name=\"outboundFlight\" value=\"([^\"]+)\">").findAll().saveAs("outboundFlight"),
                                    substring("outboundFlight").exists()
                            )
            )
                    .exec(session -> {
                        List<String> flights = session.get("outboundFlight");
                        String randomFlight;
                        if (flights != null && !flights.isEmpty()) {
                            Random random = new Random();
                            randomFlight = flights.get(random.nextInt(flights.size()));
 //                           System.out.println("Selected randomFlight: " + randomFlight);
                        } else {
                            randomFlight = "Not Found";
                            System.err.println("No outbound flights found");
                        }
                        return session.set("randomFlight", randomFlight);
                    })
    );

    public static ChainBuilder flightSelection = group("Flight_Selection").on(
            exec(
                    http("flight_selection_0")
                            .post("/cgi-bin/reservations.pl")
                            .headers(headers_9)
                            .formParam("outboundFlight", "#{randomFlight}")
                            .formParam("numPassengers", "#{numPassengers}")
                            .formParam("advanceDiscount", "0")
                            .formParam("seatType", "#{seatType}")
                            .formParam("seatPref", "#{seatPref}")
                            .formParam("reserveFlights.x", "55")
                            .formParam("reserveFlights.y", "6")
                            .check(
                                    substring("Flight Reservation").exists(),
                                    substring("Payment Details").exists(),
                                    regex("name=\"outboundFlight\" value=\"([^\"]+)\"").saveAs("returnedFlight"),
                                    bodyString().saveAs("tempResponse_flightSelection")
                            )
            )
                    .exec(session -> {
                        String departCity = session.getString("departCity");
                        String arrivalCity = session.getString("arrivalCity");
                        String username = session.getString("username");
                        String password = session.getString("password");
                        String selectedFlight = session.getString("randomFlight");
                        String returnedFlight = session.getString("returnedFlight");
                        String response = session.getString("tempResponse_flightSelection");
                        boolean flightMatch = selectedFlight != null && selectedFlight.equals(returnedFlight);
//                        System.out.println("Flight Route: " + departCity + " to " + arrivalCity);
//                        System.out.println("User Credentials: username=" + username + ", password=" + password);
//                        System.out.println("Selected Flight: " + selectedFlight);
//                        System.out.println("Returned Flight from Server: " + returnedFlight);
//                        System.out.println("Flight Match: " + (flightMatch ? "SUCCESS" : "FAILURE"));
//                        System.out.println("Flight Selection Response: " + response);
                        return session;
                    })
    );

    public static ChainBuilder logout = group("Logout").on(
            exec(
                    http("logout_0")
                            .get("/cgi-bin/welcome.pl?signOff=1")
                            .headers(headers_0)
                            .check(substring("Session ID has been created").exists())
                            .check(bodyString().saveAs("tempResponse_logout1"))
            ),
            exec(
                    http("logout_1")
                            .get("/cgi-bin/nav.pl?in=home")
                            .headers(headers_1)
                            .check(substring("Web Tours Navigation Bar").exists())
                            .check(regex("<input type=\"hidden\" name=\"userSession\" value=\"(.+?)\"").exists())
                            .check(bodyString().saveAs("tempResponse_logout2"))
            )
    );

    // 📋 Сценарий
    public static ScenarioBuilder scn = scenario("UC2_Search")
            .exec(homePage)
            .pause(3)
            .exec(loginStep)
            .pause(3)
            .exec(flights)
            .pause(3)
            .exec(findFlight)
            .pause(3)
            .exec(flightSelection)
            .pause(3)
            .exec(logout);

    // 🚀 Настройка симуляции (закрытая модель)
    private static final int USER_COUNT = 1;
    private static final Duration TEST_DURATION = Duration.ofSeconds(1);

    {
        setUp(
                scn.injectClosed(
                        constantConcurrentUsers(USER_COUNT).during(TEST_DURATION)
                )
        ).protocols(httpProtocol);
    }
}