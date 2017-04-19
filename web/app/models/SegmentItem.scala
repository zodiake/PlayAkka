package models

import javax.inject.Inject

import anorm._
import com.google.inject.ImplementedBy
import controllers.{SegmentDescQuery, SegmentNoNameQuery, SegmentQuery}
import play.api.db.Database

/**
  * Created by zodiake on 16-10-20.
  */
case class SegmentItem(id: BigDecimal, attrNo: BigDecimal, attrValue: String, cateCode: String, description: String, segCode: String)

case class AggSegment(attrValue: String, sum: Long)

case class SegmentDesc(rawBrand: String, rawDesc: String, attribute: String, attrValue: String)

@ImplementedBy(classOf[SegmentItemServiceImpl])
trait SegmentItemService {

  def findBySegment(category: SegmentQuery): List[SegmentItem]

  def findNoNameSegment(segmentNoNameQuery: SegmentNoNameQuery): List[AggSegment]

  def findSegmentDesc(segmentDescQuery: SegmentDescQuery): List[SegmentDesc]
}

object SegmentItemService {

  import anorm.SqlParser._

  val pageSize = 20

  val findBySegmentSql =
    """
      select * from (
      select i.itemid,c.attrno,c.attrvalue,ca.catcode,i.prod_desc_raw,d.segcode,rownum r
      from new_item i join coded_trans_catcode ca on ca.itemid=i.itemid
      join coded_trans_item c on c.itemid=i.itemid
      join db_cate_segment d on d.catcode=ca.catcode and d.segno=c.attrno
      where i.periodcode={period} and d.segcode={segcode} and ca.catcode={cateCode} and rownum<{maxSize})
      where r>{minSize}
    """


  val noNameSql =
    """
      SELECT SEGMENT.ATTRVALUE, SUM(SALES.SALES_VALUE) as sm
        FROM NEW_SALES            SALES,
             NEW_ITEM             ITEM,
             CODED_TRANS_ITEM CATCODE,
             CODED_TRANS_ITEM SEGMENT
       WHERE SALES.ITEMID = ITEM.ITEMID
         AND ITEM.ITEMID = CATCODE.ITEMID
         AND CATCODE.ITEMID = SEGMENT.ITEMID
         AND CATCODE.ATTRNO = 20
         AND CATCODE.ATTRVALUE = {attrValue}
         AND SEGMENT.ATTRNO = {attrNo}
         AND ITEM.MARK = {mark}
         AND ITEM.PERIODCODE = {period}
         AND SALES.PERIODCODE = {period}
         AND CATCODE.PERIODCODE ={period}
         AND SEGMENT.PERIODCODE ={period}
      GROUP BY SALES.PERIODCODE, SEGMENT.ATTRVALUE
      order by sm desc
    """


  val segmentDescSql =
    """
       select * from (
      SELECT ITEM.BRAND RAWBRAND,
             ITEM.PROD_DESC_RAW,
             ITEM.ATTRIBUTE,
             SEGMENT.ATTRVALUE SEGMENT,
             rownum r
        FROM NEW_ITEM ITEM,
             CODED_TRANS_ITEM CATCODE,
             CODED_TRANS_ITEM SEGMENT
       WHERE ITEM.ITEMID = CATCODE.ITEMID
         AND CATCODE.ITEMID = SEGMENT.ITEMID
         AND CATCODE.attrno = 20
         AND CATCODE.attrvalue = {catCode}
         AND SEGMENT.attrvalue= {attrValue}
         AND SEGMENT.ATTRNO = {attrNo}
         AND ITEM.MARK ={mark}
         AND ITEM.PERIODCODE={period}
         AND CATCODE.PERIODCODE = {period}
         AND SEGMENT.PERIODCODE = {period}
         and rownum<={maxSize}) where r>{minSize}
    """

  val segmentParser = get[BigDecimal]("itemid") ~ get[BigDecimal]("attrno") ~ str("attrvalue") ~ str("catcode") ~ str("prod_desc_raw") ~ str("segcode") map {
    case a ~ b ~ c ~ d ~ e ~ f => SegmentItem(a, b, c, d, e, f)
  }

  val noNameSqlParser = str("attrvalue") ~ long("sm") map {
    case a ~ b => AggSegment(a, b)
  }

  val descParser = str("rawBrand") ~ str("prod_desc_raw") ~ str("attribute") ~ str("segment") map {
    case a ~ b ~ c ~ d => SegmentDesc(a, b, c, d)
  }
}

class SegmentItemServiceImpl @Inject()(val database: Database) extends SegmentItemService {

  import SegmentItemService._

  override def findBySegment(category: SegmentQuery): List[SegmentItem] = {
    database.withConnection(implicit conn =>
      SQL(findBySegmentSql).on('period -> category.period, 'cateCode -> category.category, 'segcode -> category.segment, 'minSize -> (category.page - 1) * pageSize, 'maxSize -> category.page * pageSize).as(segmentParser.*)
    )
  }

  override def findNoNameSegment(segmentNoNameQuery: SegmentNoNameQuery): List[AggSegment] = {
    database.withConnection { implicit conn =>
      SQL(noNameSql).on('attrValue -> segmentNoNameQuery.attrValue, 'attrNo -> segmentNoNameQuery.attrNo, 'mark -> segmentNoNameQuery.mark, 'period -> segmentNoNameQuery.period).as(noNameSqlParser.*)
    }
  }

  override def findSegmentDesc(segmentDescQuery: SegmentDescQuery): List[SegmentDesc] = {
    database.withConnection { implicit conn =>
      SQL(segmentDescSql).on('catCode -> segmentDescQuery.catCode, 'attrNo -> segmentDescQuery.attrNo, 'attrValue -> segmentDescQuery.attrValue, 'mark -> segmentDescQuery.mark, 'period -> segmentDescQuery.period, 'minSize -> (segmentDescQuery.page - 1) * pageSize, 'maxSize -> segmentDescQuery.page * pageSize).as(descParser.*)
    }
  }
}