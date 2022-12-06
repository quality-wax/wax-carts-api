package controllers

import com.mohiva.play.silhouette.api.Silhouette
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import javax.inject.Inject
import models.core.blog.{Comment, Post, PostsPage}
import models.swagger.{ObjectList, ResponseMessage}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{CommentService, PostMetaService, PostService, UserService}
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Blog endpoints")
class BlogController @Inject()(components: ControllerComponents,
                               userService: UserService,
                               postService: PostService,
                               postMetaService: PostMetaService,
                               commentService: CommentService,
                               configuration: Configuration,
                               silhouette: Silhouette[DefaultEnv],
                               messagesApi: MessagesApi)
                              (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  @ApiOperation(value = "Creates a new post", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      ),
      new ApiImplicitParam(
        value = "Post data",
        required = true,
        dataType = "models.core.blog.Post",
        paramType = "body"
      )
    )
  )
  def savePost() = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Post].map { data =>
      val meta = data.meta.copy(created = DateTime.now(DateTimeZone.UTC).toString(), lastModified = DateTime.now(DateTimeZone.UTC).toString())
      for {
        postSaved <- postService.save(data.copy(meta = meta))
      } yield {
        if (postSaved)
          Ok(Json.toJson("message" -> "Post saved successfully"))
        //TODO Add more checks to evaluate which object wasn't saved and take proper actions.
        else BadRequest(Json.toJson(Bad(code = Some(500), message = "Failed to save Post")))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Delete post", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def deletePost(postId: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    for {
      postDeleted <- postService.delete(postId)
      commentsDeleted <- commentService.deleteAllForPost(postId)
    } yield {
      if (postDeleted && commentsDeleted)
        Ok(Json.toJson("message" -> "Post deleted successfully"))
      //TODO Add more checks to evaluate which object wasn't deleted and take proper actions.
      //TODO Clean all comments related to the post from the comment collection
      else BadRequest(Json.toJson(Bad(code = Some(500), message = "Failed to delete Post")))
    }
  }

  @ApiOperation(value = "Update post", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      ),
      new ApiImplicitParam(
        value = "Post data",
        required = true,
        dataType = "models.core.blog.Post",
        paramType = "body"
      )
    )
  )
  def updatePost(postId: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Post].map { data =>
      val meta = data.meta.copy(lastModified = DateTime.now(DateTimeZone.UTC).toString())
      for {
        postUpdated <- postService.update(data.copy(slug = postId, meta = meta))
      } yield {
        if (postUpdated)
          Ok(Json.toJson("message" -> "Post update successfully"))
        else BadRequest(Json.toJson(Bad(code = Some(500), message = "Failed to updated Post")))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Creates a new comment", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Comment data",
        required = true,
        dataType = "models.core.blog.Comment",
        paramType = "body"
      )
    )
  )
  def saveComment(id: String) = silhouette.UserAwareAction.async(parse.json) { implicit request =>
    request.body.validate[Comment].map { data =>
      for {
        isSaved <- commentService.save(data.copy(id = None, date = DateTime.now(DateTimeZone.UTC).toString(), postId = id))
      } yield {
        if (isSaved)
          Ok(Json.toJson("message" -> "Comment saved successfully"))
        else BadRequest(Json.toJson(Bad(code = Some(500), message = "Failed to save comment")))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Update comment with specified ID", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      ),
      new ApiImplicitParam(
        value = "Comment data",
        required = true,
        dataType = "models.core.blog.Comment",
        paramType = "body"
      )
    )
  )
  def updateComment(id: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Comment].map { data =>
      for {
        isUpdated <- commentService.update(data.copy(id = Some(id)))
      } yield {
        if (isUpdated)
          Ok(Json.toJson("message" -> "Comment updated successfully"))
        else BadRequest(Json.toJson(Bad(code = Some(500), message = "Failed to update comment")))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Delete comment with specified ID", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def deleteComment(id: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      isDeleted <- commentService.delete(id)
    } yield {
      if (isDeleted)
        Ok(Json.toJson("message" -> "Comment deleted successfully"))
      else BadRequest(Json.toJson(Bad(code = Some(500), message = "Failed to delete comment")))
    }
  }

  @ApiOperation(value = "Get post with specified ID", response = classOf[Post])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getPost(id: String) = silhouette.UserAwareAction.async { implicit request =>
    for {
      post <- postService.get(id)
    } yield {
      if(post.isDefined)
        Ok(Json.toJson(post))
      else NotFound(Json.toJson("message" -> "Not Found"))
    }
  }

  @ApiOperation(value = "Get posts with specified status", responseContainer="List", response = classOf[Post])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getPostsWithStatus(status: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      posts <- postService.getAllWithStatus(status)
    } yield {
      Ok(Json.toJson(posts))
    }
  }

  @ApiOperation(value = "Get published posts", responseContainer="List", response = classOf[Post])
  def getPublishedPosts() = silhouette.UserAwareAction.async { implicit request =>
    for {
      posts <- postService.getAllWithStatus("published")
    } yield {
      Ok(Json.toJson(posts))
    }
  }

  @ApiOperation(value = "Get paginated published post", responseContainer="List", response = classOf[PostsPage])
  def getPaginatedPublishedPosts(pageNumber: Int) = silhouette.UserAwareAction.async { implicit request =>
    //Make the value sent unsigned
    val pageN = ((pageNumber >>> 1) << 1) + (pageNumber & 1)
    for {
      page <- postService.getPaginatedWithStatus(pageN,"published")
    } yield {
      Ok(Json.toJson(page))
    }
  }

  @ApiOperation(value = "Get drafted posts", responseContainer="List", response = classOf[Post])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getDraftPosts() = silhouette.SecuredAction.async { implicit request =>
    for {
      posts <- postService.getAllWithStatus("draft")
    } yield {
      Ok(Json.toJson(posts))
    }
  }

  @ApiOperation(value = "Get comment with specified ID", response = classOf[Comment])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getComment(id: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      comment <- commentService.get(id)
    } yield {
      if(comment.isDefined)
        Ok(Json.toJson(comment))
      else NotFound(Json.toJson("message" -> "Not Found"))
    }
  }

  @ApiOperation(value = "Get list of published comments for post with specified ID", responseContainer="List", response = classOf[Comment])
  def getPublishedPostComments(id: String) = silhouette.UserAwareAction.async { implicit request =>
    for {
      comments <- commentService.getForPost(id, "published")
    } yield {
      Ok(Json.toJson(comments))
    }
  }

  @ApiOperation(value = "Get list of unpublished comments for post with specified ID", responseContainer="List", response = classOf[Comment])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getDraftPostComments(id: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      comments <- commentService.getForPost(id, "draft")
    } yield {
      Ok(Json.toJson(comments))
    }
  }

  @ApiOperation(value = "Get post comments with specified status", responseContainer="List", response = classOf[Comment])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "X-Auth-Token",
        value = "User access token",
        required = true,
        dataType = "string",
        paramType = "header"
      )
    )
  )
  def getPostCommentsWithStatus(id: String, status: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      comments <- commentService.getForPost(id, status)
    } yield {
      Ok(Json.toJson(comments))
    }
  }
}
