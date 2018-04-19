package be.olivierdeckers.hydraui.server

import java.time.Clock

import be.olivierdeckers.hydraui.{Client, HydraTokenResponse, Policy}
import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import ujson.JsonProcessingException
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class AccessToken(token: String) extends AnyVal

trait WithHydraAccessToken {
  implicit val backend: SttpBackend[Future, Nothing]
  val clientCredentials: String
  val clock: Clock

  val accessToken: (() => Future[Either[String, AccessToken]]) = {
    case class TokenState(expiresAt: Long, token: String)
    var token = Option.empty[TokenState]

    def getAccessToken(): Future[Either[String, HydraTokenResponse]] =
      sttp.post(uri"${HydraClient.baseUrl}/oauth2/token")
        .header("Authorization", s"Basic $clientCredentials")
        .contentType("application/x-www-form-urlencoded")
        .body("grant_type=client_credentials&scope=hydra")
        .send
        .map(_.body.map(upickle.default.read[HydraTokenResponse](_)))

    () =>
      token match {
        case Some(response) if response.expiresAt > clock.millis =>
          Future.successful(Right(AccessToken(response.token)))
        case _ =>
          getAccessToken().map(_.right.map { response =>
            token = Some(TokenState(clock.millis + response.expires_in * 1000 - 1000, response.access_token))
            AccessToken(response.access_token)
          })
      }
  }

  def withAccessToken[R](block: AccessToken => Future[R]): Future[Either[String, R]] =
    accessToken().flatMap(_ match {
      case Right(token) => block(token).map(Right(_))
      case Left(error) => Future.successful(Left(error))
    })
}

class HydraClient() extends WithHydraAccessToken {

  implicit val backend: SttpBackend[Future, Nothing] = AkkaHttpBackend()
  override val clock = Clock.systemUTC()
  override val clientCredentials: String = "cm9tY29yZTo3OTNiOWE1Zjc1ZGU4ZWRmMDljNDNhYWFlMDJkODk1NDMzMGY5YmI1NDgwMWM1ZTc1YmU0ZTVkMTg0NDdiNjZj"

  import HydraClient.baseUrl

  def getClients(): Future[Either[String, Map[String, Client]]] = withAccessToken { token =>
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

  def getPolicies(): Future[Either[String, Seq[Policy]]] = withAccessToken { token =>
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

}

object HydraClient {

  val baseUrl = "https://oauth.staging1.romcore.cloud"

  def main(args: Array[String]): Unit = {
    //    new HydraClient().getAccessToken().foreach(println(_))
    implicit val token = AccessToken("a1VLTY8za43IXRnXK2_0ChPgcLZQRsFg3XLNwUygG0E.LNH8f9qMRlREWeUoPV65MuhcxXMMiDlH837pAkbEmDY")
    new HydraClient().getPolicies()
    //          .recover {
    //            case ex: Throwable => ex.printStackTrace; Map.empty
    //          }
    //          .foreach(_.values.seq.foreach(println(_)))
  }
}