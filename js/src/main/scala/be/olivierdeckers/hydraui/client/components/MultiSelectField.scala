package be.olivierdeckers.hydraui.client.components

import java.util.UUID

import be.olivierdeckers.hydraui.client.MaterializeCSS
import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node
import org.scalajs.dom.html.Select
import org.scalajs.dom.window

import scala.scalajs.js
import js.JSConverters._
import scala.scalajs.js.annotation.JSGlobal



class MultiSelectField(name: String, options: Seq[String], initiallySelected: Seq[String] = Seq()) {

  val id: String = UUID.randomUUID().toString
  var select: Select = _

  @dom
  def render(): Binding[Node] = {
    window.setTimeout(() => {
      Helper.setSelectValue(id, initiallySelected.toJSArray)
      MaterializeCSS.AutoInit()
    }, 0)

    @dom
    def select(): Binding[Select] = {
      <select id={id} multiple={true}>
        {for (option <- Vars(options: _*)) yield
          <option value={option}>
        {option}
      </option>}
      </select>
    }
    this.select = select().bind

    {
      <div class="row">
        <div class="input-field col s12">
          {this.select}
          <label>
            {name.capitalize.replace('-', ' ')}
          </label>
        </div>
      </div>
    }
  }

  def value: Seq[String] = select.options.filter(_.selected).map(_.value)
}
