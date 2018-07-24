package be.olivierdeckers.hydraui

import scala.concurrent.Future

trait Api {
  def getClients(): Future[Map[String, Client]]

  def getClient(id: String): Future[Client]

  def getPolicies(): Future[Seq[Policy]]

  def createClient(client: Client): Future[Client]

  def updateClient(client: Client): Future[Client]

  def deleteClient(id: String): Future[Unit]
}
