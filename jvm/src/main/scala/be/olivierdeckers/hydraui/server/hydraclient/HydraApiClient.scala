package be.olivierdeckers.hydraui.server.hydraclient

import java.util.UUID

import be.olivierdeckers.hydraui.{Client, GrantType, Policy, ResponseType}
import cats.Applicative
import cats.data.StateT
import cats.effect.Sync
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import io.circe.HCursor
import io.circe.generic.auto._
import io.circe.refined._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, EntityEncoder}
import ujson.Js
import ujson.circe.CirceJson

class HydraApiClient[F[_]: Sync](client: HydraClient[F]) extends Http4sClientDsl[F] {


  import be.olivierdeckers.hydraui.server.config.HydraClientConfig.config._

  implicit val clientMapDecoder: EntityDecoder[F, Map[String, Client]] = jsonOf[F, Map[String, Client]]
  implicit val clientDecoder: EntityDecoder[F, Client] = jsonOf[F, Client]
  implicit val clientEncoder: EntityEncoder[F, Client] = jsonEncoderOf[F, Client]
  implicit val policyDecoder: EntityDecoder[F, Seq[Policy]] = jsonOf[F, Seq[Policy]]
  implicit val objDecoder: io.circe.Decoder[Js.Obj] =
    (c: HCursor) => Right(CirceJson.transform(c.value, upickle.default.readwriter[Js.Obj]))

  def getClients()(implicit F: Applicative[F]): StateT[F, AccessToken, Map[String, Client]] =
    StateT.liftF(GET(baseUri / "clients"))
      .flatMap(client.securedApiCall[Map[String, Client]])

  def getPolicies: StateT[F, AccessToken, Seq[Policy]] =
    StateT.liftF(GET(baseUri / "policies"))
      .flatMap(client.securedApiCall[Seq[Policy]])

  def createClient(body: Client): StateT[F, AccessToken, Client] =
    StateT.liftF(POST(baseUri / "clients", body))
      .flatMap(client.securedApiCall[Client])

  def getClient(id: String): StateT[F, AccessToken, Client] =
    StateT.liftF(GET(baseUri / "clients" / id))
      .flatMap(client.securedApiCall[Client])

  def updateClient(c: Client): StateT[F, AccessToken, Client] =
    StateT.liftF(PUT(baseUri / "clients" / c.id.value, c))
      .flatMap(client.securedApiCall[Client])

  def deleteClient(id: String): StateT[F, AccessToken, Unit] =
    StateT.liftF(DELETE(baseUri / "clients" / id))
      .flatMap(client.securedApiCall[Unit])
}

object HydraApiClient {
  def main(args: Array[String]): Unit = {
    import eu.timepit.refined.auto._
    val client = new HydraApiClient(Http4sHydraClient)
    client.createClient(Client(Refined.unsafeApply(UUID.randomUUID.toString), "test", "", "", Seq(), Refined.unsafeApply(Seq(ResponseType.Token)), Refined.unsafeApply(Seq(GrantType.ClientCredentials)), "", true))
      .run(AccessToken.empty).unsafeRunSync()
    val result = client.getClients.run(AccessToken.empty).unsafeRunSync()
    //    val result = getAccessToken.compile.last.unsafeRunSync()
    println(result._2.mkString("\n"))
  }
}
