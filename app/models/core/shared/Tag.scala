package models.core.shared

import play.api.libs.json
import play.api.libs.json.Json

case class Tag (
               slug: String,
               name: String,
               )

object Tag {
  implicit val tagFormat: json.Format[Tag] = Json.format[Tag]
}