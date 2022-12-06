package dao

import javax.inject.Inject
import models.Pagination
import models.core.blog.{Post, PostsPage}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import models.core.product.{Product, ProductsPage}
import reactivemongo.api.{QueryOpts, ReadPreference}
import service.ProductService
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class ProductDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends ProductService {

  def products = reactiveMongoApi.database.map(_.collection[JSONCollection]("products"))

  override def save(product: Product): Future[Product] = products.flatMap(_.insert(product)).flatMap(_ => Future.successful(product))

  override def get(productSlug: String): Future[Option[Product]] = products.flatMap(_.find(Json.obj("slug" -> productSlug)).one[Product])

  override def update(product: Product): Future[Boolean] = {
    val query = Json.obj("slug" -> product.slug)
    val modifier = Json.obj("$set" -> Json.toJson(product))
    products.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
  }

  override def delete(productSlug: String): Future[Boolean] = {
    val query = Json.obj("slug" -> productSlug)
    products.flatMap(_.remove(query)).flatMap(_ => Future.successful(true))
  }

  override def getAll(): Future[List[Product]] = {
    val query = Json.obj()
    products.flatMap(_.find(query).cursor[Product]().collect[List]())
  }

  override def getPaginated(pageNumber: Int): Future[ProductsPage] = {
    val query = Json.obj()
    val skipN = (pageNumber-1) * 12
    val queryOptions = QueryOpts(skipN = skipN, batchSizeN = 12, flagsN = 0)
    for {
      ps <- products.flatMap(_.find(query).options(queryOptions).cursor[Product] (ReadPreference.primaryPreferred).collect[List] (12))
      total <- products.flatMap(_.count(Some(Json.obj())))
    } yield {
      val totalPages: Double = (total/12) + 1
      ProductsPage(ps, Pagination(pageNumber, totalPages.toLong, 12))
    }
  }
}
