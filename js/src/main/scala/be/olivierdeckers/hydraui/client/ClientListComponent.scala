package be.olivierdeckers.hydraui.client

import be.olivierdeckers.hydraui.Client
import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node

object ClientListComponent extends MainContainer {

  val clients: Vars[Client] = Vars.apply[Client]()

  @dom
  def content: Binding[Node] = {
    <table class="highlight">
      <thead>
        <tr>
          <th>Client name</th>
          <th>Client uri</th>
        </tr>
      </thead>
      <tbody>
        {for (client <- clients) yield {
        <tr>
          <td>
            {client.client_name}
          </td>
          <td>
            {client.client_uri}
          </td>
        </tr>
      }}
      </tbody>
    </table>
  }

}
