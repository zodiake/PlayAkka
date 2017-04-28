package controllers

import com.google.inject.Inject
import models.{Top100Service, Top100Update}
import play.api.data.{Form, Forms}
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsPath, Reads}
import play.api.mvc.{Action, BodyParsers, Session}
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by zodiake on 17-4-20.
  */
object CategoryCheckController {
  val lastCheckTimestamp = "lastTimestamp"
  val lastDoubleCheckTimestamp = "lastDoubleTimestamp"

  val form = Form(
    Forms.tuple(
      "web" -> Forms.text,
      "category" -> Forms.text,
      "period" -> Forms.number
    )
  )

  case class PostJson(category: String, period: String, list: Seq[Top100Update])

  implicit val top100Reads: Reads[Top100Update] = (
    (JsPath \ "itemId").read[BigDecimal] and
      (JsPath \ "cateCode").read[String]
    ) (Top100Update.apply _)

  implicit val postJsonReads: Reads[PostJson] = (
    (JsPath \ "category").read[String] and
      (JsPath \ "period").read[String] and
      (JsPath \ "list").read[Seq[Top100Update]]
    ) (PostJson.apply _)
}

class CategoryCheckController @Inject()(val service: Top100Service, val messagesApi: MessagesApi) extends BaseController {

  import CategoryCheckController._

  def getQueryForm = UserAuthAction { implicit request =>
    form.bindFromRequest.fold(
      error => Ok(views.html.checkCategory.list(form)),
      success => {
        val rows = service.findByCategoryAndWeb(success._2, success._1, success._3)
        val result = if (rows.size == 0) None else Some(rows)
        Ok(views.html.checkCategory.list(form.fill(success), result)).addingToSession(lastCheckTimestamp -> System.currentTimeMillis().toString)
      }
    )
  }

  def getCheckForm = UserAuthAction { implicit request =>
    form.bindFromRequest.fold(
      error => Ok(views.html.checkCategory.doubleList(form)),
      success => {
        val rows = service.findByCategoryAndWebAndChecked(success._2, success._1, success._3)
        val result = if (rows.size == 0) None else Some(rows)
        Ok(views.html.checkCategory.doubleList(form.fill(success), result)).addingToSession(lastDoubleCheckTimestamp -> System.currentTimeMillis().toString)
      }
    )
  }

  def update = UserAuthAction(BodyParsers.parse.json) { implicit request =>
    request.body.validate[PostJson].fold(
      error => Ok(views.html.checkCategory.list(form)),
      success => {
        val list = success.list.filter(i => !i.cateCode.isEmpty && i.cateCode != success.category)
        val diff = getDuration(request.session, lastCheckTimestamp)
        service.updateCategoryById(list, success.category, success.period, diff, request.session("app.name"))
        Ok(Json.toJson("ok"))
      }
    )
  }

  def updateCheckForm = UserAuthAction(BodyParsers.parse.json) { implicit request =>
    request.body.validate[PostJson].fold(
      error => Ok(views.html.checkCategory.list(form)),
      success => {
        val list = success.list.filter(i => !i.cateCode.isEmpty && i.cateCode != success.category)
        val diff = getDuration(request.session, lastDoubleCheckTimestamp)
        service.updateCheckedRows(list, success.category, success.period, diff, request.session("app.name"))
        Ok(Json.toJson("ok"))
      }
    )
  }

  private def getDuration(session: Session, key: String): Double = {
    val beginTime = (session get key).getOrElse(System.currentTimeMillis().toString)
    session - key
    (System.currentTimeMillis() - beginTime.toLong) / 6000
  }

  def category = Action { request =>
    Ok(Json.toJson(service.findAllCategory))
  }
}
