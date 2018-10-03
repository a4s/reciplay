package security.authentication

import com.google.inject.AbstractModule
import com.google.inject.TypeLiteral
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.SilhouetteProvider
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator


class Module extends AbstractModule {

  def configure() = {

    bind(new TypeLiteral[IdentityService[User]] {}).to(classOf[UserService])
    bind(new TypeLiteral[com.mohiva.play.silhouette.api.services.AuthenticatorService[BearerTokenAuthenticator]] {}).to(classOf[AuthenticatorService])
    bind(new TypeLiteral[com.mohiva.play.silhouette.api.Environment[Env]] {}).to(classOf[security.authentication.Environment])
    bind(new TypeLiteral[Silhouette[Env]] {}).to(new TypeLiteral[SilhouetteProvider[Env]] {})
  }
}
