package controllers

import play.api.mvc.Controller
import scala.sys.process._

/**
  * Created by zodiake on 16-11-4.
  */
class PythonController extends Controller {
  def python = UserAuthAction {
    "ls -l /home/zodiake/".!
    Ok
  }
}
