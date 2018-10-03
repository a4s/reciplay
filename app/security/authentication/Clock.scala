package security.authentication

import org.joda.time.DateTime
import javax.inject.Singleton

@Singleton
class Clock extends com.mohiva.play.silhouette.api.util.Clock {

	def now = DateTime.now
}
