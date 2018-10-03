package controllers

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import play.api.libs.json.Json
import play.api.mvc.InjectedController
import security.login.LoginService

case class UserInfo(val name: String, val id: String)

@Singleton
class LoginController @Inject() (loginService: LoginService)(implicit val ec: ExecutionContext)
  extends InjectedController {

  private implicit val userInfoFormat = Json.format[UserInfo]

  def login = Action.async(parse.json) {
    implicit request =>

      Json.fromJson[UserInfo](request.body) fold (

        _ => Future.successful(BadRequest),

        loginService.makeTokenFor(_) map {
          Created(_)
        })

  }

}
