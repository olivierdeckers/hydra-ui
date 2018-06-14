package be.olivierdeckers.hydraui.client

import autowire._
import be.olivierdeckers.hydraui.client.components.{InputField, MultiSelectField, SwitchField}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.{MouseEvent, Node, window}
import be.olivierdeckers.hydraui.{Api, GrantType, ResponseType, Client => HydraClient}
import cats.data.Validated.{Invalid, Valid}
import com.thoughtworks.binding.Binding.{SingleMountPoint, Var}
import scalajs.concurrent.JSExecutionContext.Implicits.queue

case class EditClientComponent(id: String) extends MainContainer {

  val client: Var[Option[HydraClient]] = Var(None)

  @dom
  def content: Binding[Node] = {
    Client[Api].getClient(id).call().foreach {
      case Left(error) => Main.route.state.value = Main.Clients
      case Right(c) =>
        client.value = Some(c)
        window.setTimeout(() => MaterializeCSS.updateTextFields(), 0)
    }

    val nameField = new InputField("name", client.bind.map(_.client_name.value))
    val urlField = new InputField("url", client.bind.map(_.client_uri.value))
    val ownerField = new InputField("owner", client.bind.map(_.owner))
    val redirectUriField = new InputField("redirect-uri", client.bind.map(_.redirect_uris.mkString(",")))
    val responseTypesField = new MultiSelectField("response-types", ResponseType.all.map(_.value), client.bind.map(_.response_types.value.map(_.value)).getOrElse(Seq()))
    val grantTypesField = new MultiSelectField("grant-types", GrantType.all.map(_.value), client.bind.map(_.grant_types.value.map(_.value)).getOrElse(Seq()))
    val scopeField = new InputField("scope", client.bind.map(_.scope))
    val publicField = new SwitchField("public", client.bind.map(_.public))

    def onClickSubmit = { evt: MouseEvent =>
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

      import scalajs.concurrent.JSExecutionContext.Implicits.queue
      import autowire._
      updatedClient match {
        case Valid(c) =>
          Client[Api].updateClient(c).call()
            .map {
              case Left(e) => println(e)
              case _ =>
                Main.route.state.value = Main.Clients
            }
        case Invalid(e) => println(e)
      }
    }

    //TODO delete functionality + call
    //TODO cleanup code duplication with createclientcomponent

    {
      <div class="row">
        <div class="col s12 m6 offset-m3">
          <h1 class="header">Create client</h1>
          <form action="#">
            {nameField.render().bind}
            {urlField.render().bind}
            {ownerField.render().bind}
            {redirectUriField.render().bind}
            {responseTypesField.render().bind}
            {grantTypesField.render().bind}
            {scopeField.render().bind}
            {publicField.render().bind}
          </form>

          <button class="btn waves-effect waves-light" type="submit" onclick={onClickSubmit}>Submit
            <i class="material-icons right">send</i>
          </button>

        </div>
      </div>
    }
  }
}
