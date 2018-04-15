package be.olivierdeckers.hydraui.client

import com.thoughtworks.binding.Binding
import org.scalajs.dom.Node

trait MainContainer {
  def content: Binding[Node]
}
