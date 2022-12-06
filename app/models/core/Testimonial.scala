package models.core


import play.api.libs.json
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class Testimonial(
                        id: Option[String],
                        date: String,
                        name: String,
                        email: String,
                        country: String,
                        stars: Int,
                        content: String,
                        status: String
                      )

object Testimonial {
  implicit object TestimonialWrites extends OWrites[Testimonial] {
    def writes(testi: Testimonial): JsObject =
      testi.id match {
        case Some(id) =>
          Json.obj(
            "_id" -> testi.id,
            "date" -> testi.date,
            "name" -> testi.name,
            "email" -> testi.email,
            "country" -> testi.country,
            "stars" -> testi.stars,
            "content" -> testi.content,
            "status" -> testi.status
          )
        case _ =>
          Json.obj(
            "date" -> testi.date,
            "name" -> testi.name,
            "email" -> testi.email,
            "country" -> testi.country,
            "stars" -> testi.stars,
            "content" -> testi.content,
            "status" -> testi.status
          )
      }
  }

  implicit object TestimonialReads extends Reads[Testimonial] {
    def reads(json: JsValue): JsResult[Testimonial] = json match {
      case testi: JsObject =>
        Try {
          val id = (testi \ "_id" \ "$oid").asOpt[String]

          val date = (testi \ "date").as[String]
          val name = (testi \ "name").as[String]
          val email = (testi \ "email").as[String]
          val country = (testi \ "country").as[String]
          val stars = (testi \ "stars").as[Int]
          val content = (testi \ "content").as[String]
          val status = (testi \ "status").as[String]

          JsSuccess(
            new Testimonial(
              id,
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
