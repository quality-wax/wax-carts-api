package models.order

import play.api.libs.json
import play.api.libs.json._

case class Cart (cartItems: List[CartItem], grandTotal: Double)

object Cart {
  implicit val cartFormat: json.Format[Cart] = Json.format[Cart]
}