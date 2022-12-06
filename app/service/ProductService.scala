package service
import models.core.product.{Product, ProductsPage}

import scala.concurrent.Future

trait ProductService {
  def save(product: Product): Future[Product]

  def get(productSlug: String): Future[Option[Product]]

  def update(product: Product): Future[Boolean]

  def delete(productSlug: String): Future[Boolean]

  def getAll(): Future[List[Product]]

  def getPaginated(pageNumber: Int): Future[ProductsPage]
}
