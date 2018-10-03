package controllers

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.InjectedController
import repo.RecipesRepo

case class Oid($oid: String)
object Oid {
  implicit val oidFormat = Json.format[Oid]  
}

case class Recipe(oid: Option[Oid], cuisine: String, id: Long, ingredients: Seq[String])
object Recipe {
  implicit val recipeFormat = Json.format[Recipe]
}

@Singleton
class RecipesController @Inject() (val repo: RecipesRepo)(implicit val ec: ExecutionContext)
  extends InjectedController {


  def getRecipes = Action.async {

    request =>

      this.repo.getRecipes() map {
        _ fold (
          _ => InternalServerError,
          recipes => Ok(Json.toJson(recipes)))
      }
  }

  def postRecipe = Action.async(parse.json) {
    request =>

      Json.fromJson[Recipe](request.body) fold (

        _ => Future.successful(BadRequest),

        this.repo.saveRecipe(_) map {
          _ fold (
            InternalServerError(_),
            Created(_))
        })

  }

}
