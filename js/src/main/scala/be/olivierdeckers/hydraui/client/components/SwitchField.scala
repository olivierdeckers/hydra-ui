package be.olivierdeckers.hydraui.client.components

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node
import org.scalajs.dom.html.Input

class SwitchField(name: String, initialValue: Option[Boolean] = None) {

  var checkbox: Input = _

  @dom
  def render(): Binding[Node] = {
    checkbox = <input type="checkbox" checked={initialValue.getOrElse(false)} />

    {
      <div class="switch">
        <label>
          {name.capitalize}{checkbox}<span class="lever"></span>
        </label>
      </div>
    }
  }

  def value: Boolean = checkbox.checked
}
