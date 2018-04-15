package be.olivierdeckers.hydraui.client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node


object Policies extends MainContainer {

  @dom
  def content: Binding[Node] = {
    <div>test</div>
  }

}

object Policies2 extends MainContainer {

  @dom
  def content: Binding[Node] = {
    <div>test2</div>
  }

}
