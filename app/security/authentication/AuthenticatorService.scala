package security.authentication

import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticatorService
import javax.inject.Singleton
import javax.inject.Inject
import scala.concurrent.ExecutionContext

@Singleton
class AuthenticatorService @Inject() (
  settings: AuthenticatorSettings,
  dao: AuthenticatorRepo,
  idGenerator: IDGenerator,
  clock: Clock) //
  (implicit val ec: ExecutionContext)
    extends BearerTokenAuthenticatorService(settings, dao, idGenerator, clock)
