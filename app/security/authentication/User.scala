package security.authentication

import com.mohiva.play.silhouette.api.Identity
import com.mohiva.play.silhouette.api.LoginInfo

object User {

}

case class User(
  loginInfo:     LoginInfo,
  name:          String,
) extends Identity

object DefaultLoginInfo  {
  
  def apply(id: String): LoginInfo = LoginInfo("default-provider", id)
  
}