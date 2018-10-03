package controllers

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.InjectedController
import repo.RecipesRepo
import com.mohiva.play.silhouette.api.Silhouette
import security.authentication.Env

case class Oid($oid: String)
object Oid {
  implicit val oidFormat = Json.format[Oid]
}

case class Recipe(oid: Option[Oid], cuisine: String, id: Long, ingredients: Seq[String])
object Recipe {
  implicit val recipeFormat = Json.format[Recipe]
}

@Singleton
class RecipesController @Inject() (val repo: RecipesRepo, silhouette: Silhouette[Env])(implicit val ec: ExecutionContext)
  extends InjectedController {

  def getRecipes = silhouette.UserAwareAction.async {

    request =>

      request.identity.fold(unauthorized) {
        user =>
          this.repo.getRecipes() map {
            _ fold (
              _ => InternalServerError,
              recipes => Ok(Json.toJson(recipes)))
          }
      }
  }

  def postRecipe = silhouette.UserAwareAction.async(parse.json) {

    request =>

      request.identity.fold(unauthorized) {

        user =>

          Json.fromJson[Recipe](request.body) fold (

            _ => Future.successful(BadRequest),

            this.repo.saveRecipe(_) map {
              _ fold (
                InternalServerError(_),
                Created(_))
            })

      }
  }

  private val unauthorized = Future.successful(Unauthorized(""))
}
