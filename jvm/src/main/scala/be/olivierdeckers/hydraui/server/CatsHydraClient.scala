package be.olivierdeckers.hydraui.server

import java.time.Clock

import be.olivierdeckers.hydraui.{Client, HydraTokenResponse, Policy}
import cats.data.{EitherT, StateT}
import cats.effect.IO
import io.circe.HCursor
import io.circe.generic.auto._
import org.http4s.Http4s._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import org.http4s.{EntityDecoder, Header, Request, _}
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.loadConfigOrThrow
import ujson.Js
import ujson.circe.CirceJson

object CatsHydraClient {

  case class HydraClientConfig(credentials: String)

  val config: HydraClientConfig = loadConfigOrThrow[HydraClientConfig]("hydra.client")

  val httpClient: IO[client.Client[IO]] = Http1Client.apply[IO]()
  val clock: Clock = Clock.systemUTC()
  val logger: Logger = LoggerFactory.getLogger(CatsHydraClient.getClass)

  case class AccessToken(token: String, expiresAt: Long)

  object AccessToken {
    def empty: AccessToken = AccessToken("", 0)
  }

  type ApiRequest = IO[Request[IO]]

  def apiCall[A](request: ApiRequest)(implicit ed: EntityDecoder[IO, A]): IO[Either[Throwable, A]] =
    httpClient.flatMap { client =>
      val result = client.expect[A](request)
      result.attempt.map(e => {
        e.left.foreach(ex => logger.error(s"Error while calling hydra API with request $request: $ex"))
        e
      })
    }

  def securedApiCall[A](request: ApiRequest)(implicit ed: EntityDecoder[IO, A]): StateT[IO, AccessToken, Either[Throwable, A]] = StateT { (token: AccessToken) =>
    def performCallWithToken(token: AccessToken) =
      apiCall[A](request.putHeaders(Header("Authorization", s"Bearer ${token.token}")))

    if (token.expiresAt <= clock.millis) {
      for {
        newToken <- getAccessToken.map(_.map(r => AccessToken(r.access_token, clock.millis + r.expires_in)))
        result <- newToken match {
          case Right(t) => performCallWithToken(t)
          case Left(e) => IO.pure(Left(e))
        }
      } yield (newToken.getOrElse(AccessToken.empty), result)
    } else {
      performCallWithToken(token).map(token -> _)
    }
  }

  implicit val hydraTokenResponseDecoder: EntityDecoder[IO, HydraTokenResponse] = jsonOf[IO, HydraTokenResponse]
  implicit val clientMapDecoder: EntityDecoder[IO, Map[String, Client]] = jsonOf[IO, Map[String, Client]]
  implicit val policyDecoder: EntityDecoder[IO, Seq[Policy]] = jsonOf[IO, Seq[Policy]]
  implicit val objDecoder: io.circe.Decoder[Js.Obj] =
    (c: HCursor) => Right(CirceJson.transform(c.value, upickle.default.readwriter[Js.Obj]))

  val baseUrl: Uri = uri("https://oauth.staging1.romcore.cloud")

  def getAccessToken: IO[Either[Throwable, HydraTokenResponse]] =
    apiCall[HydraTokenResponse](POST(
      baseUrl / "oauth2" / "token",
      "grant_type=client_credentials&scope=hydra",
      Header("Authorization", s"Basic ${config.credentials}"),
      Header("Content-Type", "application/x-www-form-urlencoded")
    ))

  def getClients(): StateT[IO, AccessToken, Either[Throwable, Map[String, Client]]] =
    securedApiCall(
      GET(baseUrl / "clients")
    )

  def getPolicies(): StateT[IO, AccessToken, Either[Throwable, Seq[Policy]]] =
    securedApiCall(
      GET(baseUrl / "policies")
    )

  //  def main(args: Array[String]): Unit = {
  //    val result = getClients().run(AccessToken.empty).unsafeRunSync()
  //    //    val result = getAccessToken.compile.last.unsafeRunSync()
  //    println(result)
  //  }

}
