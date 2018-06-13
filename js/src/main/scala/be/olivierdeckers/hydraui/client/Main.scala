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

@JSExportTopLevel("Main")
object Main {

  sealed class Page(val hash: String, val content: MainContainer)

  case object Clients extends Page("#clients", ClientListComponent)

  case object Policies extends Page("#policies", PoliciesComponent)

  case object CreateClient extends Page("#create", CreateClientComponent)

  implicit val routeFormat: Route.Format[Page] = new Route.Format[Page] {
    override def unapply(hashText: String): Option[Page] =
      Seq(Clients, Policies, CreateClient).find(_.hash == window.location.hash)

    override def apply(state: Page): String = state.hash
  }

  val route: Route.Hash[Page] = Route.Hash[Page](Clients)(routeFormat)

  @dom
  def root(): Binding[BindingSeq[Node]] = {
    <nav>
      <div class="nav-wrapper">
        <div class="row">
          <div class="col s12">
            <a href="#" class="brand-logo">Hydra UI</a>
            <ul id="nav-mobile" class="right hide-on-med-and-down">
              <li>
                <a href="#clients">Clients</a>
              </li>
              <li>
                <a href="#policies">Policies</a>
              </li>
              <li>
                <a href="#create">Create client</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </nav>
      <div>
        {route.state.bind.content.content.bind}
      </div>
  }

  @JSExport
  def main(): Unit = {
    route.state.value = routeFormat.unapply(window.location.hash).getOrElse(Clients)
    route.watch()
    dom.render(org.scalajs.dom.document.body, root())

    val client = Client[Api]
    client.getClients().call().map {
      case Right(clientMap) => clients.value ++= clientMap.values
      case Left(error) => println(s"Error while fetching clients: $error")
    }

    client.getPolicies().call().map {
      case Right(policyList) => PoliciesComponent.policies.value ++= policyList
      case Left(error) => println(s"Error while fetching policies: $error")
    }
  }

}
