package security.login

import controllers.UserInfo
import security.authentication.DefaultLoginInfo
import play.api.mvc.RequestHeader
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import repo.UserRepository
import com.mohiva.play.silhouette.api.Silhouette
import security.authentication.Env
import scala.concurrent.ExecutionContext
import security.authentication.User
import scala.concurrent.Future

class LoginService @Inject() (val userRepo: UserRepository, silhouette: Silhouette[Env])(implicit val ec: ExecutionContext) {

  private lazy val userService = silhouette.env.identityService
  private lazy val authenticatorService = silhouette.env.authenticatorService

  def makeTokenFor(userInfo: UserInfo)(implicit request: RequestHeader) = {
    val loginInfo = DefaultLoginInfo(userInfo.id)
    for {
      maybeUser <- silhouette.env.identityService.retrieve(loginInfo)
      user <- maybeUser.fold {
        // user doesn't exist yet, make a new one
        newUser(loginInfo, userInfo.name)
      } {
        // user already exists
        Future.successful
      }
      authenticator <- authenticatorService.create(user.loginInfo)
      token <- authenticatorService.init(authenticator)
    } yield token
  }

  private def newUser(loginInfo: LoginInfo, name: String) = {
    for {
      userCreationResult <- userRepo.addUser(User(loginInfo, name))
        .map(result => result.fold(fault => throw new RuntimeException("User creation failed."), unit => ()))
      userOption <- userService.retrieve(loginInfo)
    } yield userOption.get
  }

}