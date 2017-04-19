package nielsen.actor

import akka.Done
import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import nielsen.actor.XmlFileActor.{DbXml, XmlDeployDone}
import nielsen.actor.XmlFileActor.DbXml.{Fact, Hlevel}

import scala.xml.Elem


/**
  * Created by zodiake on 17-4-14.
  */
object XmlFileActor {
  def props: Props = Props(new XmlFileActor)

  case class DeployMessage(fileName: String)

  case class XmlMessage(fileName: String)

  case class DbXml(tableName: String, dbName: String, lang: String, mbd: String, hLevel: String, period: Int, remoteHost: String, sequence: List[Hlevel], facts: List[Fact]) {

    def toXml: Elem = {
      val s = sequence.map(i => s"'S${i.sequence}'")
      val xml =
        <InfactModelXml xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <LogicalDbName>ecomm</LogicalDbName>
          <TargetType>infact</TargetType>
          <FactTagPrefix>F</FactTagPrefix>
          <OleDbProvider>MSDAORA.1</OleDbProvider>
          <DataSource>ECCH02PR</DataSource>
          <UserID>ecom</UserID>
          <Password>ecom</Password>
          <Schema>ECOM</Schema>
          <TableName>
            {tableName}
          </TableName>
          <DataFilter>
            {scala.xml.PCData(cdata(s))}
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
              <Levels>
                <Level>
                  <LevelName>LEVEL</LevelName>
                  <ColumnName>HLEVEL</ColumnName>
                  <CharacteristicNames>
                    <CharacteristicName>CATEGORY</CharacteristicName>
                    <CharacteristicName>BRAND</CharacteristicName>
                    <CharacteristicName>VARIANT</CharacteristicName>
                    <!--characteristicname dependes on hlevel db_customer_reqeust_v4-->
                  </CharacteristicNames>
                </Level>
              </Levels>
              <Characteristics>
                {sequence.map(i => {
                <Characteristic>
                  <CharacteristicName>
                    {i.code}
                  </CharacteristicName>
                  <ColumnName>
                    {i.sequence}
                  </ColumnName>
                </Characteristic>
              })}
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
            {facts.map(i => {
            <Fact>
              <FactName>
                {i.factName}
              </FactName>
              <ColumnName>
                {i.columnName}
              </ColumnName>
              <Decimals>0</Decimals>
              <Rounding>false</Rounding>
            </Fact>
          })}
          </Facts>
        </InfactModelXml>
      xml
    }

    def cdata(sequences: List[String]): String = {
      val m = mbd.split(",").map(i => s"'${i}'").mkString(",")
      s"db_name='${dbName}' and lang='${lang}' and MBD in (${m}) and hlevel in (${sequences.mkString(",")}) and period>=${period}"
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
      scala.xml.XML.save(path, xml, enc = "GB18030", xmlDecl = true)
  }
}
