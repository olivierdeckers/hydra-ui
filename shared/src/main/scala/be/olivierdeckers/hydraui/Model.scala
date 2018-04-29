package be.olivierdeckers.hydraui

import ujson.Js
import upickle.default.{ReadWriter, macroRW}

case class HydraTokenResponse(access_token: String, expires_in: Int)
object HydraTokenResponse {
  implicit def rw: ReadWriter[HydraTokenResponse] = macroRW
}

case class Client(
  id: String,
  client_name: String,
  client_uri: String,
  owner: String,
  redirect_uris: Option[Seq[String]],
  response_types: Option[Seq[String]],
  grant_types: Seq[String],
  scope: String,
  public: Boolean
)

object Client {
  implicit def rw: ReadWriter[Client] = macroRW
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
  implicit def rw: ReadWriter[Policy] = macroRW
}