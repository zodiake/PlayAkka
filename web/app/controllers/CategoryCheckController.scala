package controllers

import com.google.inject.Inject
import play.api.data.{Form, Forms}
import play.api.i18n.MessagesApi

/**
  * Created by zodiake on 17-4-20.
  */
object CategoryCheckController {
  val form = Form(
    Forms.tuple(
      "web" -> Forms.text,
      "category" -> Forms.text
    )
  )
}

class CategoryCheckController @Inject()(val messagesApi: MessagesApi) extends BaseController {

  import CategoryCheckController._

  def getQueryForm = UserAuthAction { implicit request =>
    Ok(views.html.checkCategory.list(form))
  }

  def query = UserAuthAction { implicit reuqest =>
    Ok
  }
}
