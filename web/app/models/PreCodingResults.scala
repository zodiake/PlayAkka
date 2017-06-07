package models

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import controllers.CodingQuery
import play.api.Logger
import play.api.cache.CacheApi
import play.api.db.Database
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by zodiake on 16-10-26.
  */
case class Segment(code: String, name: String)

case class PreCodingResults(id: BigDecimal, brand: Option[String], segType: Option[String], description: Option[String], attr: Option[String], attrValue: Option[String])

case class GroupInfo(name: String, c: Long)

@ImplementedBy(classOf[PreCodingResultsServiceImpl])
trait PreCodingResultsService {
  val sqlSelect = " SELECT result.itemid, result.brand, result.type as segType, result.proD_desc_raw, result.attribute, item.attrvalue, rownum r "
  val sqlCount = " select count(*) "

  val sqlWithBrand = " FROM coding_Result4check result,coded_Trans_item_v40 item WHERE result.itemid = item.itemid AND item.periodcode = SUBSTR(result.itemid,1,8) AND RESULT.CATCODE = {category} AND ITEM.ATTRNO = {attrno} "

  def findByCodingQuery(query: CodingQuery): (List[PreCodingResults], List[GroupInfo], Long)

  def findPreCategoryQuery(keyword: String): List[GroupInfo]
}

object PreCodingResultsServiceImpl {
  val size = 20
  val withBrandParser = get[BigDecimal]("itemid") ~ get[Option[String]]("brand") ~ get[Option[String]]("segType") ~ get[Option[String]]("prod_desc_raw") ~ get[Option[String]]("attribute") ~ get[Option[String]]("attrvalue") map {
    case a ~ b ~ c ~ d ~ e ~ f => PreCodingResults(a, b, c, d, e, f)
  }

  val groupParser = str("catcode") ~ long("c") map {
    case a ~ b => GroupInfo(a, b)
  }
}

class PreCodingResultsServiceImpl @Inject()(val database: Database, val cacheApi: CacheApi) extends PreCodingResultsService {

  import PreCodingResultsServiceImpl._

  val logger = Logger(this.getClass)

  override def findByCodingQuery(query: CodingQuery): (List[PreCodingResults], List[GroupInfo], Long) = {
    database.withConnection(implicit conn => {
      val s = SQL(s"select category_check.get_seg_desc('${query.keyWords}') as sql from dual").as(SqlParser.str("sql").single)

      val whereLike = s.substring(s.indexOf("WHERE") + 5)
      val sqlWhere = query.depends.map(_ => sqlWithBrand + " and rownum<={maxSize} and (" + whereLike + ") AND RESULT.BRANDCODE = {brand}").getOrElse(sqlWithBrand + " and rownum<={maxSize} and (" + whereLike + ")")
      val sql = "select * from (" + sqlSelect + sqlWhere + "order by itemid) where r>{minSize}"
      val sqlC = sqlCount + query.depends.map(_ => sqlWithBrand + " and (" + whereLike + ") AND RESULT.BRANDCODE = {brand}").getOrElse(sqlWithBrand + " and (" + whereLike + ")")
      val sqlg = "select item.attrvalue as catcode,count(*) as c " + query.depends.map(_ => sqlWithBrand + " and (" + whereLike + ") AND RESULT.BRANDCODE = {brand} ").getOrElse(sqlWithBrand + " and (" + whereLike + ")") + " group by item.attrvalue order by c desc"
      val parametes: Array[NamedParameter] = Array('category -> query.category, 'brand -> query.depends, 'attrno -> query.segType, 'maxSize -> query.page * size, 'minSize -> (query.page - 1) * size)
      println(sql)
      val groupInfo = cacheApi.getOrElse("query." + query)(
        database.withConnection(implicit conn => {
          SQL(sqlg).on(parametes: _*).as(groupParser.*)
        }))
      (SQL(sql).on(parametes: _*).as(withBrandParser.*), groupInfo, SQL(sqlC).on(parametes: _*).as(scalar[Long].single))
    })
  }

  override def findPreCategoryQuery(keyword: String): List[GroupInfo] = {
    val whereLike = keyword.split(";").map(i => {
      s"""( BRAND||TYPE||PROD_DESC_RAW||ATTRIBUTE LIKE '%${i}%' )"""
    }).mkString(" or ")
    val sqlGroup = "SELECT RESULT.CATCODE,COUNT(*) as c FROM coding_Result4check RESULT WHERE  " + whereLike + " GROUP BY CATCODE order by c desc"
    logger.debug(sqlGroup)
    cacheApi.getOrElse("keyWords." + keyword)(
      database.withConnection(implicit conn => {
        SQL(sqlGroup).as(groupParser.*)
      }))
  }
}


object Segment {
  val parser = str("segno") ~ str("segcode") map {
    case a ~ b => new Segment(a, b)
  }

  implicit val jsonWriter: Writes[Segment] = (
    (JsPath \ "code").write[String] and
      (JsPath \ "name").write[String]
    ) (unlift(Segment.unapply))
}

@ImplementedBy(classOf[SegmentServiceImpl])
trait SegmentService {
  val findByCategorySql =
    """
      select segno,segcode from db_cate_segment where catcode={category} order by segcode
    """

  def findByCategory(category: String): List[Segment]
}

class SegmentServiceImpl @Inject()(val database: Database, val cache: CacheApi) extends SegmentService {

  import Segment._

  override def findByCategory(category: String): List[Segment] = {
    cache.getOrElse("category." + category)(
      database.withConnection { implicit conn =>
        SQL(findByCategorySql).on("category" -> category).as(parser.*)
      }
    )
  }
}
