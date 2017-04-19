package models

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import controllers.ProductQuery
import play.api.db.Database

/**
  * Created by zodiake on 16-10-21.
  */
case class Product(id: BigDecimal, attrNo: BigDecimal, attrValue: String, storeId: String, prodId: String, levelI: String, levelII: String, brand: String, tp: Option[String])

@ImplementedBy(classOf[ProductServiceImpl])
trait ProductService {
  val list =
    """
      SELECT CODING.ITEMID,CODING.ATTRNO,CODING.ATTRVALUE,ITEM.STORE_ID,
      ITEM.PROD_ID,ITEM.CATEGORY_LEVEL_I,ITEM.CATEGORY_LEVEL_II,ITEM.BRAND,ITEM.TYPE,
      ITEM.PROD_DESC_RAW,ITEM.ATTRIBUTE FROM
      (select * from new_item where prod_id = {prodId} and periodcode = {periodCode} and mark = {mark}) ITEM,CODED_TRANS_ITEM CODING
      WHERE ITEM.ITEMID = CODING.ITEMID
    """

  val productParser = get[BigDecimal]("itemid") ~ get[BigDecimal]("attrNo") ~ str("attrValue") ~ str("store_id") ~ str("prod_id") ~ str("CATEGORY_LEVEL_I") ~ str("CATEGORY_LEVEL_II") ~ str("brand") ~ get[Option[String]]("type") map {
    case a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i => Product(a, b, c, d, e, f, g, h, i)
  }

  def findByQuery(productQuery: ProductQuery): List[Product]
}

class ProductServiceImpl @Inject()(val database: Database) extends ProductService {
  override def findByQuery(productQuery: ProductQuery): List[Product] = {
    database.withConnection { implicit conn =>
      SQL(list).on('prodId -> productQuery.prodId, 'periodCode -> productQuery.periodCode, 'mark -> productQuery.mark).as(productParser.*)
    }
  }
}
