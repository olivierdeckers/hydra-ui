package be.olivierdeckers.hydraui.client

import scala.scalajs.js
import org.scalajs.dom.window
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("M")
object MaterializeCSSNative extends js.Object {

  def AutoInit(): Unit = js.native

  def updateTextFields(): Unit = js.native

  def toast(args: js.Dictionary[Any]): Unit = js.native

}

object MaterializeCSS {

  def scheduleAutoInit: Int = window.setTimeout(() => MaterializeCSSNative.AutoInit(), 0)
  
  def scheduleUpdateTextFields(): Unit = window.setTimeout(() => MaterializeCSSNative.updateTextFields(), 0)

  def toast(text: String): Unit = MaterializeCSSNative.toast(js.Dictionary("html" -> text))

}