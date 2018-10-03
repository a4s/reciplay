package security.authentication

import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticatorSettings
import javax.inject.Singleton
import scala.concurrent.duration.DurationInt
import com.mohiva.play.silhouette.api.util.RequestPart

@Singleton
class AuthenticatorSettings extends BearerTokenAuthenticatorSettings(
  authenticatorExpiry = 7.days,
  requestParts = Some(Seq(RequestPart.QueryString, RequestPart.Headers)))
