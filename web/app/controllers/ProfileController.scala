package controllers

import anorm._
import com.google.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.i18n.MessagesApi

/**
  * Created by zodiake on 16-10-17.
  */
case class Password(password: String, repeat: String)

class ProfileController @Inject()(val database: Database, val messagesApi: MessagesApi) extends BaseController {
  val form = Form(
    mapping("password" -> nonEmptyText,
      "repeat" -> nonEmptyText)(Password.apply)(Password.unapply)
      verifying("repeat not match", fields => fields match {
      case password => validate(password.password, password.repeat)
    })
  )

  def validate(password: String, repeat: String) = {
    password == repeat
  }

  def profile = UserAuthAction { implicit request =>
    Ok(views.html.profile(form))
  }

  def changePwd = UserAuthActionWithUserInfo { userRequest =>
    implicit val request = userRequest.request
    val name = userRequest.name
    form.bindFromRequest.fold(error => Ok(views.html.profile(error)), success => {
      database.withConnection(implicit conn => {
        val result = SQL("update qr_user set password={password} where name={name}").on(("password" -> PasswordUtils.getHash(success.password)), ("name" -> name)).executeUpdate()
        if (result == 1)
          Redirect("/profile").flashing(("message", "update success"))
        else
          Ok(views.html.profile(form.withGlobalError("update failed")))
      })
    })
  }
}
