package service

import models.core.Testimonial
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait TestimonialService {
  def save(testimonial: Testimonial): Future[Boolean]

  def get(id: String): Future[Option[Testimonial]]

  def getAll(): Future[List[Testimonial]]

  def getAllWithStatus(status: String): Future[List[Testimonial]]

   def update(testimonial: Testimonial): Future[Boolean]

   def delete(id: String): Future[Boolean]
}
