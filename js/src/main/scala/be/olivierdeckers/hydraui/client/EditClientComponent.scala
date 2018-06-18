package be.olivierdeckers.hydraui.client

import autowire._
import be.olivierdeckers.hydraui.client.components.{ClientFormComponent, InputField, MultiSelectField, SwitchField}
import be.olivierdeckers.hydraui.{Api, GrantType, ResponseType, Client => HydraClient}
import cats.data.Validated.{Invalid, Valid}
import com.thoughtworks.binding.Binding.{Constants, Var}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.{MouseEvent, Node}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

case class EditClientComponent(id: String) extends MainContainer {

  val client: Var[Option[HydraClient]] = Var(None)

  val api = Client[Api]

  @dom
  def content: Binding[Node] = {
    api.getClient(id).call().foreach {
      case Left(error) => Main.route.state.value = Main.Clients
      case Right(c) =>
        client.value = Some(c)
        MaterializeCSS.scheduleUpdateTextFields()
    }

    val nameField = new InputField("name", client.bind.map(_.client_name.value))
    val urlField = new InputField("url", client.bind.map(_.client_uri.value))
    val ownerField = new InputField("owner", client.bind.map(_.owner))
    val redirectUriField = new InputField("redirect-uri", client.bind.map(_.redirect_uris.mkString(",")))
    val responseTypesField = new MultiSelectField("response-types", ResponseType.all.map(_.value), client.bind.map(_.response_types.value.map(_.value)).getOrElse(Seq()))
    val grantTypesField = new MultiSelectField("grant-types", GrantType.all.map(_.value), client.bind.map(_.grant_types.value.map(_.value)).getOrElse(Seq()))
    val scopeField = new InputField("scope", client.bind.map(_.scope))
    val publicField = new SwitchField("public", client.bind.map(_.public))

    def onClickSubmit: () => Unit = { () =>
      val updatedClient = HydraClient.validate(
        id,
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
      updatedClient match {
        case Valid(c) =>
          api.updateClient(c).call()
            .map {
              case Left(e) =>
                MaterializeCSS.toast(e)
              case _ =>
                MaterializeCSS.toast(s"Updated client ${c.client_name}")
                Main.route.state.value = Main.Clients
            }
        case Invalid(e) =>
          MaterializeCSS.toast(e)
      }
    }

    def onClickDelete = { evt: MouseEvent =>
      api.deleteClient(id).call().map {
        case Left(e) =>
          MaterializeCSS.toast(s"Error deleting client: $e")
        case Right(_) =>
          MaterializeCSS.toast(s"Deleted client ${nameField.value}")
          Main.route.state.value = Main.Clients
      }.failed.map(e => MaterializeCSS.toast(s"Error deleting client: $e"))
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

    //TODO delete functionality + call
    //TODO cleanup code duplication with createclientcomponent

    {
      <div class="row">
        <div class="col s12 m6 offset-m3">
          <h1 class="header">Edit client</h1>

          {clientForm.render.bind}

          <button class="btn waves-effect waves-light red" onclick={onClickDelete}>Delete
            <i class="material-icons right">delete</i>
          </button>

        </div>
      </div>
    }
  }
}
