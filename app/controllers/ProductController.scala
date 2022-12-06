package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import javax.inject.Inject
import models.core.product.{Product, ProductReview, ProductsPage}
import models.swagger.{ObjectList, ResponseMessage}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{ProductReviewService, ProductService, UserService}
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Product endpoints")
class ProductController @Inject()(components: ControllerComponents,
                                  userService: UserService,
                                  productService: ProductService,
                                  productReviewService: ProductReviewService,
                                  configuration: Configuration,
                                  silhouette: Silhouette[DefaultEnv],
                                  clock: Clock,
                                  messagesApi: MessagesApi)
                                 (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {


  @ApiOperation(value = "Creates a new product", response = classOf[ResponseMessage])
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
        value = "Product data",
        required = true,
        dataType = "models.core.product.Product",
        paramType = "body"
      )
    )
  )
  def createProduct() = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Product].map { data =>
      productService.save(data).flatMap(_ => Future.successful(Ok(Json.toJson("message" -> "Product created successfully"))))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Updates product with specified ID", response = classOf[ResponseMessage])
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
        value = "Product data",
        required = true,
        dataType = "models.core.product.Product",
        paramType = "body"
      )
    )
  )
  def updateProduct(id: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Product].map { data =>
      productService.update(data.copy(slug = id)).flatMap(_ => Future.successful(Ok(Json.toJson("message" -> "Product updated successfully"))))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(500), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Delete product with specified ID", response = classOf[ResponseMessage])
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
  def deleteProduct(id: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      removeReviews <- productReviewService.deleteAllForProduct(id)
      removeProduct <- productService.delete(id)
    } yield {
      if (removeProduct && removeReviews)
        Ok(Json.toJson("message" -> "Product deleted successfully"))
      else BadRequest(Json.toJson(Bad(code = Some(500), message = "Error")))
    }
  }

  @ApiOperation(value = "Add new review to product with specified ID", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Product data",
        required = true,
        dataType = "models.core.product.ProductReview",
        paramType = "body"
      )
    )
  )
  def addReview(productId: String) = silhouette.UserAwareAction.async(parse.json) { implicit request =>
    request.body.validate[ProductReview].map { data =>
      productService.get(productId).flatMap {
        case Some(_) =>
          productReviewService.save(data.copy(id = None, status = "pending",  date = DateTime.now(DateTimeZone.UTC).toString())).flatMap(_ => Future.successful(Ok(Json.toJson("message" -> "Product Review added successfully"))))
        case None => Future.successful(BadRequest(Json.toJson(Bad(code = Some(500), message = "Error"))))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(500), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Updates product review with specified ID", response = classOf[ResponseMessage])
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
        value = "Product data",
        required = true,
        dataType = "models.core.product.ProductReview",
        paramType = "body"
      )
    )
  )
  def updateReview(id: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[ProductReview].map { data =>
      productReviewService.update(data.copy(id = Some(id))).flatMap(_ => Future.successful(Ok(Json.toJson("message" -> "Product Review updated successfully"))))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(500), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Delete product review with specified ID", response = classOf[ResponseMessage])
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
  def deleteReview(reviewId: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      removeReview <- productReviewService.delete(reviewId)
    } yield {
      if (removeReview)
        Ok(Json.toJson("message" -> "Product Review deleted successfully"))
      else BadRequest(Json.toJson(Bad(code = Some(500), message = "Error")))
    }
  }

  @ApiOperation(value = "Get product review with specified ID", response = classOf[ProductReview])
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
  def getReview(id: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      review <- productReviewService.get(id)
    } yield {
      if(review.isDefined)
        Ok(Json.toJson(review.get))
      else NotFound(Json.toJson("message" -> "Not Found"))
    }
  }

  @ApiOperation(value = "Get product reviews with specified status", responseContainer="List", response = classOf[ProductReview])
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
  def getReviewsWithStatus(productId: String, status: String) = silhouette.SecuredAction.async { implicit request =>
    for {
      reviews <- productReviewService.getWithStatus(productId, status)
    } yield {
      Ok(Json.toJson(reviews))
    }
  }

  @ApiOperation(value = "Get unpublished product reviews", responseContainer="List", response = classOf[ProductReview])
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
  def getDraftProductReviews(id: String) = silhouette.SecuredAction.async {
    for{
      reviews <- productReviewService.getWithStatus(id, "draft")
    } yield {
      Ok(Json.toJson(reviews))
    }
  }

  /* Non administrative endpoints */

  @ApiOperation(value = "Get all products if possible", responseContainer="List", response = classOf[Product])
  def getAllProducts() = silhouette.UserAwareAction.async { implicit request =>
    for {
      products <- productService.getAll()
    } yield {
      Ok(Json.toJson(products))
    }
  }

  @ApiOperation(value = "Get all products if possible", responseContainer="List", response = classOf[ProductsPage])
  def getPaginatedProducts(pageNumber: Int) = silhouette.UserAwareAction.async { implicit request =>
    val pageN = ((pageNumber >>> 1) << 1) + (pageNumber & 1)
    for {
      page <- productService.getPaginated(pageN)
    } yield {
      Ok(Json.toJson(page))
    }
  }

  @ApiOperation(value = "Get product with specified ID", response = classOf[Product])
  def getProduct(id: String) = silhouette.UserAwareAction.async { implicit request =>
    for {
      product <- productService.get(id)
    } yield {
      if(product.isDefined)
        Ok(Json.toJson(product.get))
      else NotFound(Json.toJson("message" -> "Not Found"))
    }
  }

  @ApiOperation(value = "Get published product reviews", responseContainer="List", response = classOf[ProductReview])
  def getPublishedProductReviews(id: String) = silhouette.UserAwareAction.async {
    for{
      reviews <- productReviewService.getWithStatus(id, "published")
    } yield {
      Ok(Json.toJson(reviews))
    }
  }
}
