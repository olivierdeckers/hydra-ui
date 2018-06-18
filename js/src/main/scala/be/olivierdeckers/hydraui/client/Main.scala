package be.olivierdeckers.hydraui.client

import autowire._
import be.olivierdeckers.hydraui.Api
import be.olivierdeckers.hydraui.client.ClientListComponent.clients
import com.thoughtworks.binding.Binding.{BindingSeq, Var}
import com.thoughtworks.binding.{Binding, Route, dom}
import org.scalajs.dom.raw.{Event, Node}
import org.scalajs.dom.window

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Main")
object Main {

  sealed trait Page {
    def component: MainContainer
  }

  case object Clients extends Page {
    override def component = ClientListComponent
  }

  case object Policies extends Page {
    override def component = PoliciesComponent
  }

  case object CreateClient extends Page {
    override def component = CreateClientComponent
  }

  case class EditClient(id: String) extends Page {
    override def component = EditClientComponent(id)
  }

  implicit val routeFormat: Route.Format[Page] = new Route.Format[Page] {
    override def unapply(hashText: String): Option[Page] = {
      val parts = hashText.drop(1).split("/")
      parts.headOption.map {
        case "clients" => Clients
        case "policies" => Policies
        case "create" => CreateClient
        case "edit" => EditClient(parts(1))
      }
    }

    override def apply(state: Page): String = "#" + (state match {
      case _: Clients.type => "clients"
      case _: Policies.type => "policies"
      case _: CreateClient.type => "create"
      case EditClient(id) => s"edit/$id"
    })
  }

  val route: Route.Hash[Page] = Route.Hash[Page](Clients)(routeFormat)

  @dom
  def root(): Binding[BindingSeq[Node]] = {
    window.setTimeout(() => MaterializeCSSNative.AutoInit(), 0)

    {
      <nav>
        <div class="nav-wrapper">
          <div class="row">
            <div class="col s12">
              <a href="#" class="brand-logo">Hydra UI</a>
              <ul id="nav-mobile" class="right">
                <li>
                  <a href="#clients">Clients</a>
                </li>
                <li>
                  <a href="#policies">Policies</a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </nav>
        <div>
          {route.state.bind.component.content.bind}<div class="fixed-action-btn">
          <a class="btn-floating btn-large red tooltipped" onclick={_: Event => route.state.value = CreateClient} data:data-position="left" data:data-tooltip="Create client">
            <i class="large material-icons">mode_edit</i>
          </a>
          <ul>
            <li>
              <a class="btn-floating red tooltipped" onclick={_: Event => route.state.value = CreateClient} data:data-position="left" data:data-tooltip="Create policy">
                <i class="material-icons">event_note</i>
              </a>
            </li>
          </ul>
        </div>

        </div>
    }
  }

  @JSExport
  def main(): Unit = {
    route.state.value = routeFormat.unapply(window.location.hash).getOrElse(Clients)
    route.watch()
    dom.render(org.scalajs.dom.document.body, root())
  }

}
