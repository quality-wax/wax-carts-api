package models.core.blog

import play.api.libs.json
import play.api.libs.json.Json

case class PostAuthor (
                      name: String,
                      email: String,
                      bio: String,
                      facebookUrl: String,
                      instagramUrl: String,
                      twitterUrl: String,
                      avatar: String
                      )

object PostAuthor {
  implicit val authorFormat: json.Format[PostAuthor] = Json.format[PostAuthor]
}
