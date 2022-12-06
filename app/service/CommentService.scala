package service

import models.core.blog.Comment

import scala.concurrent.Future

trait CommentService {
  def save(comment: Comment): Future[Boolean]

  def get(id: String): Future[Option[Comment]]

  def update(comment: Comment): Future[Boolean]

  def getForPost(postId: String, status: String): Future[List[Comment]]

  def getAllForPost(postId: String): Future[List[Comment]]

  def delete(id: String): Future[Boolean]

  def deleteAllForPost(postId: String): Future[Boolean]
}
