package be.olivierdeckers.hydraui.server.hydraclient

import java.time.{Clock, Instant, ZoneId}

import be.olivierdeckers.hydraui.HydraTokenResponse
import cats.Id
import cats.effect._
import org.http4s.circe._
import org.http4s.{EntityDecoder, Header, Headers, Request, Response}
import utest._

object HydraClientTest extends utest.TestSuite {

  implicit val syncId: Sync[Id] = new Sync[Id]() {
    override def suspend[A](thunk: => Id[A]): Id[A] = thunk

    override def pure[A](x: A): Id[A] = x

    override def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)

    override def tailRecM[A, B](a: A)(f: A => Id[Either[A, B]]): Id[B] = ???

    override def raiseError[A](e: Throwable): Id[A] = ???

    override def handleErrorWith[A](fa: Id[A])(f: Throwable => Id[A]): Id[A] = fa
  }

  val client = new HydraClient[Id] {
    override def apiCall[A](request: Id[Request[Id]])(implicit ed: EntityDecoder[Id, A]): Id[Either[Throwable, A]] = {
      val body = fs2.Stream.fromIterator[Id, Byte]("\"test\"".getBytes.iterator)
      ed.decode(Response(body = body, headers = Headers(Header("Content-Type", "application/json"))), strict = true).value
    }

    override def getAccessToken: Id[Either[Throwable, HydraTokenResponse]] =
      Right(HydraTokenResponse("newToken", 3600))

    override val clock: Clock =
      Clock.fixed(Instant.ofEpochMilli(10), ZoneId.of("UTC"))
  }

  implicit val stringDecoder: EntityDecoder[Id, String] = jsonOf[Id, String]

  override def tests: Tests = Tests {
    "should perform call with valid token" - {
      val (token, result) = client.securedApiCall[String](Request())
        .run(AccessToken("token", 1000))
      assert(token.token == "token")
      assert(result == Right("test"))
    }

    "should refresh invalid token and perform call" - {
      val (token, result) = client.securedApiCall[String](Request())
        .run(AccessToken("token", 9))
      assert(token.token == "newToken")
      assert(result == Right("test"))
    }
  }
}
