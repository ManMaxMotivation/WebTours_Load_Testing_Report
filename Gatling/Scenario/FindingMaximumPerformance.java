package io.gatling.demo;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.util.List;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class FindingMaximumPerformance extends Simulation {

  // HTTP-протокол
  HttpProtocolBuilder httpProtocol = http
          .baseUrl("http://localhost:1080")
          .inferHtmlResources();

    // Сценарии
    public static ScenarioBuilder UC1Login = scenario("UC1_Login")
            .forever().on(
                    pace(Duration.ofSeconds(130))
                            .exec(UC1_Login.homePage)
                            .pause(50)
                            .exec(UC1_Login.loginStep)
                            .pause(80)
//          .exec(UC1_Login.logout);
            );
    public static ScenarioBuilder UC2Search = scenario("UC2_Search")
            .forever().on(
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
            .forever().on(
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
//          .exec(UC3_Buy_tickets.logFlightSelection); 42 сумма пауз
            );
    public static ScenarioBuilder UC4ViewTickets = scenario("UC4_View_tickets")
            .forever().on(
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
            .forever().on(
                    pace(Duration.ofSeconds(50))
                            .exec(UC5_Delete_tickets.homePage)
                            .pause(15)
                            .exec(UC5_Delete_tickets.login)
                            .pause(5)
                            .exec(UC5_Delete_tickets.itinerary)
                            .pause(15)
                            .exec(UC5_Delete_tickets.removal_itinerary)
                            .pause(14)
//          .exec(UC5_Delete_tickets.logout)
//          .pause(3)
//          .exec(UC5_Delete_tickets.logItinerary);
            );
    public static ScenarioBuilder UC6UsersRegistration = scenario("UC6_Users_registration")
            .forever().on(
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
//          .exec(UC6_Users_registration.logout) сумма пауз 22
//          .pause(3)
//          .exec(UC6_Users_registration.logRegisteredUsers);
            );
  // Блок настройки симуляции: определяет, как будут выполняться сценарии.
// Каждый сценарий имеет свои параметры
// За 60 секунд разгоняемся с 0 до 120 пользователей (распределение по сценариям сохраняется).
//22 минуты держим 120 пользователей.
//За 60 секунд увеличиваем до 130 пользователей.
//Ещё 22 минут держим 130 пользователей.
  {
      setUp(
              UC1Login.injectClosed(
                      rampConcurrentUsers(0).to(12).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(12).during(Duration.ofSeconds(1320)),
                      rampConcurrentUsers(12).to(13).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(13).during(Duration.ofSeconds(1320))
              ),
              UC2Search.injectClosed(
                      rampConcurrentUsers(0).to(24).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(24).during(Duration.ofSeconds(1320)),
                      rampConcurrentUsers(24).to(26).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(26).during(Duration.ofSeconds(1320))
              ),
              UC3BuyTickets.injectClosed(
                      rampConcurrentUsers(0).to(24).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(24).during(Duration.ofSeconds(1320)),
                      rampConcurrentUsers(24).to(26).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(26).during(Duration.ofSeconds(1320))
              ),
              UC4ViewTickets.injectClosed(
                      rampConcurrentUsers(0).to(36).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(36).during(Duration.ofSeconds(1320)),
                      rampConcurrentUsers(36).to(39).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(39).during(Duration.ofSeconds(1320))
              ),
              UC5DeleteTickets.injectClosed(
                      rampConcurrentUsers(0).to(12).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(12).during(Duration.ofSeconds(1320)),
                      rampConcurrentUsers(12).to(13).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(13).during(Duration.ofSeconds(1320))
              ),
              UC6UsersRegistration.injectClosed(
                      rampConcurrentUsers(0).to(12).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(12).during(Duration.ofSeconds(1320)),
                      rampConcurrentUsers(12).to(13).during(Duration.ofSeconds(60)),
                      constantConcurrentUsers(13).during(Duration.ofSeconds(1320))
              )
      )
              .protocols(httpProtocol)
              .maxDuration(Duration.ofSeconds(2760)); // <<< вот здесь

  }
      @Override
    public void after() {
        java.util.logging.Logger.getLogger("Scenarios").info("All scenarios completed, shutdown time: 5 seconds");
    }
}