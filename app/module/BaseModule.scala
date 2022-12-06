package module

import com.google.inject.AbstractModule
import dao._
import net.codingwell.scalaguice.ScalaModule
import service._

/**
  * The base Guice module.
  */
class BaseModule extends AbstractModule with ScalaModule {

  /**
    * Configures the module.
    */
  def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[PostMetaService].to[PostMetaDAOImpl]
    bind[PostService].to[PostDAOImpl]
    bind[CommentService].to[CommentDAOImpl]
    bind[ProductReviewService].to[ProductReviewDAOImpl]
    bind[ProductService].to[ProductDAOImpl]
    bind[TestimonialService].to[TestimonialDAOImpl]
  }
}
