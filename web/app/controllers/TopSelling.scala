package controllers


import com.google.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._


/**
  * Created by zodiake on 16-10-15.
  */
class TopSelling @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def main = Action{implicit request=>
    Ok(views.html.topSelling("top"))
  }
}

