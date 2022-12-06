package models.order

import play.api.libs.json
import play.api.libs.json.Json

case class Checkout(shippingDetails: ShippingDetails, cart: Cart)

object Checkout {
  implicit val checkoutFormat: json.Format[Checkout] = Json.format[Checkout]
}
