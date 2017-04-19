package models

import views.html

/**
  * Created by zodiake on 16-10-17.
  */
object FieldHelper {

  import views.html.helper.FieldConstructor

  implicit val myFields = FieldConstructor(html.custom.fieldConstructor.f)

  implicit val horizonal = FieldConstructor(html.custom.horizonalConstructor.f)
}
