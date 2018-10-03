package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import akka.stream.Materializer
import play.api.mvc.Headers
import play.api.libs.json.Json

class RecipeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "RecipesController GET" should {

    "list recipes from the application" in {
      val controller = inject[RecipesController]
      val result = controller.getRecipes().apply(FakeRequest(GET, "/recipes"))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result).validate[Seq[Recipe]].asOpt mustBe defined
    }

    "list recipes from the router" in {
      val request = FakeRequest(GET, "/recipes")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result).validate[Seq[Recipe]].asOpt mustBe defined
    }

    "post a recipe from the application" in {
      val controller = inject[RecipesController]

      val result = controller.postRecipe()(FakeRequest(
        POST,
        "/recipes",
        Headers(("Content-Type", "application/json")),
        Json.toJson(Recipe(None, "testcuisine", 0L, Seq("salt", "pepper")))))

      val stat = status(result)
      Console.out.println("POST status: " + stat)
      status(result) mustBe CREATED
      contentType(result) mustBe Some("text/plain")
      contentAsString(result) must not be empty
    }

    "post a recipe from the router" in {
      val json = Json.toJson(Recipe(None, "testcuisine", 0L, Seq("salt", "pepper")))
      Console.out.println("POSTing " + json)
      val request = FakeRequest(POST, "/recipes").withJsonBody(json)
      val result = route(app, request).get

      status(result) mustBe CREATED
      contentType(result) mustBe Some("text/plain")
      contentAsString(result) must not be empty
    }

  }
}
