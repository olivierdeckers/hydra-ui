package be.olivierdeckers.hydraui

import scala.concurrent.Future

trait Api {
  def getClients(): Future[Map[String, Client]]

  def getPolicies(): Future[Seq[Policy]]
}
