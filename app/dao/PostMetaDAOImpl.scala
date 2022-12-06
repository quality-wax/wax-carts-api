package dao

import javax.inject.Inject
import models.core.blog.PostMeta
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import service.PostMetaService
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import scala.concurrent.{ExecutionContext, Future}

class PostMetaDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends PostMetaService {

//  def post_meta_data = reactiveMongoApi.database.map(_.collection[JSONCollection]("post-meta-data"))
//
//  override def save(meta: PostMeta): Future[Boolean] = {
//    post_meta_data.flatMap(_.insert(meta)).flatMap{ result =>
//      if (result.ok)
//        Future.successful(true)
//      else
//        Future.successful(false)
//    }
//  }
//
//  override def get(postId: String): Future[Option[PostMeta]] = post_meta_data.flatMap(_.find(Json.obj("postId" -> postId)).one[PostMeta])
//
//  override def update(meta: PostMeta): Future[Boolean] = {
//    val query = Json.obj("postId" -> meta.postId)
//    val modifier = Json.obj("$set" -> Json.toJson(meta))
//    post_meta_data.flatMap(_.update(query, modifier)).flatMap{ result =>
//      if (result.ok)
//        Future.successful(true)
//      else
//        Future.successful(false)
//    }
//  }
//
//  def delete(id: String): Future[Boolean] = {
//    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
//    post_meta_data.flatMap(_.remove(query)).flatMap{ result =>
//      if (result.ok)
//        Future.successful(true)
//      else
//        Future.successful(false)
//    }
//  }
//
//  override def getAll(): Future[List[PostMeta]] = post_meta_data.flatMap(_.find(Json.obj()).cursor[PostMeta]().collect[List]())
//
//  override def getAllForStatus(status: String): Future[List[PostMeta]] = post_meta_data.flatMap(
//    _.find(Json.obj("status" -> status)).cursor[PostMeta]().collect[List]())
}
