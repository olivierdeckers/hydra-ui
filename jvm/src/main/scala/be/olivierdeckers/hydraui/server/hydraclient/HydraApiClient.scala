package be.olivierdeckers.hydraui.server.hydraclient

import java.util.UUID

import be.olivierdeckers.hydraui.{Client, GrantType, Policy, ResponseType}
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

class HydraApiClient[F[_] : Sync](client: HydraClient[F]) extends Http4sClientDsl[F] {

  import be.olivierdeckers.hydraui.server.config.HydraClientConfig.config._

  implicit val clientMapDecoder: EntityDecoder[F, Map[String, Client]] = jsonOf[F, Map[String, Client]]
  implicit val clientDecoder: EntityDecoder[F, Client] = jsonOf[F, Client]
  implicit val clientEncoder: EntityEncoder[F, Client] = jsonEncoderOf[F, Client]
  implicit val policyDecoder: EntityDecoder[F, Seq[Policy]] = jsonOf[F, Seq[Policy]]
  implicit val objDecoder: io.circe.Decoder[Js.Obj] =
    (c: HCursor) => Right(CirceJson.transform(c.value, upickle.default.readwriter[Js.Obj]))

  def getClients: StateT[F, AccessToken, Either[Throwable, Map[String, Client]]] =
    client.securedApiCall[Map[String, Client]](
      GET.apply(baseUri / "clients")
    )

  def getPolicies: StateT[F, AccessToken, Either[Throwable, Seq[Policy]]] =
    client.securedApiCall[Seq[Policy]](
      GET(baseUri / "policies")
    )

  def createClient(body: Client): StateT[F, AccessToken, Either[Throwable, Client]] =
    client.securedApiCall[Client](
      POST(baseUri / "clients", body)
    )

}

object HydraApiClient {
  def main(args: Array[String]): Unit = {
    import eu.timepit.refined.auto._
    val client = new HydraApiClient(Http4sHydraClient)
    client.createClient(Client(Refined.unsafeApply(UUID.randomUUID.toString), "test", "", "", Seq(), Refined.unsafeApply(Seq(ResponseType.Token)), Refined.unsafeApply(Seq(GrantType.ClientCredentials)), "", true))
      .run(AccessToken.empty).unsafeRunSync()
    val result = client.getClients.run(AccessToken.empty).unsafeRunSync()
    //    val result = getAccessToken.compile.last.unsafeRunSync()
    println(result._2.right.get.mkString("\n"))
  }
}
