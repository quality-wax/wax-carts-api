package dao

import javax.inject.Inject
import models.Pagination
import models.core.blog.{Post, PostsPage}
import models.core.product.Product
import play.modules.reactivemongo.ReactiveMongoApi
import service.PostService
import play.api.libs.json._
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class PostDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends PostService {

  def posts = reactiveMongoApi.database.map(_.collection[JSONCollection]("posts"))

  override def save(post: Post): Future[Boolean] = {
    posts.flatMap(_.insert(post)).flatMap{ result =>
        if (result.ok)
          Future.successful(true)
        else
          Future.successful(false)
    }
  }

  override def get(postSlug: String): Future[Option[Post]] = posts.flatMap(_.find(Json.obj("slug" -> postSlug)).one[Post])

  override def update(post: Post): Future[Boolean] = {
    val query = Json.obj("slug" -> post.slug)
    val modifier = Json.obj("$set" -> Json.toJson(post))
    posts.flatMap(_.update(query, modifier)).flatMap{ result =>
        if (result.ok)
          Future.successful(true)
        else
          Future.successful(false)
    }
  }

  override def delete(postSlug: String): Future[Boolean] ={
    val query = Json.obj("slug" -> postSlug)
    posts.flatMap(_.remove(query)).flatMap{ result =>
        if (result.ok)
          Future.successful(true)
        else
          Future.successful(false)
    }
  }

  override def getAllWithStatus(status: String): Future[List[Post]] = posts.flatMap(
    _.find(Json.obj("status" -> status)).cursor[Post]().collect[List]())

  override def getPaginatedWithStatus(pageNumber: Int, status: String): Future[PostsPage] = {
    val query = Json.obj("status" -> status)
    val skipN = (pageNumber-1) * 12
    val queryOptions = QueryOpts(skipN = skipN, batchSizeN = 12, flagsN = 0)
    for {
      ps <- posts.flatMap(_.find(query).options(queryOptions).cursor[Post] (ReadPreference.primaryPreferred).collect[List] (12))
      total <- posts.flatMap(_.count(Some(Json.obj())))
    } yield {
      val totalPages: Double = (total/12) + 1
      PostsPage(ps, Pagination(pageNumber, totalPages.toLong, 12))
    }
  }
}
