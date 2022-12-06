package models.core

import play.api.libs.json
import play.api.libs.json.Json

case class Contact (
                   firstName: String,
                   lastName: String,
                   email: String,
                   phone: String,
                   subject: String,
                   message: String,
                   )

object Contact {
  implicit val contactFormat: json.Format[Contact] = Json.format[Contact]
}
