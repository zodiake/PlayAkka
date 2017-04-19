package models

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import controllers.{SubBrandPageQuery, SubBrandQuery}
import play.api.db.Database

/**
  * Created by zodiake on 16-10-21.
  */
case class SubBrand(attrValue: String, sm: Long)

case class SubBrandConcrete(rawBrand: String, prodDesc: String, attribute: String, brand: String, attrValue: String)

@ImplementedBy(classOf[SubBrandServiceImpl])
trait SubBrandService {
  val pageSize = 15

  val listSql =
    """
      SELECT SUBBRAND.ATTRVALUE, SUM(SALES.SALES_VALUE) as sm
        FROM NEW_SALES            SALES,
             NEW_ITEM             ITEM,
             CODED_TRANS_ITEM CATCODE,
             CODED_TRANS_ITEM BRAND,
             CODED_TRANS_ITEM SUBBRAND
       WHERE SALES.ITEMID = ITEM.ITEMID
         AND ITEM.ITEMID = CATCODE.ITEMID
         AND CATCODE.ITEMID = BRAND.ITEMID
         AND BRAND.ITEMID = subbrand.ITEMID
         AND CATCODE.ATTRNO = 20
         AND CATCODE.ATTRVALUE = {catcode}
         AND BRAND.ATTRNO = 2126
         AND BRAND.ATTRVALUE = {attrValue}
         AND SUBBRAND.ATTRNO = {attrNo}
         AND ITEM.MARK = {mark}
         AND ITEM.PERIODCODE = {periodcode}
         AND SALES.PERIODCODE = {periodcode}
         AND CATCODE.PERIODCODE = {periodcode}
         AND BRAND.PERIODCODE = {periodcode}
         AND SUBBRAND.PERIODCODE = {periodcode}
      GROUP BY SALES.PERIODCODE, SUBBRAND.ATTRVALUE
      order by sm desc
    """

  val concreteSql =
    """
      select * from(
      SELECT ITEM.PERIODCODE,
             ITEM.BRAND RAWBRAND,
             ITEM.PROD_DESC_RAW,
             ITEM.attribute,
             BRAND.ATTRVALUE BRAND,
             subbrand.ATTRVALUE SUBBRAND,rownum r
        FROM NEW_ITEM ITEM,
             CODED_TRANS_ITEM CATCODE,
             CODED_TRANS_ITEM BRAND,
             CODED_TRANS_ITEM SUBBRAND
       WHERE ITEM.ITEMID = CATCODE.ITEMID
         AND CATCODE.ITEMID = BRAND.ITEMID
         AND BRAND.ITEMID=SUBBRAND.ITEMID
         AND CATCODE.attrno = 20
         AND CATCODE.attrvalue = {catcode}
         AND BRAND.ATTRNO = 2126
         AND BRAND.attrvalue={attrValue}
         AND subbrand.ATTRNO={attrNo}
         AND ITEM.MARK ={mark}
         AND ITEM.PERIODCODE={period}
         AND BRAND.PERIODCODE ={period}
         AND CATCODE.PERIODCODE ={period}
         AND SUBBRAND.PERIODCODE ={period}
         and rownum<={maxSize}
         )
         where r>{minSize}
    """

  val parser = str("attrvalue") ~ long("sm") map {
    case b ~ c => SubBrand(b, c)
  }

  val concreteParser = str("rawbrand") ~ str("prod_desc_raw") ~ str("attribute") ~ str("brand") ~ str("subbrand") map {
    case a ~ b ~ c ~ d ~ e => SubBrandConcrete(a, b, c, d, e)
  }

  def findByQuery(subBrandQuery: SubBrandQuery): List[SubBrand]

  def findConcreteByQuery(subBrandQuery: SubBrandPageQuery): List[SubBrandConcrete]
}

class SubBrandServiceImpl @Inject()(val database: Database) extends SubBrandService {
  override def findByQuery(subBrandQuery: SubBrandQuery): List[SubBrand] = {
    database.withConnection { implicit conn =>
      SQL(listSql).on('catcode -> subBrandQuery.cateCode, 'attrValue -> subBrandQuery.attrValue, 'attrNo -> subBrandQuery.attrNo, 'mark -> subBrandQuery.mark, 'periodcode -> subBrandQuery.period).as(parser.*)
    }
  }

  override def findConcreteByQuery(subBrandQuery: SubBrandPageQuery): List[SubBrandConcrete] = {
    database.withConnection { implicit conn =>
      SQL(concreteSql).on('catcode -> subBrandQuery.cateCode, 'attrValue -> subBrandQuery.attrValue, 'attrNo -> subBrandQuery.attrNo, 'mark -> subBrandQuery.mark, 'period -> subBrandQuery.period, 'minSize -> (subBrandQuery.page - 1) * pageSize, 'maxSize -> subBrandQuery.page * pageSize).as(concreteParser.*)
    }
  }
}
