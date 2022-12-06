package models.core.shared

import play.api.libs.json
import play.api.libs.json.Json

case class Category (
                 slug: String,
                 name: String,
               )

object Category {
  implicit val categoryFormat: json.Format[Category] = Json.format[Category]
}
