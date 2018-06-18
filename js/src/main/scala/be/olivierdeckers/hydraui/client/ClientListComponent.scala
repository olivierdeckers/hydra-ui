package be.olivierdeckers.hydraui.client

import be.olivierdeckers.hydraui.{Api, Client => HydraClient}
import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node
import org.scalajs.dom.raw.Event
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import autowire._

object ClientListComponent extends MainContainer {

  val clients: Vars[HydraClient] = Vars.apply[HydraClient]()

  val client = Client[Api]

  @dom
  def content: Binding[Node] = {
    client.getClients().call().map {
      case Right(clientMap) =>
        clients.value.clear()
        clients.value ++= clientMap.values
      case Left(error) =>
        MaterializeCSS.toast(s"Error while fetching clients: $error")
    }

    {
      <table class="highlight">
        <thead>
          <tr>
            <th>Client name</th>
            <th>Client uri</th>
          </tr>
        </thead>
        <tbody>
          {for (client <- clients) yield {
          <tr onclick={_: Event => Main.route.state.value = Main.EditClient(client.id.value)}>
            <td>
              {client.client_name.value}
            </td>
            <td>
              {client.client_uri.value}
            </td>
          </tr>
        }}
        </tbody>
      </table>
    }
  }

}
