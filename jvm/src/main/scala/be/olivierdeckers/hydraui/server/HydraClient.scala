package be.olivierdeckers.hydraui.server

import java.util.UUID

import akka.actor.ActorSystem
import be.olivierdeckers.hydraui.{Client, HydraTokenResponse}
import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import ujson.JsonProcessingException
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class AuthorizationToken(token: String) extends AnyVal

class HydraClient() {

  implicit val backend: SttpBackend[Future, Nothing] = AkkaHttpBackend()
  val baseUrl = "https://oauth.staging1.romcore.cloud"

  def getAccessToken(): Future[Either[String, String]] = {
    sttp.post(uri"$baseUrl/oauth2/token")
      .header("Authorization", "Basic cm9tY29yZTo3OTNiOWE1Zjc1ZGU4ZWRmMDljNDNhYWFlMDJkODk1NDMzMGY5YmI1NDgwMWM1ZTc1YmU0ZTVkMTg0NDdiNjZj")
      .contentType("application/x-www-form-urlencoded")
      .body("grant_type=client_credentials&scope=hydra")
      .send
      .map(_.body.map(upickle.default.read[HydraTokenResponse](_).access_token))
  }

  def getClients()(implicit token: AuthorizationToken): Future[Map[String, Client]] =
    sttp.get(uri"$baseUrl/clients")
    .header("authorization", s"bearer ${token.token}")
    .send
    .map(response =>
      read[Map[String, Client]](
        response.body
          .getOrElse(throw new RuntimeException(s"Error while fetching results: ${response.body.left.get}"))
      )
    ).recover {
      case e: JsonProcessingException =>
        e.printStackTrace()
        Map.empty
    }

}

object HydraClient {
  def main(args: Array[String]): Unit = {
//    new HydraClient().getAccessToken().foreach(println(_))
    implicit val token = AuthorizationToken("c_hwqx6YcoIiwkgAUXrbhMneL3ky_IaOXMWsU4SsoYg.RQv4x7D92bj5UTyr-F_q9AY6gmiYoz9JqK8-HusUXLM")
    new HydraClient().getClients()
      .recover {
        case ex: Throwable => ex.printStackTrace; Map.empty
      }
      .foreach(_.values.seq.foreach(println(_)))
  }
}