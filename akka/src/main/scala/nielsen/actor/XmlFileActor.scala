package nielsen.actor

import java.io.{File, PrintWriter}

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import nielsen.actor.XmlFileActor.DbXml
import nielsen.actor.XmlFileActor.DbXml.{Fact, Hlevel}

/**
  * Created by zodiake on 17-4-14.
  */
object XmlFileActor {
  def props: Props = Props(new XmlFileActor)

  case class DeployMessage(fileName: String)

  case class XmlMessage(fileName: String)

  case class DbXml(tableName: String, dbName: String, lang: String, mbd: List[String], period: Int, remoteHost: String, sequence: List[Hlevel], facts: List[Fact], version: String) {

    def toXml: String = {
      val s = sequence.map(i => s"'S${i.sequence}'")
      val s1 = sequence.map(i => {
        s"""<CharacteristicName>${i.code}</CharacteristicName>"""
      }).mkString("")

      val s2 = {
        sequence.map(i => {
          s"""
            <Characteristic>
                <CharacteristicName>${i.code}</CharacteristicName>
                <ColumnName>S${i.sequence}</ColumnName>
            </Characteristic>"""
        })
      }.mkString("")

      val s3 = facts.map(i => {
        s"""
            <Fact>
              <FactName>${i.factName}</FactName>
              <ColumnName>${i.columnName}</ColumnName>
              <Decimals>0</Decimals>
              <Rounding>false</Rounding>
            </Fact>"""
      }).mkString("")

      val xml =
        s"""<?xml version="1.0" encoding="GB18030"?>
        <InfactModelXml xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <LogicalDbName>ecomm</LogicalDbName>
          <TargetType>infact</TargetType>
          <FactTagPrefix>F</FactTagPrefix>
          <OleDbProvider>MSDAORA.1</OleDbProvider>
          <DataSource>ECCH02PR</DataSource>
          <UserID>ecom</UserID>
          <Password>ecom</Password>
          <Schema>ECOM</Schema>
          <TableName>${tableName}</TableName>
          <DataFilter>
            ${scala.xml.PCData(cdata(s))}
          </DataFilter>
          <Dimensions>
            <Dimension>
              <DimensionName>MKT</DimensionName>
              <TagPrefix>HM</TagPrefix>
              <ItemIdColumn>MBD</ItemIdColumn>
              <ItemShortDescColumn>MBD</ItemShortDescColumn>
              <ItemLongDescColumn>MBD</ItemLongDescColumn>
              <ItemFullDescColumn>MBD</ItemFullDescColumn>
            </Dimension>
            <Dimension>
              <DimensionName>PROD</DimensionName>
              <TagPrefix>HP</TagPrefix>
              <ItemIdColumn>PRODUCTID</ItemIdColumn>
              <ItemShortDescColumn>PRODUCT_SDESC</ItemShortDescColumn>
              <ItemLongDescColumn>PRODUCTID</ItemLongDescColumn>
              <ItemFullDescColumn>PRODUCTID</ItemFullDescColumn>
              <LevelsOnTop>true</LevelsOnTop>
              <Levels>
                <Level>
                  <LevelName>LEVEL</LevelName>
                  <ColumnName>HLEVEL</ColumnName>
                  <CharacteristicNames>
                    ${s1}
                  </CharacteristicNames>
                </Level>
              </Levels>
              <Characteristics>
                ${s2}
              </Characteristics>
            </Dimension>
            <Dimension>
              <DimensionName>PER</DimensionName>
              <TagPrefix>Y</TagPrefix>
              <ItemIdColumn>PERIOD</ItemIdColumn>
              <ItemShortDescColumn>PERIOD</ItemShortDescColumn>
              <ItemLongDescColumn>PERIOD</ItemLongDescColumn>
              <ItemFullDescColumn>PERIOD</ItemFullDescColumn>
            </Dimension>
          </Dimensions>
          <Facts>
            ${s3}
          </Facts>
        </InfactModelXml>
        """
      xml
    }

    def cdata(sequences: List[String]): String = {
      mbd match {
        case Nil =>
          s"db_name='${dbName}' and lang='${lang}' and hlevel in (${sequences.mkString(",")}) and period>=${period}"
        case _ =>
          s"db_name='${dbName}' and lang='${lang}' and MBD in (${mbd.map(i => s"'${i}'").mkString(",")}) and hlevel in (${sequences.mkString(",")}) and period>=${period}"
      }
    }
  }

  case object XmlDeployDone

  object DbXml {

    case class Hlevel(code: String, sequence: Int)

    case class Fact(factName: String, columnName: String)

  }

}

class XmlFileActor extends Actor {

  override def receive: Receive = {
    case msg: DbXml =>
      val xml = msg.toXml
      val config = ConfigFactory.load()
      val path = config.getString("path.xml")
      val pw = new PrintWriter(new File("hello.xml"))
      pw.write(xml)
      pw.close
  }
}
