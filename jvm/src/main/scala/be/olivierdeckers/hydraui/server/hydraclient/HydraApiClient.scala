package be.olivierdeckers.hydraui.server.hydraclient

import be.olivierdeckers.hydraui.{Client, Policy}
import cats.data.StateT
import cats.effect.Sync
import io.circe.HCursor
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import ujson.Js
import ujson.circe.CirceJson

class HydraApiClient[F[_]: Sync](client: HydraClient[F]) extends Http4sClientDsl[F] {

  import be.olivierdeckers.hydraui.server.config.HydraClientConfig.config._

  implicit val clientMapDecoder: EntityDecoder[F, Map[String, Client]] = jsonOf[F, Map[String, Client]]
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

}

object HydraApiClient {
  def main(args: Array[String]): Unit = {
    val client = new HydraApiClient(Http4sHydraClient)
    val result = client.getClients.run(AccessToken.empty).unsafeRunSync()
    //    val result = getAccessToken.compile.last.unsafeRunSync()
    println(result)
  }
}
