package controllers

import com.google.inject.Inject
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.libs.json.Json

import scala.concurrent.Future

/**
  * Created by zodiake on 16-10-26.
  */
case class CodingQuery(category: String, segType: Int, keyWords: String, depends: Option[String], segTypeCode: String, page: Int)

case class PreCategoryQuery(keyword: String)

class PreCodingController @Inject()(val preCodingResultsService: PreCodingResultsService, val segmentService: SegmentService, val messagesApi: MessagesApi) extends BaseController {
  val form = Form(
    mapping(
      "category" -> text,
      "segType" -> number,
      "keyWords" -> text,
      "depends" -> optional(text),
      "segTypeCode" -> text,
      "page" -> default(number, 1)
    )(CodingQuery.apply)(CodingQuery.unapply) verifying("subbrand depend brand", fields => fields match {
      case codingQuery => validate(codingQuery.segTypeCode, codingQuery.depends)
    })
  )

  val categoryForm = Form(
    mapping(
      "keyword" -> text
    )(PreCategoryQuery.apply)(PreCategoryQuery.unapply)
  )

  def validate(s: String, de: Option[String]) = (s, de) match {
    case (a, None) if (a.indexOf("SUBBRAND") > -1) => false
    case _ => true
  }

  def list = UserAuthAction.async { implicit request =>
    form.bindFromRequest.fold(
      error => {
        val segList = error("category").value.map(i => {
          segmentService.findByCategory(i)
        }).getOrElse(List.empty[Segment])
        Future.successful(Ok(views.html.pre.list(error, List.empty[PreCodingResults], List.empty[GroupInfo], 0, segList)))
      },
      success => {
        val list = preCodingResultsService.findByCodingQuery(success)
        val segList = segmentService.findByCategory(success.category)
        Future.successful(Ok(views.html.pre.list(form.fill(success), list._1, list._2, list._3, segList)))
      }
    )
  }

  def findByCategory = UserAuthAction { implicit request =>
    import models.Segment._
    val category = request.getQueryString("category")
    category.map(i => {
      val list = segmentService.findByCategory(i)
      Ok(Json.toJson(list))
    }) getOrElse {
      val empty = Json.parse( """[]""")
      Ok(empty)
    }
  }

  def preCategory = UserAuthAction { implicit request =>
    categoryForm.bindFromRequest.fold(
      error => Ok(views.html.pre.categoryList(error, List.empty[GroupInfo])),
      success => {
        val list = preCodingResultsService.findPreCategoryQuery(success.keyword)
        Ok(views.html.pre.categoryList(categoryForm.fill(success), list))
      }
    )
  }
}
