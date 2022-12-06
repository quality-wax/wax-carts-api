package models

import play.api.libs.json
import play.api.libs.json.Json

case class Pagination (
                      currentPage: Long,
                      totalPages: Long,
                      pageCount: Long //Maximum number of items in page
                      )

object Pagination {
    implicit val paginationFormat: json.Format[Pagination] = Json.format[Pagination]

}