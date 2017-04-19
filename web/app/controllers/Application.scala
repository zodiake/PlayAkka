package controllers

import anorm.SqlParser._
import anorm._
import com.google.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.Logger

case class User(name: String, password: String)

case class UserAuthParser(name: String, menu_name: String, menu_ref: String)

case class UserMenu(menu_name: String, menu_ref: String)

case class UserAuth(name: String, menus: List[UserMenu])

class Application @Inject()(val database: Database, val messagesApi: MessagesApi) extends Controller with I18nSupport {
  val form = Form(
    mapping("name" -> nonEmptyText, "password" -> nonEmptyText)(User.apply)(User.unapply)
  )

  val parser = str("name") ~ str("password") map {
    case name ~ password => User(name, password)
  }

  val authParser = str("name") ~ str("menu_name") ~ str("menu_reference") map {
    case name ~ menuName ~ menuReference => UserAuthParser(name, menuName, menuReference)
  }

  //get /
  def index = Action { implicit request =>
    Ok(views.html.index(form))
  }

  //post /
  def login = Action { implicit request =>
    form.bindFromRequest.fold(error => BadRequest(views.html.index(error)), s => {
      database.withConnection(implicit conn => {
        val sql = "select * from qr_user join qr_auth_menu on authentication=auth_id join qr_menu on qr_auth_menu.menu_id=qr_menu.menu_id where name={name} and password={password}"
        val result = SQL(sql)
          .on("name" -> s.name, "password" -> PasswordUtils.getHash(s.password)).as(authParser.*)
        if (result.isEmpty) {
          Ok(views.html.index(form.withGlobalError("name or password error")))
        } else {
          val menuAuth = result.foldRight(new UserAuth(s.name, List[UserMenu]()))((a, s) => {
            val m = UserMenu(a.menu_name, a.menu_ref)
            UserAuth(s.name, m :: s.menus)
          })
          val menus=menuAuth.menus.map(i=>s"${i.menu_name}:${i.menu_ref}").mkString(",")
          Logger.debug(menus)
          Redirect("/main").withSession(("app.name", s.name), ("app.id", "1"),("app.menu",menus))
        }
      })
    })
  }

  //get /main
  def main = UserAuthAction { implicit request =>
    Ok(views.html.pain(request))
  }
}