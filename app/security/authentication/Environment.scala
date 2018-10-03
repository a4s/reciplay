package security.authentication

import com.mohiva.play.silhouette.api.EventBus
import com.mohiva.play.silhouette.api.RequestProvider
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext

trait Env extends com.mohiva.play.silhouette.api.Env {
  type I = User
  type A = BearerTokenAuthenticator
}

class Environment @Inject() (val identityService: IdentityService[User],
                             val authenticatorService: com.mohiva.play.silhouette.api.services.AuthenticatorService[BearerTokenAuthenticator]) //
                             (implicit val ec: ExecutionContext)
    extends com.mohiva.play.silhouette.api.Environment[Env] {

  def requestProviders: Seq[RequestProvider] = Nil
  val eventBus: EventBus = new EventBus
  val executionContext = ec
}
