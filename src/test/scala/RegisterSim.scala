import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random



class UserRegistrationSimulation extends Simulation {

  // HTTP 프로토콜 설정
  val httpProtocol = http
    .baseUrl("http://localhost:8080") // 테스트할 서버의 기본 URL
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // 동적으로 이메일을 생성할 Feeder
  val emailFeeder = Iterator.continually(Map(
    "email" -> s"user${Random.nextInt(100000)}@example.com" // 동적 이메일 생성
  ))

  val nameFeeder = Iterator.continually(Map("username" -> s"testuser${Random.nextInt(10000)}"))

  // 시나리오 설정
  val scn = scenario("User Registration")
    .feed(emailFeeder) // 이메일 동적 생성
    .feed(nameFeeder)  // 유저명 동적 생성
    .exec(http("Register User")
      .post("/auth/register")
      .body(StringBody("""
        {
          "Name": "${username}",
          "Email": "${email}",  // 동적으로 생성된 이메일 값 사용
          "Pw": "password123",
          "Phone": "1234567890"
        }
      """)).asJson
      .check(status.is(200))) // 상태 코드가 200이면 성공

  // 시나리오 실행 설정 (1000명의 동시 사용자를 시뮬레이션)
  setUp(
    scn.inject(atOnceUsers(1000)) // 1000명의 동시 사용자가 동시에 회원가입을 시도
  ).protocols(httpProtocol)
}