package be.olivierdeckers.hydraui

import cats.data.Validated
import cats.data.Validated.Valid
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.{Empty, NonEmpty}
import eu.timepit.refined.boolean.Or
import shapeless.Witness
import eu.timepit.refined.string._
import io.circe.{Decoder, DecodingFailure, Encoder}
import io.circe.syntax._
import ujson.Js
import upickle.default.{ReadWriter, Reader, Writer, macroRW}

object RefinedReadWriters {
  implicit def refinedReader[T,C](implicit reader: Reader[T]): Reader[Refined[T, C]] =
    reader.map[Refined[T,C]](Refined.unsafeApply)

  implicit def refinedWriter[T,C](implicit writer: Writer[T]): Writer[Refined[T,C]] = writer.comap(_.value)
}

case class HydraTokenResponse(access_token: String, expires_in: Int)
object HydraTokenResponse {
  implicit def rw: ReadWriter[HydraTokenResponse] = macroRW
}

sealed abstract class ResponseType(val value: String)
object ResponseType {
  case object Code extends ResponseType("code")
  case object Token extends ResponseType("token")
  case object IdToken extends ResponseType("id_token")

  implicit def rw: ReadWriter[ResponseType] = macroRW
  def all: Seq[ResponseType] = Seq(Code, Token, IdToken)
  def fromString(str: String): Option[ResponseType] = all.find(_.value == str)

  implicit val encodeResponseType: Encoder[ResponseType] = Encoder.instance(_.value.asJson)

  implicit val decodeResponseType: Decoder[ResponseType] =
    Decoder.instance(cursor => cursor.value.asString.flatMap(fromString).toRight(DecodingFailure(s"Unknown ResponseType ${cursor.value}", cursor.history)))

}

sealed abstract class GrantType(val value: String)
object GrantType {
  case object ClientCredentials extends GrantType("client_credentials")
  case object AuthorizationCode extends GrantType("authorization_code")
  case object Implicit extends GrantType("implicit")
  case object RefreshToken extends GrantType("refresh_token")
  case object Password extends GrantType("password")

  implicit val rw: ReadWriter[GrantType] = ReadWriter.merge(
    macroRW[ClientCredentials.type], macroRW[AuthorizationCode.type], macroRW[Implicit.type], macroRW[RefreshToken.type], macroRW[Password.type]
  )
  def all: Seq[GrantType] = Seq(ClientCredentials, AuthorizationCode, Implicit, RefreshToken, Password)
  def fromString(str: String): Option[GrantType] = all.find(_.value == str)

  implicit val encodeResponseType: Encoder[GrantType] = Encoder.instance(_.value.asJson)

  implicit val decodeResponseType: Decoder[GrantType] =
    Decoder.instance(cursor => cursor.value.asString.flatMap(fromString).toRight(DecodingFailure(s"Unknown GrantType ${cursor.value}", cursor.history)))
}

case class Client(
  id: String Refined NonEmpty,
  client_name: String Refined NonEmpty,
  client_uri: String Refined Client.URI,
  owner: String,
  redirect_uris: Seq[String Refined Client.URI],
  response_types: Seq[ResponseType] Refined NonEmpty,
  grant_types: Seq[GrantType] Refined NonEmpty,
  scope: String,
  public: Boolean
)

object Client {
  // We are not using Uri from refined, since this depends on java.net.URL which is not available on scalajs
  type URI = Empty Or MatchesRegex[W.`"^https?://.+(:[0-9]+)?.*$"`.T]

  import RefinedReadWriters._
  implicit def rw: ReadWriter[Client] = macroRW

  def validate(id: String, client_name: String, client_uri: String, owner: String, redirect_uris: Seq[String], response_types: Seq[ResponseType], grant_types: Seq[GrantType], scope: String, public: Boolean): Validated[String, Client] = {
    //TODO include the field name in the error
    import cats.implicits._
    import cats.data._
    (
      refineV[NonEmpty](id).toValidated,
      refineV[NonEmpty](client_name).toValidated,
      refineV[URI](client_uri).toValidated,
      Valid(owner),
      toTraverseOps(redirect_uris.map(refineV[URI](_)).toList).sequence.toValidated,
      refineV[NonEmpty](response_types).toValidated,
      refineV[NonEmpty](grant_types).toValidated,
      Valid(scope),
      Valid(public)
    ).mapN(Client.apply _)
  }
}

case class Policy(
  id: String,
  description: String,
  subjects: Seq[String],
  effect: String,
  resources: Seq[String],
  actions: Seq[String],
  conditions: Js.Obj
)

object Policy {
  import RefinedReadWriters._
  implicit def rw: ReadWriter[Policy] = macroRW
}