package models.core.blog

import models.core.shared.{Category, Seo, Tag}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class Post (
                slug: String,
                title: String,
                author: PostAuthor,
                meta: PostMeta,
                body: String,
                category: Category,
                tag: Tag,
                seo: Seo,
                status: String
                )

object Post {
  implicit val postFormat: json.Format[Post] = Json.format[Post]
  implicit val postOFormat: OFormat[Post] = Json.format[Post]
}