package security.authentication

import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import scala.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import play.api.libs.json._
import com.mohiva.play.silhouette.api.util.JsonFormats._
import reactivemongo.api.ReadPreference.nearest
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json.BSONFormats.BSONDateTimeFormat
import reactivemongo.play.json._
import reactivemongo.api.Cursor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import scala.concurrent.ExecutionContext
import reactivemongo.bson.BSONDateTime

@Singleton
class AuthenticatorRepo @Inject() (val reactiveMongoApi: ReactiveMongoApi)(implicit val ec: ExecutionContext)
    extends com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository[BearerTokenAuthenticator] {

  private[this] final val logger: Logger = LoggerFactory.getLogger(classOf[AuthenticatorRepo])

  def collectionFuture: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("authtokens"))

  implicit val jodaDateTimeWrites = new Writes[DateTime] {
    def writes(time: DateTime): JsValue = Json.toJson(BSONDateTime(time.getMillis))
  }

  implicit val jodaDateTimeReads: Reads[DateTime] =
    (JsPath \ "$date").read[Long] map (millis => new DateTime().withMillis(millis).withZone(DateTimeZone.forOffsetHours(0)))

  private final implicit val authenticatorJsonConversion = Json.format[BearerTokenAuthenticator]

  def find(id: String): Future[Option[BearerTokenAuthenticator]] = {
    val selector = Json.obj("id" -> id)
    collectionFuture.flatMap {
      collection =>
        collection
          .find(selector)
          .cursor[BearerTokenAuthenticator](nearest)
          .collect[List](-1,
            cursorErrorHandler("Could not fetch token with ID " + id + " from DB"))
          .map {
            case List() =>
              logger.info("No authenticator found for ID " + id)
              None
            case authenticator :: List() => Some(authenticator)
            case authenticator :: otherAuthenticators =>
              logger.warn("Multiple authenticators found with ID " + id)
              Some(authenticator)
          }
    }
  }

  def add(authenticator: BearerTokenAuthenticator): Future[BearerTokenAuthenticator] = {
    collectionFuture.flatMap {
      collection =>
        collection.insert(authenticator).map {
          case writeResult if (writeResult.code.isEmpty) => authenticator
          case writeResult => // TODO what else can we do?
            logger.error("Error status adding authenticator:", writeResult)
            throw new RuntimeException("Adding authenticator to DB failed. Error message: " + messageFor(writeResult))
        }
    }
  }

  def update(authenticator: BearerTokenAuthenticator): Future[BearerTokenAuthenticator] = {
    val selector = Json.obj("id" -> authenticator.id)
    collectionFuture.flatMap {
      collection =>
        collection.update(selector, authenticator).map {
          case writeResult if (writeResult.code.isEmpty) => authenticator
          case writeResult => // TODO what else can we do?
            logger.error("Error status updating authenticator:", writeResult)
            throw new RuntimeException("Updating authenticator in DB failed. Error message" + writeResult.errmsg)

        }
    }
  }

  def remove(id: String): Future[Unit] = {
    val selector = Json.obj("id" -> id)
    collectionFuture.flatMap {
      collection =>
        collection.remove(selector).map {
          case writeResult if (writeResult.code.isEmpty) => ()
          case writeResult =>
            logger.error("Removing authenticator resulted in error.", writeResult)
            throw new RuntimeException("Removing authenticator in DB failed. Error message: " + messageFor(writeResult))
        }
    }
  }

  private def messageFor(result: WriteResult) = result.writeConcernError.map(_.errmsg.toString()).getOrElse("Unknown error (code " + result.code + ").")

  private def cursorErrorHandler(msgHeader: String) = Cursor.FailOnError {
    (l: List[BearerTokenAuthenticator], t: Throwable) =>
      {
        logger.error(msgHeader + ": cursor error. " + t)
      }
  }
}
