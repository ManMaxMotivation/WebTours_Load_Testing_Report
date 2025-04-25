package io.gatling.demo;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.util.List;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class Scenarios extends Simulation {

  // HTTP-протокол
  HttpProtocolBuilder httpProtocol = http
          .baseUrl("http://localhost:1080")
          .inferHtmlResources();

  // Сценарии
  public static ScenarioBuilder UC1Login = scenario("UC1_Login")
          .pace(Duration.ofSeconds(130))
          .exec(UC1_Login.homePage)
          .pause(50)
          .exec(UC1_Login.loginStep)
          .pause(80);
//          .exec(UC1_Login.logout);

  public static ScenarioBuilder UC2Search = scenario("UC2_Search")
          .pace(Duration.ofSeconds(70))
          .exec(UC2_Search.homePage)
          .pause(9)
          .exec(UC2_Search.loginStep)
          .pause(6)
          .exec(UC2_Search.flights)
          .pause(11)
          .exec(UC2_Search.findFlight)
          .pause(19)
          .exec(UC2_Search.flightSelection)
          .pause(15)
          .exec(UC2_Search.logout)
          .pause(15);

  public static ScenarioBuilder UC3BuyTickets = scenario("UC3_Buy_ticket")
          .pace(Duration.ofSeconds(42,5))
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
          .pause(6)
          .exec(UC3_Buy_tickets.logout)
          .pause(5);
//          .exec(UC3_Buy_tickets.logFlightSelection); 42 сумма пауз

  public static ScenarioBuilder UC4ViewTickets = scenario("UC4_View_tickets")
          .pace(Duration.ofSeconds(230))
          .exec(UC4_View_tickets.homePage)
          .pause(40)
          .exec(UC4_View_tickets.login)
          .pause(50)
          .exec(UC4_View_tickets.itinerary)
          .pause(50)
          .exec(UC2_Search.flights)
          .pause(50)
          .exec(UC4_View_tickets.logout)
          .pause(40);

  public static ScenarioBuilder UC5DeleteTickets = scenario("UC5_Delete_tickets")
          .pace(Duration.ofSeconds(50))
          .exec(UC5_Delete_tickets.homePage)
          .pause(15)
          .exec(UC5_Delete_tickets.login)
          .pause(5)
          .exec(UC5_Delete_tickets.itinerary)
          .pause(15)
          .exec(UC5_Delete_tickets.removal_itinerary)
          .pause(15);
//          .exec(UC5_Delete_tickets.logout)
//          .pause(3)
//          .exec(UC5_Delete_tickets.logItinerary);

  public static ScenarioBuilder UC6UsersRegistration = scenario("UC6_Users_registration")
          .exec(UC6_Users_registration.generateUser)
          .pace(Duration.ofSeconds(30))
          .exec(UC6_Users_registration.homePage)
          .pause(6)
          .exec(UC6_Users_registration.signUpNow)
          .pause(6)
          .exec(UC6_Users_registration.logUserData)
          .pause(3)
          .exec(UC6_Users_registration.submitForm)
          .pause(3)
          .exec(UC6_Users_registration.logResponse)
          .pause(2)
          .exec(UC6_Users_registration.registrationConfirmation)
          .pause(2);
//          .exec(UC6_Users_registration.logout) сумма пауз 22
//          .pause(3)
//          .exec(UC6_Users_registration.logRegisteredUsers);

  // Блок настройки симуляции: определяет, как будут выполняться сценарии.
// Каждый сценарий имеет свои параметры
// продолжительность основной ступени (1200 сек) - отладочный тест на 20 мин - 10 пользователей.
  {
      setUp(
              UC1Login.injectClosed(
                      constantConcurrentUsers(1).during(Duration.ofSeconds(1200))
              ),

              UC2Search.injectClosed(
                      constantConcurrentUsers(2).during(Duration.ofSeconds(1200))
              ),

              UC3BuyTickets.injectClosed(
                      constantConcurrentUsers(2).during(Duration.ofSeconds(1200))
              ),

              UC4ViewTickets.injectClosed(
                      constantConcurrentUsers(3).during(Duration.ofSeconds(1200))
              ),

              UC5DeleteTickets.injectClosed(
                      constantConcurrentUsers(1).during(Duration.ofSeconds(1200))
              ),

              UC6UsersRegistration.injectClosed(
                      constantConcurrentUsers(1).during(Duration.ofSeconds(1200))
              )
      ).protocols(httpProtocol);
  }

    @Override
    public void after() {
        java.util.logging.Logger.getLogger("Scenarios").info("All scenarios completed, shutdown time: 5 seconds");
    }
}