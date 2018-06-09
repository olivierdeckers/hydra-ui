package be.olivierdeckers.hydraui.client.components

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node
import org.scalajs.dom.html.Select

class MultiSelectField(name: String, options: Seq[String]) {

  var select: Select = _

  @dom
  def render(): Binding[Node] = {
    @dom
    def select(): Binding[Select] = {
      <select multiple={true}>
        {for (option <- Vars(options: _*)) yield <option value={option}>
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
