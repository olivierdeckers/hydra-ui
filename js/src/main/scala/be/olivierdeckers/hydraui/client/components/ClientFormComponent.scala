package be.olivierdeckers.hydraui.client.components

import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.{Event, Node}

class ClientFormComponent(onSubmit: () => Unit, val fields: BindingSeq[Node]) {
  @dom
  def render: Binding[BindingSeq[Node]] = {
    <form action="#">
      {for (field <- fields) yield field}
    </form>

    <button class="btn waves-effect waves-light" onclick={_: Event => onSubmit()}>Submit
      <i class="material-icons right">send</i>
    </button>
  }
}
