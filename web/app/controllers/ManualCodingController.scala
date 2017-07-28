package controllers

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import spark.SegmentCoding
import play.api.libs.json._

import scala.io.Codec

@Singleton
class ManualCodingController @Inject()(val messagesApi: MessagesApi, val config: Configuration) extends BaseController {

  val kraFilePath = config.getString("kraFile").getOrElse("")
  val catFilePath = config.getString("catFile").getOrElse("")
  val configFilePath = config.getString("configFile").getOrElse("")

  val configFile = scala.io.Source.fromFile(configFilePath)(Codec.UTF8).getLines().toStream.map(_.split(","))
  val catFile = scala.io.Source.fromFile(catFilePath)(Codec.UTF8).getLines().map(_.split(",")).toList

  def manual(description: Option[String], customConfigFile: Option[Array[String]]) = Action { implicit req =>
    customConfigFile.fold(
      description.fold(Ok(views.html.manual(req)))(i => Ok(Json.toJson(SegmentCoding.coding(i, kraFilePath, configFile, catFile))))
    )(i =>

      description.fold(Ok(views.html.manual(req)))(i => Ok(Json.toJson(SegmentCoding.coding(i, kraFilePath, configFile, catFile))))
    )
  }
}
