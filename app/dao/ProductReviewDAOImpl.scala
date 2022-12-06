package dao

import javax.inject.Inject
import models.core.product.ProductReview
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection._
import reactivemongo.play.json._
import service.ProductReviewService

import scala.concurrent.{ExecutionContext, Future}

class ProductReviewDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends ProductReviewService {

  def reviews = reactiveMongoApi.database.map(_.collection[JSONCollection]("reviews"))

  override def save(review: ProductReview): Future[Boolean] = {
    reviews.flatMap(_.insert(review)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def getWithStatus(productId: String, status: String): Future[List[ProductReview]] = {
    val query = Json.obj("productId" -> productId, "status" -> status)
    reviews.flatMap(_.find(query).cursor[ProductReview]().collect[List]())
  }

  override def getAllForProduct(productId: String): Future[List[ProductReview]] = {
    val query = Json.obj("productId" -> productId)
    reviews.flatMap(_.find(query).cursor[ProductReview]().collect[List]())
  }

  override def update(review: ProductReview): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> review.id.get))
    val modifier = Json.obj("$set" -> Json.toJson(review.copy(id = None)))
    reviews.flatMap(_.update(query, modifier)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def delete(id: String): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    reviews.flatMap(_.remove(query)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def deleteAllForProduct(productId: String): Future[Boolean] = {
    val query = Json.obj("productId" -> productId)
    reviews.flatMap(_.remove(query)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def get(id: String): Future[Option[ProductReview]] = reviews.flatMap(_.find(Json.obj("_id" -> Json.obj("$oid" -> id))).one[ProductReview])
}
