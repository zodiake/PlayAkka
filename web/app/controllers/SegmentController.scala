package controllers

import com.google.inject.Inject
import models.{AggSegment, SegmentDesc, SegmentItem, SegmentItemService}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

import scala.concurrent.Future

/**
  * Created by zodiake on 16-10-20.
  */
case class SegmentQuery(category: String, segment: String, period: Int, page: Int)

case class SegmentDescQuery(catCode: String, attrNo: String, attrValue: String, period: Int, mark: String, page: Int)

case class SegmentNoNameQuery(attrValue: String, attrNo: String, mark: String, period: Int)

class SegmentController @Inject()(val messagesApi: MessagesApi, val segmentItemService: SegmentItemService) extends BaseController {
  val form = Form(
    mapping("category" -> text, "segment" -> text, "period" -> number, "page" -> default(number, 1))(SegmentQuery.apply)(SegmentQuery.unapply)
  )

  val noNameForm = Form(
    mapping("attrValue" -> text, "attrNo" -> text, "mark" -> text, "period" -> default(number, 1))(SegmentNoNameQuery.apply)(SegmentNoNameQuery.unapply)
  )

  val descForm = Form(
    mapping("catCode" -> text, "attrNo" -> text, "attrValue" -> text, "period" -> number, "mark" -> text, "page" -> default(number, 1))(SegmentDescQuery.apply)(SegmentDescQuery.unapply)
  )

  def list = UserAuthAction.async { implicit request =>
    form.bindFromRequest.fold(
      error => Future.successful(Ok(views.html.segment.list(error, List.empty[SegmentItem]))),
      success => {
        val list = segmentItemService.findBySegment(success)
        Future.successful(Ok(views.html.segment.list(form, list)))
      })
  }

  def noName = UserAuthAction.async { implicit request =>
    noNameForm.bindFromRequest.fold(
      error => Future.successful(Ok(views.html.segment.noNameList(error, List.empty[AggSegment]))),
      success => {
        val list = segmentItemService.findNoNameSegment(success)
        Future.successful(Ok(views.html.segment.noNameList(noNameForm.fill(success), list)))
      }
    )
  }

  def segmentDesc = UserAuthAction.async { implicit request =>
    descForm.bindFromRequest.fold(
      error => Future.successful(Ok(views.html.segment.desc(error, List.empty[SegmentDesc]))),
      success => {
        val list = segmentItemService.findSegmentDesc(success)
        Future.successful(Ok(views.html.segment.desc(descForm.fill(success), list)))
      }
    )
  }
}
