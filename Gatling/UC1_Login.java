package io.gatling.demo;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
import java.util.*;

public class UC1_Login extends Simulation {

    // 🌐 HTTP-протокол
    public static HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:1080")
            .inferHtmlResources()
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptEncodingHeader("gzip, deflate, br")
            .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
            .upgradeInsecureRequestsHeader("1")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0");

    public static final Map<CharSequence, String> headers_0 = Map.ofEntries(
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );

    public static final Map<CharSequence, String> headers_1 = Map.ofEntries(
            Map.entry("Priority", "u=4"),
            Map.entry("Sec-Fetch-Dest", "frame"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin")
    );

    public static final Map<CharSequence, String> headers_2 = Map.ofEntries(
            Map.entry("Origin", "http://localhost:1080"),
            Map.entry("Priority", "u=0, i"),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            Map.entry("Sec-Fetch-Site", "same-origin"),
            Map.entry("Sec-Fetch-User", "?1")
    );

    public static final FeederBuilder<String> usersFeeder = csv("users.csv").circular();

    public static ChainBuilder homePage = group("Home_Page").on(
            exec(
                    http("home_page_0")
                            .get("/cgi-bin/welcome.pl?signOff=1")
                            .headers(headers_0)
                            .check(
                                    substring("Web Tours").exists(),
                                    bodyString().saveAs("tempResponse_0")
                            )
            ),
            exec(
                    http("home_page_1")
                            .get("/cgi-bin/nav.pl?in=home")
                            .headers(headers_1)
                            .check(
                                    regex("name=\"userSession\" value=\"(.+?)\"").saveAs("userSession"),
                                    substring("Web Tours Navigation Bar").exists(),
                                    bodyString().saveAs("tempResponse_1")
                            )
            )
    );

    public static ChainBuilder loginStep = group("Login").on(
            feed(usersFeeder),
            exec(
                    http("login_0")
                            .post("/cgi-bin/login.pl")
                            .headers(headers_2)
                            .formParam("userSession", "#{userSession}")
                            .formParam("username", "#{username}")
                            .formParam("password", "#{password}")
                            .formParam("login.x", "0")
                            .formParam("login.y", "0")
                            .formParam("JSFormSubmit", "off")
                            .check(
                                    substring("User password was correct").exists(),
                                    bodyString().saveAs("tempResponse_login_0")
                            )
            ),
            exec(
                    http("login_1")
                            .get("/cgi-bin/nav.pl?page=menu&in=home")
                            .headers(headers_1)
                            .check(
                                    substring("Web Tours Navigation Bar").exists(),
                                    bodyString().saveAs("tempResponse_login_1")
                            )
            ),
            exec(
                    http("login_2")
                            .get("/cgi-bin/login.pl?intro=true")
                            .headers(headers_1)
                            .check(
                                    substring("Welcome").exists(),
                                    bodyString().saveAs("tempResponse_login_2")
                            )
            )
    );

    public static ChainBuilder logout = group("Logout").on(
            exec(
                    http("logout1")
                            .get("/cgi-bin/welcome.pl?signOff=1")
                            .headers(headers_0)
                            .check(
                                    substring("Web Tours").exists(),
                                    bodyString().saveAs("tempResponse_logout1")
                            )
            ),
            exec(
                    http("logout2")
                            .get("/cgi-bin/nav.pl?in=home")
                            .headers(headers_1)
                            .check(
                                    substring("Web Tours Navigation Bar").exists(),
                                    regex("<input type=\"hidden\" name=\"userSession\" value=\"(.+?)\"").exists(),
                                    bodyString().saveAs("tempResponse_logout2")
                            )
            )
    );

    public static ScenarioBuilder scn = scenario("UC1_Login")
            .exec(homePage)
            .pause(3)
            .exec(loginStep)
            .pause(3)
            .exec(logout);

// 🚀 Настройка симуляции
private static final int USER_COUNT = 1;
private static final Duration TEST_DURATION = Duration.ofSeconds(1);

    {
setUp(
        scn.injectOpen(
                constantUsersPerSec(USER_COUNT).during(TEST_DURATION)
                )
                        ).protocols(httpProtocol);
    }
            }