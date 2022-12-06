package models.core.shared

import play.api.libs.json
import play.api.libs.json.Json

case class Seo (
               title: String,
               description: String,
               metaTags: List[MetaTag],
               propertyTags: List[PropertyTag]
               )

object Seo {
  implicit val seoFormat: json.Format[Seo] = Json.format[Seo]
}