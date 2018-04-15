package be.olivierdeckers.hydraui

import upickle.default.{ReadWriter, macroRW}

case class HydraTokenResponse(access_token: String)
object HydraTokenResponse {
  implicit def rw: ReadWriter[HydraTokenResponse] = macroRW
}

case class Client(
  id: String,
  client_name: String,
  client_uri: String,
  owner: String,
  redirect_uris: Seq[String],
  response_types: Seq[String],
  grant_types: Seq[String],
  scope: String,
  public: Boolean
)
object Client {
  implicit def rw: ReadWriter[Client] = macroRW
}
