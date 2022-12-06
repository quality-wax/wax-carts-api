package models.core.blog
import play.api.libs.json
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class Comment (
                   id: Option[String],
                   postId: String,
                   date: String,
                   authorName: String,
                   authorEmail: String,
                   content: String,
                   status: String
                   )

object Comment {
  implicit object CommentWrites extends OWrites[Comment] {
    def writes(comment: Comment): JsObject =
      comment.id match {
        case Some(id) => Json.obj(
          "_id" -> comment.id,
          "postId" -> comment.postId,
          "date" -> comment.date,
          "authorName" -> comment.authorName,
          "authorEmail" -> comment.authorEmail,
          "content" -> comment.content,
          "status" -> comment.status
        )

        case _ =>
          Json.obj(
            "postId" -> comment.postId,
            "date" -> comment.date,
            "authorName" -> comment.authorName,
            "authorEmail" -> comment.authorEmail,
            "content" -> comment.content,
            "status" -> comment.status
          )
      }
  }

  implicit object CommentReads extends Reads[Comment] {
    def reads(json: JsValue): JsResult[Comment] = json match {
      case comment: JsObject =>
        Try {
          val id = (comment \ "_id" \ "$oid").asOpt[String]

          val postId = (comment \ "postId").as[String]
          val date = (comment \ "date").as[String]
          val authorName = (comment \ "authorName").as[String]
          val authorEmail = (comment \ "authorEmail").as[String]
          val content = (comment \ "content").as[String]
          val status = (comment \ "status").as[String]

          JsSuccess(
            new Comment(
              id,
              postId,
              date,
              authorName,
              authorEmail,
              content,
              status
            )
          )
        } match {
          case Success(value) => value
          case Failure(cause) => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

}