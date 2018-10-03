package security.authentication

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json._
import play.api.libs.json._
import reactivemongo.api.commands.WriteResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactivemongo.api.ReadPreference.nearest
import repo.UserRepository
import repo.UserFault
import repo.OtherUserFault
import repo.UserConflict
import scala.concurrent.ExecutionContext

object UserServiceJson {

  implicit val userJsoner = Json.format[User]
  implicit val loginInfoJsoner = Json.format[LoginInfo]

}

@Singleton
class UserService @Inject() (val userRepo: UserRepository)(implicit val ec: ExecutionContext)
  extends IdentityService[User] {

  private[this] final val logger: Logger = LoggerFactory.getLogger(classOf[UserService])

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userRepo.retrieve(loginInfo)

  def create(user: User): Future[Either[UserFault, Unit]] = userRepo.addUser(user)

  def removeUser(loginInfo: LoginInfo): Future[Either[UserFault, Unit]] = userRepo.removeUser(loginInfo)

}
