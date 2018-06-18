package be.olivierdeckers.hydraui

import scala.concurrent.Future

trait Api {
  def getClients(): Future[Either[String,Map[String, Client]]]

  def getClient(id: String): Future[Either[String, Client]]

  def getPolicies(): Future[Either[String,Seq[Policy]]]

  def createClient(client: Client): Future[Either[String, Client]]

  def updateClient(client: Client): Future[Either[String, Client]]

  def deleteClient(id: String): Future[Either[String, Unit]]
}
