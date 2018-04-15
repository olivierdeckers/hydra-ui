package be.olivierdeckers.hydraui.client

import autowire._
import be.olivierdeckers.hydraui.Api
import be.olivierdeckers.hydraui.client.ClientList.clients
import com.thoughtworks.binding.Binding.{BindingSeq, Var}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Event
import org.scalajs.dom.raw.Node

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

@JSExportTopLevel("Main")
object Main {

  val route: Var[Binding[Node]] = Var.apply[Binding[Node]](ClientList.content)

  @dom
  def root(): Binding[BindingSeq[Node]] = {
    <nav>
      <div class="nav-wrapper">
        <a href="#" class="brand-logo">Hydra UI</a>
        <ul id="nav-mobile" class="right hide-on-med-and-down">
          <li><a href="#" onclick={_: Event => route.value = ClientList.content }>Clients</a></li>
          <li><a href="#" onclick={_: Event => route.value = Policies.content }>Policies</a></li>
          <li><a href="#" onclick={_: Event => route.value = Policies2.content }>test</a></li>
        </ul>
      </div>
    </nav>
    <div>
      { route.bind.bind }
    </div>
  }

  @JSExport
  def main(): Unit = {
    dom.render(org.scalajs.dom.document.body, root())
    Client[Api].getClients().call().map(clients.value ++= _.values)
  }

}
