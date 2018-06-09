package be.olivierdeckers.hydraui.client

import be.olivierdeckers.hydraui.{ Client => HydraClient }
import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node

object ClientListComponent extends MainContainer {

  val clients: Vars[HydraClient] = Vars.apply[HydraClient]()

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
