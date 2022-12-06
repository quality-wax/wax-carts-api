package models.core.blog

import models.core.shared.Seo
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class PostMeta (
                    created: String,
                    published: String,
                    lastModified: String,
                    featuredImage: String,
                    )
 object PostMeta {
   implicit val postMetaFormat: json.Format[PostMeta] = Json.format[PostMeta]
   implicit val postMetaOFormat: OFormat[PostMeta] = Json.format[PostMeta]
 }