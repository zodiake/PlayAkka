package controllers

import javax.inject.Inject

import models.{Item, ItemService}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
  * Created by zodiake on 16-10-20.
  */
case class CategoryQuery(cateCode: String, period: Int, page: Int)

class CateCodeController @Inject()(val messagesApi: MessagesApi, val itemService: ItemService) extends BaseController {
  def form = Form(
    mapping("cateCode" -> text, "period" -> number, "page" -> default(number, 1))(CategoryQuery.apply)(CategoryQuery.unapply)
  )

  def list = UserAuthAction.async { implicit request =>
    form.bindFromRequest.fold(
      error => Future.successful(Ok(views.html.category.list(error, List.empty[Item]))),
      success => {
        val futureList = Future(itemService.findByCategory(success))
        for {
          list <- futureList
        } yield {
          Ok(views.html.category.list(form.fill(success), list))
        }
      })
  }
}
