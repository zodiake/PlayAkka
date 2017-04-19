package controllers


import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

/**
  * Created by zodiake on 16-10-18.
  */
class UserRequest[A](val name: String, val request: Request[A]) extends WrappedRequest[A](request) {
}

object UserAuthAction extends ActionBuilder[Request] {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    request.session.get("app.name").map(i => block(request)).getOrElse(Future.successful(Redirect("/").flashing(("app.login", "please logging"))))
  }
}

object UserAuthActionWithUserInfo extends ActionBuilder[UserRequest] {
  override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {
    request.session.get("app.name").map(i => {
      block(new UserRequest[A](i, request))
    }).getOrElse(Future.successful(Redirect("/").flashing(("app.login", "please logging"))))
  }
}

