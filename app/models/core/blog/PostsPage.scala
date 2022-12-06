package models.core.blog

import models.Pagination
import play.api.libs.json
import play.api.libs.json.Json

case class PostsPage (
                     posts: List[Post],
                     pagination: Pagination
                     )

object PostsPage {
  implicit val postsPageFormat: json.Format[PostsPage] = Json.format[PostsPage]
}