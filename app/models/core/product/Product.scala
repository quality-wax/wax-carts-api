package models.core.product

import models.core.shared.{Category, Image, Seo}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class Product (
                   slug: String,
                   name: String,
                   description1: String,
                   description2: String,
                   additionalInfo: String,
                   stars: Int,
                   photos: List[Image],
                   prices: List[Price],
                   categories: List[Category],
                   seo: Seo
                   )
object Product {
  implicit val productFormat: json.Format[Product] = Json.format[Product]
  implicit val productOFormat: OFormat[Product] = Json.format[Product]
}