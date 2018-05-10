package be.olivierdeckers.hydraui.server.hydraclient

import java.time.Clock

import be.olivierdeckers.hydraui.HydraTokenResponse
import cats.Monad
import cats.data.StateT
import cats.effect.IO
import cats.syntax.all._
import io.circe.generic.auto._
import org.http4s.Http4s._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.blaze.Http1Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, Header, Request, client}
import org.slf4j.{Logger, LoggerFactory}

trait HydraClient[F[_]] {

  def apiCall[A](request: F[Request[F]])(implicit ed: EntityDecoder[F,A]): F[Either[Throwable, A]]

  def securedApiCall[A](request: F[Request[F]])(implicit ed: EntityDecoder[F,A], a: Monad[F]): StateT[F, AccessToken, Either[Throwable, A]] =
    StateT { (token: AccessToken) =>
      def performCallWithToken(token: AccessToken) =
        apiCall[A](request.putHeaders(Header("Authorization", s"Bearer ${token.token}")))

      if (token.expiresAt <= clock.millis) {
        for {
          newToken <- getAccessToken.map(_.map(r => AccessToken(r.access_token, clock.millis + r.expires_in)))
          result <- newToken match {
            case Right(t) => performCallWithToken(t)
            case Left(e) => a.pure(Left(e))
          }
        } yield (newToken.getOrElse(AccessToken.empty), result)
      } else {
        performCallWithToken(token).map(token -> _)
      }
    }

  def getAccessToken: F[Either[Throwable, HydraTokenResponse]]

  val clock: Clock

}

object Http4sHydraClient extends HydraClient[IO] with Http4sClientDsl[IO]  {
  import be.olivierdeckers.hydraui.server.config.HydraClientConfig.config._
  val httpClient: IO[client.Client[IO]] = Http1Client.apply[IO]()
  val clock: Clock = Clock.systemUTC()
  val logger: Logger = LoggerFactory.getLogger(Http4sHydraClient.getClass)

  override def apiCall[A](request: IO[Request[IO]])(implicit ed: EntityDecoder[IO, A]): IO[Either[Throwable, A]] =
    httpClient.flatMap { client =>
      val result = client.expect[A](request)
      result.attempt.map(e => {
        e.left.foreach(ex => logger.error(s"Error while calling hydra API with request $request: $ex"))
        e
      })
    }

  implicit val hydraTokenResponseDecoder: EntityDecoder[IO, HydraTokenResponse] = jsonOf[IO, HydraTokenResponse]

  override def getAccessToken: IO[Either[Throwable, HydraTokenResponse]] =
    apiCall[HydraTokenResponse](POST(
      baseUri / "oauth2" / "token",
      "grant_type=client_credentials&scope=hydra",
      Header("Authorization", s"Basic $credentials"),
      Header("Content-Type", "application/x-www-form-urlencoded")
    ))
}