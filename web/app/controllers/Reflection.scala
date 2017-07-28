package controllers

import play.api.Play
import play.api.mvc.{BodyParser, Request}

import scala.concurrent.Future
import scala.reflect.macros.blackbox
import scala.reflect.runtime.{universe => ru}

case class LabelText(label: String, text: String)

object Reflection {
  def main(args: Array[String]): Unit = {

    def parser[A]: BodyParser[A] = ???

    def rh[A]: Request[A] = ???


  }
}

