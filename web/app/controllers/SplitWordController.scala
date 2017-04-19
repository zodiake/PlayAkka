package controllers

import com.google.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.i18n.MessagesApi

import scala.concurrent.Future
import scala.sys.process._
import com.typesafe.config.ConfigFactory

/**
  * Created by zodiake on 16-11-29.
  */
case class SplitWord(source: String, sink: String, dict: String)

class SplitWordController @Inject()(val database: Database, val messagesApi: MessagesApi) extends BaseController {
  val form = Form(
    mapping("source" -> nonEmptyText,
      "sink" -> nonEmptyText, "dict" -> nonEmptyText)(SplitWord.apply)(SplitWord.unapply)
  )

  def split = UserAuthAction { implicit request =>
    Ok(views.html.split(form))
  }

  def post = UserAuthAction { implicit request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    form.bindFromRequest.fold(error => Ok(views.html.split(error)), success => {
      val oldDict = "D:/ICTCLAS50_oldDict"
      val newDict = "D:/ICTCLAS50_newDict"
      val dict = if (success.dict == "old") oldDict else newDict
      Future(s"python ${dict}/HDFS.py ${success.source} ${success.sink}".!)
      Redirect("/splitWord").flashing(("haha", "haha"))
    })
  }
}
