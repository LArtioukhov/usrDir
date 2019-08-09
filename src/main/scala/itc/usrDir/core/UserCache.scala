package itc.usrDir.core

import akka.actor._
import itc.usrDir._
import itc.usrDir.config.{AppConfig, CurrentConfig}
import its.usrDir.commands._
import its.usrDir.data._

import scala.collection.mutable

class UserCache(conf: CurrentConfig) extends Actor with ActorLogging {

  private val users: mutable.SortedMap[ElementId, User] =
    mutable.SortedMap.empty

  override def receive: Receive = {
    case SetRoles(uId, appName, roles) ⇒

      if (users.contains(uId)) {
        val currentUser = users(uId)
        val newAppRoles = AppRoles(appName, roles)
        val updatedUser = currentUser.withAppRoles(currentUser.appRoles.filter(_.appName != appName) + newAppRoles)
        users(uId) = updatedUser
        sender ! updatedUser
      } else {
        val newUser = User(uId, Set(AppRoles(appName, roles)))
        users(uId) = newUser
        sender ! newUser
      }

    case CheckKey(uId, appName, key) ⇒

      if (users.contains(uId)) {

        val userRoles: Set[String] =
          users(uId).appRoles
            .filter(_.appName == appName)
            .flatMap(_.roles)

        val appConfig: Set[AppConfig] =
          conf.securityConfig
            .filter(_.appName == appName)

        val appGroups: Set[String] =
          appConfig
            .flatMap(_.appRoles)
            .filter(role ⇒ userRoles.contains(role.roleName))
            .flatMap(_.securityGroups)

        val userKeys: Set[String] =
          appConfig
            .flatMap(_.securityGroups)
            .filter(group ⇒ appGroups.contains(group.groupName))
            .flatMap(_.keys)
            .map(_.name)

        sender ! UserKeyPresent(userKeys.contains(key))
      } else sender ! UserKeyPresent()

  }
}

object UserCache {
  def props(conf: CurrentConfig) = Props(new UserCache(conf))
}
