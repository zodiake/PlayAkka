package controllers

import com.google.inject.Inject
import models.{Product, ProductService}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

import scala.concurrent.Future

/**
  * Created by zodiake on 16-10-21.
  */
case class ProductQuery(prodId: String, periodCode: Int, mark: String)

class ProductQueryController @Inject()(val productService: ProductService, val messagesApi: MessagesApi) extends BaseController {
  val form = Form(
    mapping(
      "prodId" -> text,
      "periodCode" -> number,
      "mark" -> text
    )(ProductQuery.apply)(ProductQuery.unapply)
  )

  def list = UserAuthAction.async { implicit request =>
    form.bindFromRequest.fold(
      error => Future.successful(Ok(views.html.productQuery.list(error, List.empty[Product]))),
      success => {
        val list = productService.findByQuery(success)
        Future.successful(Ok(views.html.productQuery.list(form.fill(success), list)))
      }
    )
  }
}
