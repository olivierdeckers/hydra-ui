package be.olivierdeckers.hydraui.server.config

import org.http4s.Uri
import pureconfig.error.FailureReason
import pureconfig.{ConfigReader, loadConfigOrThrow}

case class HydraClientConfig(credentials: String, baseUri: Uri)
object HydraClientConfig {
  implicit val UriReader: ConfigReader[Uri] = ConfigReader.fromNonEmptyString { s =>
    Uri.fromString(s).left.map(failure => new FailureReason {
      override def description: String = s"Invalid uri: ${failure.sanitized}"
    })
  }
  val config: HydraClientConfig = loadConfigOrThrow[HydraClientConfig]("hydra.client")
}
