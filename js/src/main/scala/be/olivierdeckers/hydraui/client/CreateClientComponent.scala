package be.olivierdeckers.hydraui.client

import java.util.UUID

import be.olivierdeckers.hydraui.GrantType.AuthorizationCode
import be.olivierdeckers.hydraui.client.components._
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.{MouseEvent, Node, window}
import be.olivierdeckers.hydraui.{Api, GrantType, ResponseType, Client => HydraClient}
import cats.data.Validated.{Invalid, Valid}

object CreateClientComponent extends MainContainer {

  @dom
  def content: Binding[Node] = {
    // To allow materialize css to initialize the multi select boxes
    window.setTimeout(() => MaterializeCSS.AutoInit(), 0)

    val nameField = new InputField("name")
    val urlField = new InputField("url")
    val ownerField = new InputField("owner")
    val redirectUriField = new InputField("redirect-uri")
    val responseTypesField = new MultiSelectField("response-types", ResponseType.all.map(_.value))
    val grantTypesField = new MultiSelectField("grant-types", GrantType.all.map(_.value))
    val scopeField = new InputField("scope")
    val publicField = new SwitchField("public")

    def onClickSubmit = { evt: MouseEvent =>
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

      import scalajs.concurrent.JSExecutionContext.Implicits.queue
      import autowire._
      client match {
        case Valid(c) =>
          Client[Api].createClient(c).call()
            .map {
              case Left(e) => println(e)
              case _ => // do nothing
            }
        case Invalid(e) => println(e)
      }
      println(client)
    }

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
