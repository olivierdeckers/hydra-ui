package be.olivierdeckers.hydraui.server.hydraclient

import java.time.Clock

import be.olivierdeckers.hydraui.HydraTokenResponse
import cats.{Applicative, Monad, MonadError}
import cats.data.{State, StateT}
import cats.effect.IO
import cats.implicits._
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

  def apiCall[A](request: Request[F])(implicit ed: EntityDecoder[F, A]): F[A]

  def securedApiCall[A](request: Request[F])(implicit M: MonadError[F, Throwable], ed: EntityDecoder[F, A]): StateT[F, AccessToken, A] =
    for {
      _ <- StateT.modifyF[F,AccessToken](token => {
        if (token.expiresAt <= clock.millis) {
          M.map(getAccessToken)(r => AccessToken(r.access_token, clock.millis + r.expires_in))
        } else {
          M.pure(token)
        }
      })
      r <- StateT.inspectF((t: AccessToken) => apiCall[A](request.putHeaders(Header("Authorization", s"Bearer ${t.token}")))(ed))
    } yield r

  def getAccessToken: F[HydraTokenResponse]

  val clock: Clock

}

object Http4sHydraClient extends HydraClient[IO] with Http4sClientDsl[IO]  {
  import be.olivierdeckers.hydraui.server.config.HydraClientConfig.config._
  val httpClient: IO[client.Client[IO]] = Http1Client.apply[IO]()
  val clock: Clock = Clock.systemUTC()
  val logger: Logger = LoggerFactory.getLogger(Http4sHydraClient.getClass)

  override def apiCall[A](request: Request[IO])(implicit ed: EntityDecoder[IO, A]): IO[A] =
    httpClient.flatMap { client =>
      client.expect[A](request)
    }

  implicit val hydraTokenResponseDecoder: EntityDecoder[IO, HydraTokenResponse] = jsonOf[IO, HydraTokenResponse]

  override def getAccessToken: IO[HydraTokenResponse] =
    POST(
      baseUri / "oauth2" / "token",
      "grant_type=client_credentials&scope=hydra",
      Header("Authorization", s"Basic $credentials"),
      Header("Content-Type", "application/x-www-form-urlencoded")
    ).flatMap(apiCall[HydraTokenResponse])
}