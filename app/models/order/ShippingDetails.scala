package models.order

import play.api.libs.json
import play.api.libs.json.Json

case class ShippingDetails (
                           firstName: String,
                           lastName: String,
                           email: String,
                           phone: String,
                           country: String,
                           state: String,
                           city: String,
                           zip: String,
                           address1: String,
                           address2: String,
                           contactOption: String,
                           paymentOption: String,
                           notes: String,
                           )
object ShippingDetails {
  implicit val shippingDetailsFormat: json.Format[ShippingDetails] = Json.format[ShippingDetails]
}