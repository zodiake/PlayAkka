package models

import java.io.{File, PrintWriter}
import java.util.Calendar

import akka.actor.Actor
import anorm._
import play.api.db.Database

/**
  * Created by zodiake on 16-10-18.
  */
case object write

case class SegConf(a: Long, b: String, c: String, d: String, e: String, f: String, g: String, h: String, i: String, j: String, k: String, l: String) {
  override def toString: String = {
    s"${a}\t${b}\t${c}\t${d}\t${e}\t${f}\t${g}\t${h}\t${i}\t${j}\t${k}\t${l}"
  }
}

class WriteActor(database: Database) extends Actor {

  val dict = "/tmp/segconf"

  override def receive: Receive = {
    case write =>
      val calendar = Calendar.getInstance()
      val month = calendar.get(Calendar.MONTH)
      val year = calendar.get(Calendar.YEAR)
      val d = new File(s"${dict}/${year}${month}.csv")
      if (!d.exists) {
        val file = new PrintWriter(d)
        val parser = SqlParser.long("SEGID") ~ SqlParser.str("CATCODE") ~ SqlParser.str("SEGNO") ~ SqlParser.str("SEGTYPE") ~ SqlParser.str("CSEGMENT") ~ SqlParser.str("SEGNAME") ~ SqlParser.str("SHORTDESC") ~ SqlParser.str("ECCMANU") ~ SqlParser.str("ESEGNAME") ~ SqlParser.str("ECMANU") ~ SqlParser.str("SEGCODE") ~ SqlParser.str("FNO") map {
          case a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l => SegConf(a, b, c, d, e, f, g, h, i, j, k, l)
        }
        database.withConnection(implicit conn => {
          val result = SQL("select * from segconf").fold(file)((write, r) => {
            val c = r.as(parser)
            c.map(i => write.println(i.toString))
            write
          })
        })
        file.close()
      }
  }
}
