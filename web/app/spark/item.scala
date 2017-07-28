package spark

import scala.language.dynamics

class item extends java.io.Serializable with Dynamic {

  var map = Map.empty[String, String]

  def selectDynamic(name: String) =
    map get name getOrElse sys.error("method not found")

  def updateDynamic(name: String)(value: String) {
    map += name -> value
  }

  //def applyDynamicNamed(name: String)(args: (String, Any)*) =

}
