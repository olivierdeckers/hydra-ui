package be.olivierdeckers.hydraui.client

import java.util.UUID

import be.olivierdeckers.hydraui.client.components._
import be.olivierdeckers.hydraui.{Api, GrantType, ResponseType, Client => HydraClient}
import cats.data.Validated.{Invalid, Valid}
import com.thoughtworks.binding.Binding.{BindingSeq, Constants}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.Node

object CreateClientComponent extends MainContainer {

  @dom
  def content: Binding[Node] = {
    // To allow materialize css to initialize the multi select boxes
    MaterializeCSS.scheduleAutoInit

    val nameField = new InputField("name")
    val urlField = new InputField("url")
    val ownerField = new InputField("owner")
    val redirectUriField = new InputField("redirect-uri")
    val responseTypesField = new MultiSelectField("response-types", ResponseType.all.map(_.value))
    val grantTypesField = new MultiSelectField("grant-types", GrantType.all.map(_.value))
    val scopeField = new InputField("scope")
    val publicField = new SwitchField("public")

    def onClickSubmit: () => Unit = { () =>
      val client = HydraClient.validate(
        UUID.randomUUID().toString,
        nameField.value,
        urlField.value,
        ownerField.value,
        redirectUriField.value.split(','),
        responseTypesField.value.flatMap(ResponseType.fromString),
        grantTypesField.value.flatMap(GrantType.fromString),
        scopeField.value,
        publicField.value
      )

      import autowire._

      import scalajs.concurrent.JSExecutionContext.Implicits.queue
      client match {
        case Valid(c) =>
          Client[Api].createClient(c).call()
            .map(_ => {
              MaterializeCSS.toast(s"Created client ${c.client_name}")
              Main.route.state.value = Main.Clients
            }).failed.foreach(e => MaterializeCSS.toast(e.getMessage))
        case Invalid(e) =>
          MaterializeCSS.toast(e)
      }
    }

    val clientForm = new ClientFormComponent(
      onClickSubmit,
      Constants(
        nameField.render().bind,
        urlField.render().bind,
        ownerField.render().bind,
        redirectUriField.render().bind,
        responseTypesField.render().bind,
        grantTypesField.render().bind,
        scopeField.render().bind,
        publicField.render().bind
      )
    )

    {
      <div class="row">
        <div class="col s12 m6 offset-m3">
          <h1 class="header">Create client</h1>

          {clientForm.render.bind}

        </div>
      </div>
    }
  }


}
