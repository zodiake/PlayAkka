package models

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.db.Database

/**
  * Created by zodiake on 17-4-21.
  */
case class Top100(itemId: BigDecimal, prodDesc: String, category: String, checkedCategory: Option[String])

case class Top100Update(itemId: BigDecimal, cateCode: String)

@ImplementedBy(classOf[Top100ServiceImpl])
trait Top100Service {
  val tableName = ConfigFactory.load().getString("top100.tableName")

  val findByCategoryAndWebSql = s"select itemid,prod_raw_desc,catcode,checked_category from $tableName where catcode={catCode} and storeCode={storeCode} and checkedby is null and periodcode={period} order by itemid"

  val findByCategoryAndWebSqlAndCheckedSql = s"select itemid,prod_raw_desc,catcode,checked_category from $tableName where catcode={catCode} and storeCode={storeCode} and checkedby is not null and doublecheckedby is null and periodcode={period} order by itemid"

  val updateCategoryById = s"update $tableName set checked_category={category} where itemid={itemId}"

  val parser = get[BigDecimal]("itemid") ~ get[String]("prod_raw_desc") ~ get[String]("catcode") ~ get[Option[String]]("checked_category") map { case itemid ~ prodDesc ~ category ~ checkedCategory => Top100(itemid, prodDesc, category, checkedCategory) }

  def findByCategoryAndWeb(category: String, web: String, period: Int): List[Top100]

  def findByCategoryAndWebAndChecked(category: String, web: String, period: Int): List[Top100]

  def updateCategoryById(seq: Seq[Top100Update], category: String, period: String, duration: Double, name: String): Unit

  def updateCheckedRows(seq: Seq[Top100Update], category: String, period: String, duration: Double, name: String): Unit

  def findAllCategory: List[String]
}

class Top100ServiceImpl @Inject()(val database: Database) extends Top100Service {

  override def findByCategoryAndWeb(category: String, web: String, period: Int): List[Top100] = {
    database.withConnection { implicit conn =>
      SQL(findByCategoryAndWebSql).on('catCode -> category, 'storeCode -> web, 'period -> period).as(parser.*)
    }
  }

  override def findByCategoryAndWebAndChecked(category: String, web: String, period: Int): List[Top100] = {
    database.withConnection { implicit conn =>
      SQL(findByCategoryAndWebSqlAndCheckedSql).on('catCode -> category, 'storeCode -> web, 'period -> period).as(parser.*)
    }
  }

  override def updateCategoryById(seq: Seq[Top100Update], category: String, period: String, duration: Double, name: String): Unit = {
    val rowsNeedUpdateCategory = seq.filter(i => !i.cateCode.isEmpty && i.cateCode != category)
    database.withConnection { implicit conn =>
      rowsNeedUpdateCategory.foreach(s =>
        SQL(updateCategoryById).on('category -> s.cateCode, 'itemId -> s.itemId).executeUpdate()
      )
      val sql = SQL(s"update $tableName set check_period={duration},checked_by={name} where periodcode={period} and catcode={category}")
        .on('period -> period, 'category -> category, 'duration -> duration, 'name -> name)
      sql.executeUpdate()
    }
  }

  override def updateCheckedRows(seq: Seq[Top100Update], category: String, period: String, duration: Double, name: String): Unit = {
    val rowsNeedUpdateCategory = seq.filter(i => !i.cateCode.isEmpty && i.cateCode != category)
    database.withConnection { implicit conn =>
      rowsNeedUpdateCategory.foreach(s =>
        SQL(updateCategoryById).on('category -> s.cateCode, 'itemId -> s.itemId).executeUpdate()
      )
      seq.foreach { s =>
        SQL(s"update $tableName set double_check_period={duration},double_checked_by={name} where periodcode={period} and catcode={category} and ")
          .on('period -> period, 'category -> category, 'duration -> duration, 'name -> name)
          .executeUpdate()
      }
    }
  }

  override def findAllCategory: List[String] = {
    database.withConnection { implicit conn =>
      SQL("select distinct(catcode) as category from test_qirong_category_fix order by category").as(SqlParser.str("category").*)
    }
  }
}
