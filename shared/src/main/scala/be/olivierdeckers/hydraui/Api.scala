package be.olivierdeckers.hydraui

import scala.concurrent.Future

trait Api {
  def getClients(): Future[Either[String,Map[String, Client]]]

  def getPolicies(): Future[Either[String,Seq[Policy]]]
}
