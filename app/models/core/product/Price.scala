package models.core.product

import play.api.libs.json
import play.api.libs.json.Json

case class Price (
                 unit: String,
                 amount: Double
                 )

object Price {
  implicit val priceFormat: json.Format[Price] = Json.format[Price]
}
