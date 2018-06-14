package be.olivierdeckers.hydraui.client.components

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node
import org.scalajs.dom.html.Input

class InputField(name: String, initialValue: Option[String] = None) {

  var input: Input = _

  @dom
  def render(): Binding[Node] = {
    input = <input type="text" id={name} value={initialValue.getOrElse(null)} />

    {
      <div class="input-field">
        {input}<label for={name}>
        {name.capitalize.replace('-', ' ')}
      </label>
      </div>
    }
  }

  def value: String = input.value
}
