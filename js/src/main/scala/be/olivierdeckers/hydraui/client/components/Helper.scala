package be.olivierdeckers.hydraui.client.components

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/**
  * This is implemented in index.html, to work around the issue that scalajs
  * doesn't accept multiple values for a select box
  */
@js.native
@JSGlobal("Helper")
object Helper extends js.Object {
  def setSelectValue(id: String, values: js.Array[String]): Unit = js.native
}
