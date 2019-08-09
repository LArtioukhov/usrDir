package itc.usrDir

import akka.actor._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern._
import akka.util.Timeout
import itc.usrDir.config.CurrentConfig
import its.usrDir.commands._
import its.usrDir.data._

trait WebServiceRoutes extends JsonSupport {

  def userCacheProcessor: ActorRef
  def getCurrentConfig: CurrentConfig

  implicit lazy val timeout: Timeout =
    getCurrentConfig.webInterfaceConfig.timeout

  def generateRoute(): Route = {

    val appsPath = getCurrentConfig.securityConfig.map(appConfig => appRoute(appConfig.appName)).toSeq

    pathPrefix("usrDir") {
      pathPrefix("v01") {
        path("status") {
          get {
            complete(getCurrentConfig)
          }
        } ~ pathPrefix("app") {
          concat(appsPath: _*)
        }
      }
    }
  }

  def appRoute(appName: String): Route = {
    pathPrefix(appName) {
      pathEnd {
        get {
          complete(getCurrentConfig.securityConfig.find(_.appName == appName))
        }
      } ~ pathPrefix("user") {
        pathPrefix(Segment) { uId ⇒
          pathPrefix("key") {
            path(Segment) { keyName ⇒
              get {
                complete {
                  (userCacheProcessor ? CheckKey(uId, appName, keyName)).mapTo[UserKeyPresent]
                }
              }
            }
          } ~ pathEnd {
            put {
              entity(as[SetRoles]) { setRoles ⇒
                if (setRoles.appName != appName)
                  reject(ValidationRejection("Incorrect appName"))
                else if (setRoles.roles.forall { role =>
                  getCurrentConfig.securityConfig.filter(_.appName == appName).flatMap(_.appRoles).map(_.roleName).contains(role)
                })
                  complete {
                    (userCacheProcessor ? setRoles).mapTo[User]
                  }
                else reject(ValidationRejection("Incorrect roles set"))
              }
            }
          }
        }
      }
    }
  }
}
