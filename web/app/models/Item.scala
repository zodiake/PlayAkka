package models

import anorm._
import com.google.inject.{ImplementedBy, Inject, Singleton}
import controllers.CategoryQuery
import play.api.db.Database

/**
  * Created by zodiake on 16-10-20.
  */
case class Item(id: BigDecimal, catCode: String, brand: Option[String], itemType: Option[String], desc: String)

@ImplementedBy(classOf[ItemServiceImpl])
trait ItemService {
  val pageSize = 20

  def database: Database

  def findByCategory(category: CategoryQuery): List[Item]

  def countByCategory(category: CategoryQuery): Int

}

object ItemServiceImpl {

  import anorm.SqlParser._

  val findByCategorySql =
    """
      select itemid,catcode,brand,type,prod_desc_raw from (
      select
      c.itemid,c.catcode,n.brand,n.type ,n.prod_desc_raw,rownum r
      from CODED_TRANS_catcode c
      join new_item n on n.itemid=c.itemid
      where n.periodcode={period} and c.catcode={cateCode}
      and rownum<={maxSize} order by c.itemid )
      where r>{minSize}
    """


  val countByCategorySql =
    """
      select
      count(*) as num
      from CODED_TRANS_catcode c
      join new_item n on n.itemid=c.itemid
      where n.periodcode={period} and c.catcode={cateCode}
    """

  val categoryParser = get[BigDecimal]("itemid") ~ str("catcode") ~ get[Option[String]]("brand") ~ get[Option[String]]("type") ~ str("prod_desc_raw") map {
    case id ~ code ~ brand ~ t ~ d => Item(id, code, brand, t, d)
  }

}

@Singleton
class ItemServiceImpl @Inject()(val database: Database) extends ItemService {

  import ItemServiceImpl._

  override def findByCategory(categoryQuery: CategoryQuery): List[Item] = {
    database.withConnection(implicit conn =>
      SQL(findByCategorySql).on('period -> categoryQuery.period, 'cateCode -> categoryQuery.cateCode, 'minSize -> (categoryQuery.page - 1) * pageSize, 'maxSize -> categoryQuery.page * pageSize).as(categoryParser.*)
    )
  }

  override def countByCategory(categoryQuery: CategoryQuery): Int = {
    database.withConnection(implicit conn =>
      SQL(countByCategorySql).on('period -> categoryQuery.period, 'cateCode -> categoryQuery.cateCode).as(SqlParser.int("num").single)
    )
  }

}
