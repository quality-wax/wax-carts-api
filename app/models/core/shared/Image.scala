package models.core.shared

import play.api.libs.json
import play.api.libs.json.Json

case class Image (
                 url: String,
                 alt: String
                 )

object Image {
  implicit val imageFormat: json.Format[Image] = Json.format[Image]
}
