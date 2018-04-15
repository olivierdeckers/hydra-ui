package be.olivierdeckers.hydraui.client

import autowire._
import be.olivierdeckers.hydraui.Api
import be.olivierdeckers.hydraui.client.ClientListComponent.clients
import com.thoughtworks.binding.Binding.{BindingSeq, Var}
import com.thoughtworks.binding.{Binding, Route, dom}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.window

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

//TODO source mapping
//TODO auto reload

@JSExportTopLevel("Main")
object Main {

  sealed class Page(val hash: String, val content: MainContainer)
  case object Clients extends Page("#clients", ClientListComponent)
  case object Policies extends Page("#policies", PoliciesComponent)
  case object Test extends Page("#test", TestComponent)

  val currentPage = Var(Clients)

  val route: Route.Hash[Page] = Route.Hash[Page](Clients)(new Route.Format[Page] {
    override def unapply(hashText: String): Option[Page] =
      Seq(Clients, Policies, Test).find(_.hash == window.location.hash)

    override def apply(state: Page): String = state.hash
  })

  @dom
  def root(): Binding[BindingSeq[Node]] = {
    <nav>
      <div class="nav-wrapper">
        <a href="#" class="brand-logo">Hydra UI</a>
        <ul id="nav-mobile" class="right hide-on-med-and-down">
          <li><a href="#clients">Clients</a></li>
          <li><a href="#policies">Policies</a></li>
          <li><a href="#test">test</a></li>
        </ul>
      </div>
    </nav>
    <div>
      { route.state.bind.content.content.bind }
    </div>
  }

  @JSExport
  def main(): Unit = {
    route.watch()
    dom.render(org.scalajs.dom.document.body, root())
    Client[Api].getClients().call().map(clients.value ++= _.values)
  }

}
