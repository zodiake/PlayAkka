package controllers

import java.io.{File, PrintWriter}

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.proxy.RemoteLookupProxy
import com.google.inject.Inject
import models.DbXmlService
import nielsen.actor.XmlFileActor.DbXml.Fact
import nielsen.actor.XmlFileActor.DbXml
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Action
import service.XmlService

import scala.concurrent.Future

/**
  * Created by zodiake on 17-4-12.
  */
case class XmlForm(tableName: String, dbName: String, lang: String, mbd: List[String],
                   period: Int, remoteHost: Option[String], nd: Option[Boolean],
                   wd: Option[Boolean], salesValue: Option[String], salesVolume: Option[String], averagePrice: Option[String], version: String)

class XmlController @Inject()(val messagesApi: MessagesApi, val system: ActorSystem,
                              val configuration: Configuration, val dbXmlService: DbXmlService) extends BaseController {
  lazy val remoteActor = createRemoteFileDeploy

  def dbXml = UserAuthAction { implicit request =>

    Ok(views.html.hosts.create(form))
  }

  def deployXml = UserAuthAction.async { implicit request =>
    form.bindFromRequest.fold(
      error => {
        Logger.debug(error.errors.toString())
        Future.successful(Ok(views.html.hosts.create(error)))
      },
      success => {
        val hLevels = dbXmlService.findByCategory(success.dbName)
        val nd = success.nd.map(_ => "ND").map(_ -> "FACT1")
        val wd = success.wd.map(_ => "WD").map(_ -> "FACT2")
        val sv = success.salesValue.map("SALESVALUE(" + _ + ")").map(_ -> "FACT3")
        val svo = success.salesVolume.map("SALESVALUE(" + _ + ")").map(_ -> "FACT4")
        val ap = success.averagePrice.map("AVERAGEPRICE(" + _ + ")").map(_ -> "FACT5")
        val facts = List(nd, wd, sv, svo, ap).filter(_ != None).map {
          case Some(i) => Fact(i._1, i._2)
        }
        val message = DbXml(success.tableName, success.dbName, success.lang, success.mbd, success.period, "", hLevels, facts, success.version)
        Logger.debug(hLevels.toString)
        //remoteActor(success.remoteHost) ! message
        val xml = XmlService.toXml(success.tableName, success.dbName, success.lang, success.mbd, success.period, "", hLevels, facts, success.version)

        val path = configuration.getString("path.xml").get
        val pw = new PrintWriter(new File(path + request.id.toString + ".xml"))
        //Logger.debug(xml)
        pw.write(xml)
        pw.close()
        Future.successful(Ok.sendFile(content = new java.io.File(path + request.id.toString + ".xml"), fileName = _ => success.tableName + ".xml").withHeaders(("Content-Disposition", "attachment; filename=" + success.tableName + ".xml")))
      })
  }

  def form = Form(
    mapping(
      "tableName" -> text, "dbName" -> text,
      "lang" -> text, "mbd" -> list(text),
      "period" -> number, "remoteHost" -> optional(text),
      "nd" -> optional(boolean), "wd" -> optional(boolean),
      "salesValue" -> optional(text), "salesVolume" -> optional(text),
      "averagePrice" -> optional(text), "version" -> text)(XmlForm.apply)(XmlForm.unapply)
  )

  def createRemoteFileDeploy: Map[String, ActorRef] = {
    val path = createPath().toMap
    path.map(entry => entry._1 -> system.actorOf(Props(new RemoteLookupProxy(entry._2)), "actor" + entry._1))
  }

  def createPath(): List[(String, String)] = {
    import collection.JavaConverters._
    val protocol = configuration.getString("host.protocol").getOrElse("akka.tcp")
    val port = configuration.getString("host.port").getOrElse("2551")
    val systemName = configuration.getString("host.system").getOrElse("remoteFileSystem")
    val actorName = configuration.getString("host.actor").getOrElse("remoteFile")
    val hosts = configuration.getStringList("host.remoteHost ").get.asScala
    hosts.map(i => (i, s"$protocol://$systemName@$i:$port/$actorName")).toList
  }

  def getAllTableName = Action {
    Ok(Json.toJson(dbXmlService.findAllDbNames))
  }

}
