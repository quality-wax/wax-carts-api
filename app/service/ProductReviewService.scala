package service

import models.core.product.ProductReview

import scala.concurrent.Future

trait ProductReviewService {
  def save(review: ProductReview): Future[Boolean]

  def get(id: String): Future[Option[ProductReview]]

  def getWithStatus(productId: String, status: String): Future[List[ProductReview]]

  def getAllForProduct(productId: String): Future[List[ProductReview]]

  def deleteAllForProduct(productId: String): Future[Boolean]

  def update(review: ProductReview): Future[Boolean]

  def delete(id: String): Future[Boolean]
}
