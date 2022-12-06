package service

import models.core.blog.{Post, PostsPage}

import scala.concurrent.Future

trait PostService {
  def save(post: Post): Future[Boolean]

  def get(postSlug: String): Future[Option[Post]]

  def getAllWithStatus(status: String): Future[List[Post]]

  def getPaginatedWithStatus(pageNumber: Int, status: String): Future[PostsPage]

  def update(post: Post): Future[Boolean]

  def delete(postSlug: String): Future[Boolean]
}
