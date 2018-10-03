package repo

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import controllers.Oid
import controllers.Recipe
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.ReadPreference.nearest
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

@Singleton
class RecipesRepo @Inject() (val dbApi: ReactiveMongoApi)(implicit val ec: ExecutionContext) {

  val recipeCollection = dbApi.database.map(_.collection[JSONCollection]("recipes00"))
  val maxRead = 10

  def saveRecipe(recipe: Recipe): Future[Either[String, String]] = recipeCollection flatMap {

    collection =>

      val oidString = BSONObjectID.generate().stringify
      val oid = Some(Oid(oidString))

      collection.insert(recipe.copy(oid = oid)) map {
        writeResult => Right(oidString)
      } recover {
        case throwable => Left(throwable.getMessage)
      }
  }

  def getRecipes(): Future[Either[String, Seq[Recipe]]] = recipeCollection flatMap {

    collection =>
      collection.find(Json.obj()).
        cursor[Recipe](nearest).
        collect[Seq](
          maxRead,
          Cursor.ContOnError[Seq[Recipe]]()) map {
            list => Right(list)
          } recover {
            case throwable => Left("Error reading recipes")
          }
  }

}
