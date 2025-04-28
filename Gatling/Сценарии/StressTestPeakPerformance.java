package io.gatling.demo;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class StressTestPeakPerformance extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:1080")
            .inferHtmlResources();

    // Сценарии без .forever()
    public static ScenarioBuilder UC1Login = scenario("UC1_Login")
            .exec(
                    pace(Duration.ofSeconds(130))
                            .exec(UC1_Login.homePage)
                            .pause(50)
                            .exec(UC1_Login.loginStep)
                            .pause(80)
            );

    public static ScenarioBuilder UC2Search = scenario("UC2_Search")
            .exec(
                    pace(Duration.ofSeconds(70))
                            .exec(UC2_Search.homePage)
                            .pause(12)
                            .exec(UC2_Search.loginStep)
                            .pause(6)
                            .exec(UC2_Search.flights)
                            .pause(11)
                            .exec(UC2_Search.findFlight)
                            .pause(14)
                            .exec(UC2_Search.flightSelection)
                            .pause(14)
                            .exec(UC2_Search.logout)
                            .pause(13)
            );

    public static ScenarioBuilder UC3BuyTickets = scenario("UC3_Buy_ticket")
            .exec(
                    pace(Duration.ofSeconds(42))
                            .exec(UC3_Buy_tickets.homePage)
                            .pause(6)
                            .exec(UC3_Buy_tickets.login)
                            .pause(6)
                            .exec(UC3_Buy_tickets.flights)
                            .pause(6)
                            .exec(UC3_Buy_tickets.findFlight)
                            .pause(6)
                            .exec(UC3_Buy_tickets.flightSelection)
                            .pause(3)
                            .exec(UC3_Buy_tickets.payment)
                            .pause(4)
                            .exec(UC3_Buy_tickets.itinerary)
                            .pause(5)
                            .exec(UC3_Buy_tickets.logout)
                            .pause(5)
            );

    public static ScenarioBuilder UC4ViewTickets = scenario("UC4_View_tickets")
            .exec(
                    pace(Duration.ofSeconds(230))
                            .exec(UC4_View_tickets.homePage)
                            .pause(40)
                            .exec(UC4_View_tickets.login)
                            .pause(50)
                            .exec(UC4_View_tickets.itinerary)
                            .pause(50)
                            .exec(UC2_Search.flights)
                            .pause(50)
                            .exec(UC4_View_tickets.logout)
                            .pause(40)
            );

    public static ScenarioBuilder UC5DeleteTickets = scenario("UC5_Delete_tickets")
            .exec(
                    pace(Duration.ofSeconds(50))
                            .exec(UC5_Delete_tickets.homePage)
                            .pause(15)
                            .exec(UC5_Delete_tickets.login)
                            .pause(5)
                            .exec(UC5_Delete_tickets.itinerary)
                            .pause(15)
                            .exec(UC5_Delete_tickets.removal_itinerary)
                            .pause(14)
            );

    public static ScenarioBuilder UC6UsersRegistration = scenario("UC6_Users_registration")
            .exec(
                    pace(Duration.ofSeconds(36))
                            .exec(UC6_Users_registration.generateUser)
                            .pause(1)
                            .exec(UC6_Users_registration.homePage)
                            .pause(4)
                            .exec(UC6_Users_registration.signUpNow)
                            .pause(3)
                            .exec(UC6_Users_registration.logUserData)
                            .pause(3)
                            .exec(UC6_Users_registration.submitForm)
                            .pause(3)
                            .exec(UC6_Users_registration.logResponse)
                            .pause(3)
                            .exec(UC6_Users_registration.registrationConfirmation)
                            .pause(3)
            );

    {
        setUp(
                UC1Login.injectClosed(
                        rampConcurrentUsers(0).to(29).during(Duration.ofSeconds(180)), // 3 мин
                        constantConcurrentUsers(29).during(Duration.ofMinutes(7)), // 7 мин
                        rampConcurrentUsers(29).to(20).during(Duration.ofSeconds(10)), // -25%
                        constantConcurrentUsers(20).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(20).to(15).during(Duration.ofSeconds(10)), // -25%
                        constantConcurrentUsers(15).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(15).to(11).during(Duration.ofSeconds(10)), // -25%
                        constantConcurrentUsers(11).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(11).to(8).during(Duration.ofSeconds(10)), // -25%
                        constantConcurrentUsers(8).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(8).to(0).during(Duration.ofSeconds(10)) // до 0
                ),
                UC2Search.injectClosed(
                        rampConcurrentUsers(0).to(58).during(Duration.ofSeconds(180)),
                        constantConcurrentUsers(58).during(Duration.ofMinutes(7)),
                        rampConcurrentUsers(58).to(40).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(40).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(40).to(30).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(30).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(30).to(23).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(23).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(23).to(17).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(17).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(17).to(0).during(Duration.ofSeconds(10))
                ),
                UC3BuyTickets.injectClosed(
                        rampConcurrentUsers(0).to(58).during(Duration.ofSeconds(180)),
                        constantConcurrentUsers(58).during(Duration.ofMinutes(7)),
                        rampConcurrentUsers(58).to(40).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(40).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(40).to(30).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(30).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(30).to(23).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(23).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(23).to(17).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(17).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(17).to(0).during(Duration.ofSeconds(10))
                ),
                UC4ViewTickets.injectClosed(
                        rampConcurrentUsers(0).to(87).during(Duration.ofSeconds(180)),
                        constantConcurrentUsers(87).during(Duration.ofMinutes(7)),
                        rampConcurrentUsers(87).to(61).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(61).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(61).to(46).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(46).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(46).to(35).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(35).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(35).to(26).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(26).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(26).to(0).during(Duration.ofSeconds(10))
                ),
                UC5DeleteTickets.injectClosed(
                        rampConcurrentUsers(0).to(29).during(Duration.ofSeconds(180)),
                        constantConcurrentUsers(29).during(Duration.ofMinutes(7)),
                        rampConcurrentUsers(29).to(20).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(20).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(20).to(15).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(15).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(15).to(11).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(11).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(11).to(8).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(8).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(8).to(0).during(Duration.ofSeconds(10))
                ),
                UC6UsersRegistration.injectClosed(
                        rampConcurrentUsers(0).to(29).during(Duration.ofSeconds(180)),
                        constantConcurrentUsers(29).during(Duration.ofMinutes(7)),
                        rampConcurrentUsers(29).to(20).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(20).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(20).to(15).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(15).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(15).to(11).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(11).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(11).to(8).during(Duration.ofSeconds(10)),
                        constantConcurrentUsers(8).during(Duration.ofMinutes(2)),
                        rampConcurrentUsers(8).to(0).during(Duration.ofSeconds(10))
                )
        )
                .protocols(httpProtocol)
                .maxDuration(Duration.ofMinutes(20));
    }

}