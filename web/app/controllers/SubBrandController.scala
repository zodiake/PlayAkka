package controllers

import com.google.inject.Inject
import models.{SubBrand, SubBrandConcrete, SubBrandService}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

import scala.concurrent.Future


/**
  * Created by zodiake on 16-10-21.
  */
case class SubBrandQuery(cateCode: String, attrValue: String, attrNo: Int, mark: String, period: Int)

case class SubBrandPageQuery(cateCode: String, attrValue: String, attrNo: Int, mark: String, period: Int, page: Int = 1)

class SubBrandController @Inject()(val subBrandService: SubBrandService, val messagesApi: MessagesApi) extends BaseController {
  val form = Form(
    mapping(
      "cateCode" -> text,
      "attrValue" -> text,
      "attrNo" -> number,
      "mark" -> text,
      "period" -> number
    )(SubBrandQuery.apply)(SubBrandQuery.unapply)
  )

  val pageForm = Form(
    mapping(
      "cateCode" -> text,
      "attrValue" -> text,
      "attrNo" -> number,
      "mark" -> text,
      "period" -> number,
      "page" -> default(number, 1)
    )(SubBrandPageQuery.apply)(SubBrandPageQuery.unapply)
  )

  def list = UserAuthAction.async { implicit request =>
    form.bindFromRequest.fold(
      error => Future.successful(Ok(views.html.subBrand.list(error, List.empty[SubBrand]))),
      success => {
        val list = subBrandService.findByQuery(success)
        Future.successful(Ok(views.html.subBrand.list(form.fill(success), list)))
      }
    )
  }

  def concrete = UserAuthAction.async { implicit request =>
    pageForm.bindFromRequest.fold(
      error => Future.successful(Ok(views.html.subBrand.concreteList(error, List.empty[SubBrandConcrete]))),
      success => {
        val list = subBrandService.findConcreteByQuery(success)
        Future.successful(Ok(views.html.subBrand.concreteList(pageForm.fill(success), list)))
      }
    )
  }
}
