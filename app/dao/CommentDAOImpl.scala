package dao

import javax.inject.Inject
import models.core.blog.Comment
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import service.CommentService

import scala.concurrent.{ExecutionContext, Future}

class CommentDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends CommentService {

  def comments = reactiveMongoApi.database.map(_.collection[JSONCollection]("comments"))

  override def save(comment: Comment): Future[Boolean] = {
    comments.flatMap(_.insert(comment)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def get(id: String): Future[Option[Comment]] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    comments.flatMap(_.find(query).one[Comment])
  }

  override def update(comment: Comment): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> comment.id.get))
    val modifier = Json.obj("$set" -> Json.toJson(comment.copy(id = None)))
    comments.flatMap(_.update(query, modifier)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def delete(id: String): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    comments.flatMap(_.remove(query)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }

  override def getForPost(postId: String, status: String): Future[List[Comment]] = {
    val query = Json.obj("postId" -> postId, "status" -> status)
    comments.flatMap(_.find(query).cursor[Comment]().collect[List]())
  }

  override def getAllForPost(postId: String): Future[List[Comment]] = {
    val query = Json.obj("postId" -> postId)
    comments.flatMap(_.find(query).cursor[Comment]().collect[List]())
  }

  override def deleteAllForPost(postId: String): Future[Boolean] = {
    val query = Json.obj("postId" -> postId)
    comments.flatMap(_.remove(query)).flatMap{ result =>
      if (result.ok)
        Future.successful(true)
      else
        Future.successful(false)
    }
  }
}
