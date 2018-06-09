package be.olivierdeckers.hydraui.client

import java.util.UUID

import be.olivierdeckers.hydraui.client.components._
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.{MouseEvent, Node}
import be.olivierdeckers.hydraui.{Client => HydraClient}

object CreateClientComponent extends MainContainer {

  @dom
  def content: Binding[Node] = {
    // To allow materialize css to initialize the multi select boxes
    org.scalajs.dom.window.setTimeout(() => MaterializeCSS.AutoInit(), 0)

    val nameField = new InputField("name")
    val urlField = new InputField("url")
    val ownerField = new InputField("owner")
    val redirectUriField = new InputField("redirect-uri")
    val responseTypesField = new MultiSelectField("response-types", Seq("code", "token", "id_token"))
    val grantTypesField = new MultiSelectField("grant-types", Seq("autorization_code", "implicit", "client_credentials", "refresh_token"))
    val scopeField = new InputField("scope")
    val publicField = new SwitchField("public")

    def onClickSubmit = { evt: MouseEvent =>
      val client = HydraClient(
        UUID.randomUUID().toString,
        nameField.value,
        urlField.value,
        ownerField.value,
        redirectUriField.value.split(','),
        responseTypesField.value,
        grantTypesField.value,
        scopeField.value,
        publicField.value
      )
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
