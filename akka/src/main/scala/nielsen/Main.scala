package nielsen

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import nielsen.actor.XmlFileActor

/**
  * Created by zodiake on 17-4-14.
  */
object Main {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val system = ActorSystem("remoteFileSystem", config)
    system.actorOf(XmlFileActor.props, "remoteFile")
  }
}