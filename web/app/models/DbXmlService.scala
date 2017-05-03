package models

import anorm._
import com.google.inject.{ImplementedBy, Inject, Singleton}
import nielsen.actor.XmlFileActor.DbXml.Hlevel
import play.api.db.Database

import scala.xml.{Elem, NodeBuffer}

/**
  * Created by zodiake on 17-4-12.
  */

@ImplementedBy(classOf[DbXmlServiceImpl])
trait DbXmlService {
  def findByCategory(category: String): List[Hlevel]

  def findAllDbNames: List[String]
}

object DbXmlServiceImpl {

  import anorm.SqlParser._

  val sql =
    """
      select * from db_customer_request_v4 where db_name={dbName}
    """
  val getAllDBNames =
    """
      select distinct(db_name) as names from db_customer_request_v4
    """
  val hlevelParser = get[String]("segcode") ~ get[Int]("seqence") map {
    case segCode ~ sequence => Hlevel(segCode, sequence)
  }
}

@Singleton
class DbXmlServiceImpl @Inject()(val database: Database) extends DbXmlService {

  import DbXmlServiceImpl._

  override def findByCategory(category: String): List[Hlevel] = {
    database.withConnection(implicit conn =>
      SQL(sql).on("dbName" -> category).as(hlevelParser.*)
    )
  }

  override def findAllDbNames: List[String] = {
    database.withConnection(implicit conn =>
      SQL(getAllDBNames).as(SqlParser.str("names").*)
    )
  }
}

