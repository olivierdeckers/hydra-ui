package be.olivierdeckers.hydraui.server

import be.olivierdeckers.hydraui.{Client, HydraTokenResponse, Policy}
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

  def getPolicies()(implicit token: AuthorizationToken): Future[Seq[Policy]] =
    sttp.get(uri"$baseUrl/policies")
      .header("authorization", s"Bearer ${token.token}")
      .send
      .map(response =>
        read[Seq[Policy]](response.body.getOrElse(throw new RuntimeException(s"Error while fetching results: ${response.body.left.get}")))
      )
      .recover {
        case e: JsonProcessingException =>
          e.printStackTrace()
          Seq()
      }

}

object HydraClient {
  def main(args: Array[String]): Unit = {
    //    new HydraClient().getAccessToken().foreach(println(_))
    implicit val token = AuthorizationToken("a1VLTY8za43IXRnXK2_0ChPgcLZQRsFg3XLNwUygG0E.LNH8f9qMRlREWeUoPV65MuhcxXMMiDlH837pAkbEmDY")
    new HydraClient().getPolicies()
    //          .recover {
    //            case ex: Throwable => ex.printStackTrace; Map.empty
    //          }
    //          .foreach(_.values.seq.foreach(println(_)))
  }
}