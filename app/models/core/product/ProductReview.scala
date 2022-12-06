package models.core.product

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class ProductReview(
                          id: Option[String],
                          productId: String,
                          date: String,
                          name: String,
                          email: String,
                          country: String,
                          stars: Int,
                          content: String,
                          status: String,
                        )

object ProductReview {
  implicit object ProductReviewWrites extends OWrites[ProductReview] {
    def writes(review: ProductReview): JsObject =
      review.id match {
        case Some(id) =>
          Json.obj(
            "_id" -> review.id,
            "productId" -> review.productId,
            "date" -> review.date,
            "name" -> review.name,
            "email" -> review.email,
            "country" -> review.country,
            "stars" -> review.stars,
            "content" -> review.content,
            "status" -> review.status
          )
        case _ =>
          Json.obj(
            "productId" -> review.productId,
            "date" -> review.date,
            "name" -> review.name,
            "email" -> review.email,
            "country" -> review.country,
            "stars" -> review.stars,
            "content" -> review.content,
            "status" -> review.status
          )
      }
  }

  implicit object ProductReviewReads extends Reads[ProductReview] {
    def reads(json: JsValue): JsResult[ProductReview] = json match {
      case review: JsObject =>
        Try {
          val id = (review \ "_id" \ "$oid").asOpt[String]
          val productId = (review \ "productId").as[String]
          val date = (review \ "date").as[String]
          val name = (review \ "name").as[String]
          val email = (review \ "email").as[String]
          val country = (review \ "country").as[String]
          val stars = (review \ "stars").as[Int]
          val content = (review \ "content").as[String]
          val status = (review \ "status").as[String]

          JsSuccess(
            new ProductReview(
              id,
              productId,
              date,
              name,
              email,
              country,
              stars,
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