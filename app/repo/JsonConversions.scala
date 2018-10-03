package repo

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import reactivemongo.bson.BSONDateTime
import reactivemongo.play.json.BSONFormats.BSONDateTimeFormat

object JsonConversions {

	implicit val dateTimeWrites = new Writes[OffsetDateTime] {
		def writes(time: OffsetDateTime): JsValue = Json.toJson(BSONDateTime(time.withNano(0).toEpochSecond * 1000))
	}

	implicit val dateTimeReads: Reads[OffsetDateTime] =
		(JsPath \ "$date").read[Long] map (millis => OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.ofHours(0)))

}
