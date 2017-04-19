package controllers

import play.api.mvc.{Action, Controller}

/**
  * Created by zodiake on 17-4-12.
  */
class StaticHostController extends Controller {
  def list = Action { implicit request => Ok(views.html.hosts.list(request)) }
}
