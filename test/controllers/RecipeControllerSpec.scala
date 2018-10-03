package controllers

import java.util.UUID
import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest

import com.mohiva.play.silhouette.api.LoginInfo

import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.test.Injecting
import security.authentication.AuthenticatorRepo
import security.authentication.DefaultLoginInfo
import security.authentication.Env
import security.authentication.UserService
import security.login.LoginService

class RecipeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val timeout: FiniteDuration = FiniteDuration(10, TimeUnit.SECONDS)

  "RecipesController GET" should {

    "list recipes from the application" in {
      val controller = inject[RecipesController]
      val (loginInfo, token) = loginUser("test user")

      val result = controller.getRecipes().apply(FakeRequest(GET, "/recipes").withHeaders(("X-Auth-Token", token)))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result).validate[Seq[Recipe]].asOpt mustBe defined
      cleanup(loginInfo, token)
    }

    "list recipes from the router" in {
      val (loginInfo, token) = loginUser("test user")
      val request = FakeRequest(GET, "/recipes").withHeaders(("X-Auth-Token", token))
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result).validate[Seq[Recipe]].asOpt mustBe defined
      cleanup(loginInfo, token)
    }

    "post a recipe from the application" in {

      val controller = inject[RecipesController]
      val (loginInfo, token) = loginUser("test user")
      val result = controller.postRecipe()(FakeRequest(
        POST,
        "/recipes",
        Headers(("Content-Type", "application/json"), ("X-Auth-Token", token)),
        Json.toJson(Recipe(None, "testcuisine", 0L, Seq("salt", "pepper")))))

      val stat = status(result)
      Console.out.println("POST status: " + stat)
      status(result) mustBe CREATED
      contentType(result) mustBe Some("text/plain")
      contentAsString(result) must not be empty
      cleanup(loginInfo, token)
    }

    "post a recipe from the router" in {
      val (loginInfo, token) = loginUser("test user")
      val json = Json.toJson(Recipe(None, "testcuisine", 0L, Seq("salt", "pepper")))
      Console.out.println("POSTing " + json)
      val request = FakeRequest(POST, "/recipes").withHeaders(("X-Auth-Token", token)).withJsonBody(json)
      val result = route(app, request).get

      status(result) mustBe CREATED
      contentType(result) mustBe Some("text/plain")
      contentAsString(result) must not be empty
      cleanup(loginInfo, token)
    }

  }

  def loginUser(userName: String): (LoginInfo, Env#A#Value) = {

    val loginService = inject[LoginService]

    val loginInfo = DefaultLoginInfo(UUID.randomUUID().toString())
    val userInfo = UserInfo(userName, loginInfo.providerKey)
    implicit val request = FakeRequest()
    val token = Await.result(loginService.makeTokenFor(userInfo)(request), timeout)

    (loginInfo, token)
  }

  def cleanup(loginInfo: LoginInfo, token: Env#A#Value) = {

    val authenticatorRepo = inject[AuthenticatorRepo]
    val userService = inject[UserService]

    Await.result(authenticatorRepo.remove(token), timeout)
    Await.result(userService.removeUser(loginInfo), timeout)

  }

}
