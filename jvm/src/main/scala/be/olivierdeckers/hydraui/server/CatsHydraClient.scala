package be.olivierdeckers.hydraui.server

import java.time.Clock

import be.olivierdeckers.hydraui.server.hydraclient.AccessToken
import be.olivierdeckers.hydraui.{Client, HydraTokenResponse, Policy}
import cats.data.StateT
import cats.effect.{IO, Sync}
import io.circe.HCursor
import io.circe.generic.auto._
import org.http4s.Http4s._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.dsl.io._
import org.http4s.{EntityDecoder, Header, Request, _}
import org.slf4j.{Logger, LoggerFactory}
import ujson.Js
import ujson.circe.CirceJson

trait HttpClient[F[_]] {

  def apiCall[A](request: F[Request[F]])(implicit ed: EntityDecoder[F,A]): F[Either[Throwable, A]]

  def securedApiCall[A](request: F[Request[F]])(implicit ed: EntityDecoder[F,A]): StateT[F, AccessToken, Either[Throwable, A]]

}

object Http4sHttpClient extends HttpClient[IO] {
  import be.olivierdeckers.hydraui.server.config.HydraClientConfig.config._
  val httpClient: IO[client.Client[IO]] = Http1Client.apply[IO]()
  val clock: Clock = Clock.systemUTC()
  val logger: Logger = LoggerFactory.getLogger(Http4sHttpClient.getClass)

  override def apiCall[A](request: IO[Request[IO]])(implicit ed: EntityDecoder[IO, A]): IO[Either[Throwable, A]] =
    httpClient.flatMap { client =>
      val result = client.expect[A](request)
      result.attempt.map(e => {
        e.left.foreach(ex => logger.error(s"Error while calling hydra API with request $request: $ex"))
        e
      })
    }

  override def securedApiCall[A](request: IO[Request[IO]])(implicit ed: EntityDecoder[IO, A]): StateT[IO, AccessToken, Either[Throwable, A]] =
    StateT { (token: AccessToken) =>
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

  def getAccessToken: IO[Either[Throwable, HydraTokenResponse]] =
    apiCall[HydraTokenResponse](POST(
      baseUri / "oauth2" / "token",
      "grant_type=client_credentials&scope=hydra",
      Header("Authorization", s"Basic $credentials"),
      Header("Content-Type", "application/x-www-form-urlencoded")
    ))
}

class CatsHydraClient[F[_]: Sync](client: HttpClient[F]) extends Http4sClientDsl[F] {

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

object CatsHydraClient {
  def main(args: Array[String]): Unit = {
    val client = new CatsHydraClient(Http4sHttpClient)
    val result = client.getClients.run(AccessToken.empty).unsafeRunSync()
    //    val result = getAccessToken.compile.last.unsafeRunSync()
    println(result)
  }
}
