package controllers

import java.io.File
import java.util.Calendar

import akka.actor.{ActorSystem, Props}
import anorm._
import com.google.inject.Inject
import models.{WriteActor, write}
import play.api.db.Database
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

/**
  * Created by zodiake on 16-10-18.
  */
case class FileStatus(name: String, status: String)

class ExportController @Inject()(val database: Database, val actorSystem: ActorSystem) extends Controller {
  val dict = "/tmp/segconf"

  def export = UserAuthAction { implicit request =>
    Ok(views.html.export(request))
  }

  //post /export
  def exportExcel = Action { implicit request =>
    val writeRef = actorSystem.actorOf(Props(classOf[WriteActor], database))
    writeRef ! write
    Redirect("/confList")
  }

  //get /confList
  def list = UserAuthAction.async { implicit request =>

    def getListOfFiles(dir: String): List[String] = {
      val d = new File(dir)
      if (d.exists && d.isDirectory) {
        d.listFiles.filter(_.isFile).map(_.getName).toList
      } else {
        List[String]()
      }
    }

    val fileList = getListOfFiles(dict)
    database.withConnection(implicit conn => {
      val result = SQL("select count(*) as num from segconf").as(SqlParser.int("num").single)
      val calendar = Calendar.getInstance()
      val month = calendar.get(Calendar.MONTH)
      val year = calendar.get(Calendar.YEAR)
      val fileName = s"${dict}/${year}${month}.csv"
      val d = new File(fileName)
      val lists = if (d.exists) {
        fileList.map(i =>
          if (i == d.getName) {
            val lines = scala.io.Source.fromFile(fileName).getLines.size

            FileStatus(i, f"${lines.toDouble / result * 100}%2.2f")
          } else {
            FileStatus(i, "100")
          }
        )
      } else {
        fileList.map(i => FileStatus(i, "100"))
      }
      Future(Ok(views.html.download(lists)))
    })
  }

  def download(name: String) = UserAuthAction { implicit request =>
    Ok.sendFile(new java.io.File(dict + "/" + name))
  }
}

