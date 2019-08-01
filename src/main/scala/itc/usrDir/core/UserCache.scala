package itc.usrDir.core

import akka.actor._
import itc.usrDir._
import its.usrDir.commands.GetList
import its.usrDir.data.{ User, UsersList }

import scala.collection.mutable

class UserCache extends Actor with ActorLogging {

  private val users: mutable.Map[ElementId, User] = mutable.HashMap.empty

  override def receive: Receive = {
    case GetList() => sender ! UsersList(users.values.map(_.toUserShortRepresentation).toSeq)
  }
}

object UserCache {
  def props = Props(new UserCache)
}
