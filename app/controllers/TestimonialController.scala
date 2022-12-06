package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import javax.inject.Inject
import models.core.Testimonial
import models.swagger.{ObjectList, ResponseMessage}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{TestimonialService, UserService}
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Store Testimonial endpoints")
class TestimonialController @Inject()(components: ControllerComponents,
                                      testimonialService: TestimonialService,
                                      configuration: Configuration,
                                      silhouette: Silhouette[DefaultEnv],
                                      clock: Clock,
                                      messagesApi: MessagesApi)
                                     (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  @ApiOperation(value = "Creates a new testimonial", response = classOf[ResponseMessage])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Testimonial data",
        required = true,
        dataType = "models.core.Testimonial",
        paramType = "body"
      )
    )
  )
  def createTestimonial() = silhouette.UserAwareAction.async(parse.json) { implicit request =>
    request.body.validate[Testimonial].map { data =>
      testimonialService.save(data.copy(id = None, status = "draft", date = DateTime.now(DateTimeZone.UTC).toString())).flatMap(_ => Future.successful(Ok(Json.toJson("message" -> "Testimonial created successfully"))))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Update testimonial with specified ID", response = classOf[ResponseMessage])
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
        value = "Testimonial data",
        required = true,
        dataType = "models.core.Testimonial",
        paramType = "body"
      )
    )
  )
  def updateTestimonial(id: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Testimonial].map { data =>
      testimonialService.update(data).flatMap(_ => Future.successful(Ok(Json.toJson("message" -> "Testimonial updated successfully"))))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  @ApiOperation(value = "Delete testimonial with specified ID", response = classOf[ResponseMessage])
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
  def deleteTestimonial(id: String)  = silhouette.SecuredAction.async(parse.json) { implicit request =>
    for{
      isDeleted <- testimonialService.delete(id)
    } yield {
      if(isDeleted)
        Ok(Json.toJson("message" -> "Testimonial has been deleted successfully"))
      else BadRequest(Json.toJson("message" -> "Failed to delete testimonial"))
    }
  }

  @ApiOperation(value = "Get testimonial with specified ID", response = classOf[Testimonial])
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
  def getTestimonial(id: String)  = silhouette.SecuredAction.async(parse.json) { implicit request =>
    for{
      testimonial <- testimonialService.get(id)
    } yield {
      if(testimonial.isDefined)
        Ok(Json.toJson(testimonial))
      else NotFound(Json.toJson("message" -> "Not found"))
    }
  }

  @ApiOperation(value = "Get all store testimonials", responseContainer="List", response = classOf[Testimonial])
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
  def getTestimonials()  = silhouette.SecuredAction.async(parse.json) { implicit request =>
    for{
      testimonials <- testimonialService.getAll()
    } yield {
        Ok(Json.toJson(testimonials))
    }
  }

  @ApiOperation(value = "Get all published testimonials", responseContainer="List", response = classOf[Testimonial])
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
  def getPublishedTestimonials()  = silhouette.UserAwareAction.async(parse.json) { implicit request =>
    for{
      testimonials <- testimonialService.getAllWithStatus("published")
    } yield {
      Ok(Json.toJson(testimonials))
    }
  }

  @ApiOperation(value = "Get all unpublished testimonials", responseContainer="List", response = classOf[Testimonial])
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
  def getDraftTestimonials()  = silhouette.SecuredAction.async(parse.json) { implicit request =>
    for{
      testimonials <- testimonialService.getAllWithStatus("draft")
    } yield {
      Ok(Json.toJson(testimonials))
    }
  }
}
