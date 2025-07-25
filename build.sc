import mill._
import mill.scalalib._
import $ivy.`com.lihaoyi::mill-contrib-playlib:0.11.7`, mill.playlib._

object resmgrserver extends PlayModule {
  def scalaVersion = "3.7.1"
  def playVersion = "3.0.8"
  def twirlVersion = "2.0.9"

  object test extends PlayTests
}
