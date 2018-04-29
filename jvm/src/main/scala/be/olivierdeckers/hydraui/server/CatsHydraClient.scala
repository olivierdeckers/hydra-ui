package be.olivierdeckers.hydraui.server

import java.time.Clock

import be.olivierdeckers.hydraui.{Client, HydraTokenResponse, Policy}
import cats.data.StateT
import cats.effect.IO
import fs2.Stream
import io.circe.generic.auto._
import org.http4s.Http4s._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import org.http4s.{EntityDecoder, Header, Request, _}
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.loadConfigOrThrow

object CatsHydraClient {

  case class HydraClientConfig(credentials: String)
  val config: HydraClientConfig = loadConfigOrThrow[HydraClientConfig]("hydra.client")

  val httpClient: Stream[IO, client.Client[IO]] = Http1Client.stream[IO]()
  val clock: Clock = Clock.systemUTC()
  val logger: Logger = LoggerFactory.getLogger(CatsHydraClient.getClass)

  case class AccessToken(token: String, expiresAt: Long)
  object AccessToken {
    def empty: AccessToken = AccessToken("", 0)
  }

  type ApiRequest = IO[Request[IO]]

  def securedApiCall[A](request: ApiRequest)(implicit ed: EntityDecoder[IO, A]): StateT[IO, AccessToken, Either[Throwable, A]] = StateT { (token: AccessToken) =>
    def performCallWithToken(token: AccessToken) =
      httpClient.flatMap { client =>
        val authorizedRequest = request
          .putHeaders(Header("Authorization", s"Bearer ${token.token}"))
        val result = client.expect[A](authorizedRequest)
        val attempt = result.attempt.map(e => {
          e.left.foreach(ex => logger.error(s"Error while calling hydra API with request $request: $ex"))
          e
        })
        Stream.eval(attempt)
      }.compile.last.map(_.get)

    if (token.expiresAt <= clock.millis) {
      for {
        newToken <- getAccessToken.compile.last.map(_.get).map(r => AccessToken(r.access_token, clock.millis + r.expires_in))
        result <- performCallWithToken(newToken)
      } yield (newToken, result)
    } else {
      performCallWithToken(token).map(token -> _)
    }
  }

  implicit val hydraTokenResponseDecoder: EntityDecoder[IO, HydraTokenResponse] = jsonOf[IO, HydraTokenResponse]
  implicit val clientMapDecoder: EntityDecoder[IO, Map[String, Client]] = jsonOf[IO, Map[String, Client]]
  implicit val policyDecode: EntityDecoder[IO, Seq[Policy]] = jsonOf[IO, Seq[Policy]]

  val baseUrl: Uri = uri("https://oauth.staging1.romcore.cloud")

  def getAccessToken: Stream[IO, HydraTokenResponse] = httpClient.flatMap { client =>
    val request = POST(
      baseUrl / "oauth2" / "token",
      "grant_type=client_credentials&scope=hydra",
      Header("Authorization", s"Basic ${config.credentials}"),
      Header("Content-Type", "application/x-www-form-urlencoded")
    )

    Stream.eval(client.expect[HydraTokenResponse](request))
  }

  def getClients(): StateT[IO, AccessToken, Either[Throwable, Map[String, Client]]] =
    securedApiCall(
      GET(baseUrl / "clients")
    )

  def getPolicies(): StateT[IO, AccessToken, Either[Throwable, Seq[Policy]]] =
    securedApiCall (
      GET(baseUrl / "policies")
    )

  def main(args: Array[String]): Unit = {
    val result = getClients().run(AccessToken.empty).unsafeRunSync()
    //    val result = getAccessToken.compile.last.unsafeRunSync()
    println(result)
  }

}
