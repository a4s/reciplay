package repo

import scala.concurrent.Future
import org.slf4j.{ LoggerFactory, Logger }
import javax.inject.Inject
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.play.json._
import reactivemongo.bson.BSONDateTime
import reactivemongo.api.Cursor
import java.time.Instant
import reactivemongo.api.ReadPreference.nearest
import reactivemongo.api.commands.WriteResult
import java.time.OffsetDateTime
import reactivemongo.api.commands.UpdateWriteResult
import javax.inject.Singleton
import reactivemongo.bson.BSONDateTime
import com.mohiva.play.silhouette.api.LoginInfo
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.api.collections.GenericCollection
import reactivemongo.play.json.JSONSerializationPack
import reactivemongo.play.json.collection.JSONCollectionProducer
import scala.concurrent.ExecutionContext
import reactivemongo.api.commands.GetLastError
import play.modules.reactivemongo.ReactiveMongoApi
import security.authentication.User

trait UserFault
class UserConflict extends UserFault
class OtherUserFault extends UserFault

object UserRepository {
  import JsonConversions._
  implicit val userFormat = Json.format[User]
}

@Singleton
class UserRepository @Inject() (val dbApi: ReactiveMongoApi)(implicit val ec: ExecutionContext) {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[UserRepository])

  private def userCollection = dbApi.database.map(_.collection[GenericCollection[JSONSerializationPack.type]]("users"))

  import JsonConversions._
  import UserRepository.userFormat

  private case class DishKey(val location: String, startTime: OffsetDateTime, name: String)
  private implicit val dishKeyFormat = Json.format[DishKey]

  /**
   * Persists a user with the start date truncated to seconds.
   */
  def addUser(user: User): Future[Either[UserFault, Unit]] = {
    userCollection.flatMap {
      col =>
        col.insert(user)
          .map {
            case writeResult: WriteResult if (writeResult.code.isEmpty) =>
              logger.debug("Normal result")
              Right(())
            case other =>
              logger.error("Unexpected result inserting new user:" + other)
              Left(new OtherUserFault)
          }
          .recover(recoverCreate)
    }
  }

  def removeUser(loginInfo: LoginInfo): Future[Either[UserFault, Unit]] = {
    userCollection.flatMap {
      col =>
        col.remove(Json.obj("loginInfo" -> loginInfo))

          .map {
            itsAll => Right(())
          }
          .recover {
            case writeResult: WriteResult =>
              logger.error("Error removing user: " + writeResult)
              Left(new OtherUserFault)
            case other =>
              logger.error("Some completely insane error removing user: " + other)
              Left(new OtherUserFault)
          }
    }
  }

  def listAllUsers()(implicit user: User): Future[Option[List[User]]] = {
    userCollection.flatMap {
      col =>
        col.find(Json.obj())
          .cursor[User](nearest).collect[List](
            -1,
            cursorErrorHandler("Could not fetch users from DB"))
          .map {
            list => Some(list)
          }.recover {
            case throwable =>
              logger.error("Error fetching users from DB: " + throwable)
              None
          }
    }
  }

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val selector = Json.obj("loginInfo" -> loginInfo)
    userCollection.flatMap {
      col =>
        col.find(selector)
          .cursor[User](nearest)
          .collect[List](
            -1,
            cursorErrorHandler("Could not fetch users from DB"))
          .map {
            case Nil         => None
            case user :: Nil => Some(user)
            case user :: otherUsers =>
              logger.warn("Multiple users found with login info " + loginInfo)
              Some(user)
          }
    }
  }

  private def recoverCreate[U >: Left[UserFault, Unit]]: PartialFunction[Throwable, U] = {
    case writeResult: WriteResult if (writeResult.code.isDefined && writeResult.code.get == 11000) => Left(new UserConflict)
    case throwable =>
      logger.error("Error adding user to DB: " + throwable)
      Left(new OtherUserFault)
  }

  private def cursorErrorHandler(msgHeader: String) = Cursor.FailOnError {
    (l: List[User], t: Throwable) =>
      {
        logger.error(msgHeader + ": cursor error. " + t)
      }
  }

}

