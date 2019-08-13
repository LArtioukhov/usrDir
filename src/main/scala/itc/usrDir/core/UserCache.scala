package itc.usrDir.core

import akka.actor._
import itc.usrDir._
import itc.usrDir.commands._
import itc.usrDir.config.{AppConfig, CurrentConfig}
import itc.usrDir.data._

import scala.collection.mutable

class UserCache(conf: CurrentConfig) extends Actor with ActorLogging {

  private val users: mutable.SortedMap[ElementId, User] =
    mutable.SortedMap.empty

  override def receive: Receive = {

    case GetUser(uId, iAppName) =>
      val result = {
        val u = if (users.contains(uId)) users(uId) else User(uId)
        u.withAppRoles(u.appRoles.filter(_.appName == iAppName))
      }
      sender ! result

    case InsertUser(user) =>
      users(user.uId) = user

    case SetRoles(uId, appName, roles) =>
      val user = if (users.contains(uId)) {
        val currentUser = users(uId)
        val newAppRoles = AppRoles(appName, roles)
        val updatedUser =
          if (roles.nonEmpty) currentUser.withAppRoles(currentUser.appRoles.filter(_.appName != appName) + newAppRoles)
          else currentUser.withAppRoles(currentUser.appRoles.filter(_.appName != appName))

        users(uId) = updatedUser
        updatedUser
      } else {
        val newUser = User(uId, Set(AppRoles(appName, roles)))
        users(uId) = newUser
        newUser
      }
      sender ! user
      context.actorSelection("../UserStorage") ! SaveUser(user)

    case CheckKey(uId, appName, key) =>
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
            .filter(role => userRoles.contains(role.roleName))
            .flatMap(_.securityGroups)

        val userKeys: Set[String] =
          appConfig
            .flatMap(_.securityGroups)
            .filter(group => appGroups.contains(group.groupName))
            .flatMap(_.keys)
            .map(_.name)

        sender ! UserKeyPresent(userKeys.contains(key))
      } else sender ! UserKeyPresent()

  }
}

object UserCache {
  def props(conf: CurrentConfig) = Props(new UserCache(conf))
}
