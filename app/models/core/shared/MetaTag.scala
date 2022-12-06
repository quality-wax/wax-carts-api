package models.core.shared

import play.api.libs.json
import play.api.libs.json.Json

case class MetaTag (
                     name: String,
                     content: String
                   )

object MetaTag {
  implicit val metaTagFormat: json.Format[MetaTag] = Json.format[MetaTag]
}
