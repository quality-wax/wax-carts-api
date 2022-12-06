package models.core.product

import models.Pagination
import play.api.libs.json
import play.api.libs.json.Json

case class ProductsPage (
                        products: List[Product],
                        pagination: Pagination
                        )

object ProductsPage {
  implicit val productsPageFormat: json.Format[ProductsPage] = Json.format[ProductsPage]
}