package models.order

import play.api.libs.json
import play.api.libs.json._
case class CartItem (productName: String, productQuantities: String, productTotal: Double)

object CartItem {
  implicit val cartItemFormat: json.Format[CartItem] = Json.format[CartItem]
}
