package controllers

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import io.swagger.annotations.{Api, ApiOperation}
import models.core.Contact
import models.order.Checkout
import play.api.libs.json.{JsError, Json}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc._
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.Future

@Api(value = "Order and Contact")
class OrderController @Inject()(components: ControllerComponents,
                                silhouette: Silhouette[DefaultEnv],
                                mailerClient: MailerClient) extends AbstractController(components) {

  def checkout = Action.async(parse.json) { implicit request =>
    request.body.validate[Checkout].map { checkout =>
      mailerClient.send(Email(
        subject = "New Order",
        from = "noreply@brimweb.com",
        to = Seq("sales@qualitywaxandcarts.com"),
        bodyText = Some(views.txt.emails.order(checkout).body),
        bodyHtml = Some(views.html.emails.order(checkout).body)
      ))
      Future.successful(Ok(Json.toJson("message" -> "Thank you. Your order is being processed")))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }

  def contact = Action.async(parse.json) { implicit request =>
    request.body.validate[Contact].map { contact =>
      mailerClient.send(Email(
        subject = "New Message",
        from = "noreply@brimweb.com",
        to = Seq("infos@qualitywaxandcarts.com"),
        bodyText = Some(views.txt.emails.contact(contact).body),
        bodyHtml = Some(views.html.emails.contact(contact).body)
      ))
      Future.successful(Ok(Json.toJson("message" -> "Your message has been sent. We will get to you ASAP")))
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(code = Some(400), message = JsError.toJson(error)))))
    }
  }
}
