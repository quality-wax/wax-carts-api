package dao

import javax.inject.Inject
import models.core.Testimonial
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import service.TestimonialService
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class TestimonialDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends TestimonialService {

  def testimonials = reactiveMongoApi.database.map(_.collection[JSONCollection]("testimonials"))

  override def save(testimonial: Testimonial): Future[Boolean] = {
    testimonials.flatMap(_.insert(testimonial)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def getAll(): Future[List[Testimonial]] = {
    val query = Json.obj()
    testimonials.flatMap(_.find(query).cursor[Testimonial]().collect[List]())
  }

  override def update(testimonial: Testimonial): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> testimonial.id.get))
    val modifier = Json.obj("$set" -> Json.toJson(testimonial.copy(id = None)))
    testimonials.flatMap(_.update(query, modifier)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def delete(id: String): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    testimonials.flatMap(_.remove(query)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def getAllWithStatus(status: String): Future[List[Testimonial]] = {
    val query = Json.obj("status" -> status)
    testimonials.flatMap(_.find(query).cursor[Testimonial]().collect[List]())
  }

  override def get(id: String): Future[Option[Testimonial]] = testimonials.flatMap(_.find(Json.obj("_id" -> Json.obj("$oid" -> id))).one[Testimonial])
}
