package formatters.json

import models.core.blog.{Post, PostMeta}
import play.api.libs.json
import play.api.libs.json.Json

case class PostData (
                    post: Post,
                    postMeta: PostMeta
                    )

object PostData {
  implicit val postDataFormat: json.Format[PostData] = Json.format[PostData]
}