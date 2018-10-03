package security.authentication

import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import javax.inject.Inject

@Singleton
class IDGenerator @Inject() (implicit val ec: ExecutionContext) //
  extends SecureRandomIDGenerator(idSizeInBytes = 32)
