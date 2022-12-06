package models.core.shared

import play.api.libs.json
import play.api.libs.json.Json

case class PropertyTag (
                         property: String,
                         content: String
                       )
object PropertyTag {
  implicit val propertyTagFormat: json.Format[PropertyTag] = Json.format[PropertyTag]
}