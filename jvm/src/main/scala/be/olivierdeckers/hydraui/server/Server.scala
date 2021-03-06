package be.olivierdeckers.hydraui.server

import upickle.default._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import be.olivierdeckers.hydraui.server.hydraclient.{AccessToken, Http4sHydraClient, HydraApiClient}
import be.olivierdeckers.hydraui.{Api, Client, Policy}
import cats.effect.IO
import upickle.Js

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Elem

object AutowireServer extends autowire.Server[Js.Value, Reader, Writer] {
  def read[Result: Reader](p: Js.Value) = upickle.default.readJs[Result](p)

  def write[Result: Writer](r: Result) = upickle.default.writeJs(r)
}

object Server extends Api {

//  val hydraClient = new HydraClient()

  def index(): Elem = {
    <html>
      <head>
        <title>Hydra UI</title>
        <script type="text/javascript" src="/client-fastopt.js"></script>
        <script type="text/javascript" src="META-INF/resources/webjars/materialize-css/1.0.0-beta/dist/js/materialize.min.js"></script>
        <link rel="stylesheet" type="text/css" href="META-INF/resources/webjars/materialize-css/1.0.0-beta/dist/css/materialize.min.css"/>
        <link href="//fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
      </head>
      <body>
        <script>
          Main.main();
        </script>
      </body>
    </html>
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val route = {
      get {
        pathSingleSlash {
          complete {
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              index().toString()
            )
          }
        } ~
          getFromResourceDirectory("")
      } ~
        post {
          path("api" / Segments) { s =>
            extract(_.request.entity match {
              case HttpEntity.Strict(nb: ContentType.NonBinary, data) =>
                data.decodeString(nb.charset.value)
            }) { e =>
              complete {
                AutowireServer.route[Api](Server)(
                  autowire.Core.Request(
                    s,
                    upickle.json.read(e).asInstanceOf[Js.Obj].value.toMap
                  )
                ).map(upickle.json.write(_))
              }
            }
          }
        }
    }

    Http().bindAndHandle(route, "0.0.0.0", port = 8080)
    println("Started server on port 8080")
  }

  val client = new HydraApiClient[IO](Http4sHydraClient)
  // TODO testability is not good, since IO is opaque
  //TODO replace state monad with a Ref for handling the token as shared mutable state
  var token: AccessToken = AccessToken.empty
  override def getClients(): Future[Map[String, Client]] =
    client.getClients.run(token).unsafeToFuture().map {
      case (newToken, response) =>
        token = newToken
        response
    }

  override def getPolicies(): Future[Seq[Policy]] =
    client.getPolicies.run(token).unsafeToFuture().map {
      case (newToken, response) =>
        token = newToken
        response
    }

  override def createClient(body: Client): Future[Client] =
    client.createClient(body).run(token).unsafeToFuture().map {
      case (newToken, response) =>
        token = newToken
        response
    }

  override def getClient(id: String): Future[Client] =
    client.getClient(id).run(token).unsafeToFuture().map {
      case (newToken, response) =>
        token = newToken
        response
    }

  override def updateClient(c: Client): Future[Client] =
    client.updateClient(c).run(token).unsafeToFuture.map {
      case (newToken, response) =>
        token = newToken
        response
    }

  override def deleteClient(id: String): Future[Unit] =
    client.deleteClient(id).run(token).unsafeToFuture.map {
      case (newToken, response) =>
        token = newToken
        response
    }
}
