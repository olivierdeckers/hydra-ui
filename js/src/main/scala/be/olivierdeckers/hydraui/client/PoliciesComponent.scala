package be.olivierdeckers.hydraui.client

import be.olivierdeckers.hydraui.{Api, Policy}
import com.thoughtworks.binding.Binding.{Constants, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import autowire._


object PoliciesComponent extends MainContainer {

  val policies: Vars[Policy] = Vars()

  val client = Client[Api]

  @dom
  def multilineText(lines: Seq[String]): Binding[Node] = {
    <ul>
      { for (line <- Constants(lines: _*)) yield {
        <li>
          {line}
        </li>
      }}
    </ul>
  }

  @dom
  def content: Binding[Node] = {
    client.getPolicies().call().map(policyList => {
      policies.value.clear()
      policies.value ++= policyList
    }).failed.foreach(error => MaterializeCSS.toast(s"Error while fetching clients: $error"))

    {
      <table class="highlight">
        <thead>
          <tr>
            <th>Description</th>
            <th>Resources</th>
            <th>Actions</th>
            <th>Subjects</th>
          </tr>
        </thead>
        <tbody>
          {for (policy <- policies) yield {
          <tr>
            <td>
              {policy.description}
            </td>
            <td>
              {multilineText(policy.resources).bind}
            </td>
            <td>
              {multilineText(policy.actions).bind}
            </td>
            <td>
              {multilineText(policy.subjects).bind}
            </td>
          </tr>
        }}
        </tbody>
      </table>
    }
  }

}


