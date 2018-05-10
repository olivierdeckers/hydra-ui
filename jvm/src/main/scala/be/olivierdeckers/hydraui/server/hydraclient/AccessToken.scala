package be.olivierdeckers.hydraui.server.hydraclient

case class AccessToken(token: String, expiresAt: Long)

object AccessToken {
  def empty: AccessToken = AccessToken("", 0)
}
