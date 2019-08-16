package itc.usrDir

import akka.actor._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern._
import akka.util.Timeout
import itc.usrDir.commands._
import itc.usrDir.config.CurrentConfig
import itc.usrDir.data._

trait WebServiceRoutes extends JsonSupport {

  def userCacheProcessor: ActorRef
  def getCurrentConfig: CurrentConfig

  implicit lazy val timeout: Timeout =
    getCurrentConfig.interfacesConfig.timeout

  def contextRoute(innerRoute: Route): Route = pathPrefix(getCurrentConfig.systemName) {
    pathPrefix(getCurrentConfig.version) {
      innerRoute
    }
  }

  def statusRoute: Route = path("status") {
    get {
      complete(getCurrentConfig)
    }
  }

  def appRoute(appName: String): Route = {
    pathPrefix(appName) {
      pathEnd {
        get {
          complete(getCurrentConfig.securityConfig.find(_.appName == appName))
        }
      } ~ pathPrefix("user") {
        pathPrefix(Segment) { uId =>
          pathPrefix("key") {
            path(Segment) { keyName =>
              get {
                complete {
                  (userCacheProcessor ? CheckKey(uId, appName, keyName)).mapTo[UserKeyPresent]
                }
              }
            }
          } ~ pathEnd {
            put {
              entity(as[SetRoles]) { setRoles =>
                if (setRoles.appName != appName)
                  reject(ValidationRejection("Incorrect appName"))
                else if (setRoles.uId != uId)
                  reject(ValidationRejection("Incorrect user Id"))
                else if (!setRoles.roles.forall { role =>
                  getCurrentConfig.securityConfig
                    .filter(_.appName == appName)
                    .flatMap(_.appRoles)
                    .map(_.roleName)
                    .contains(role)
                })
                  reject(ValidationRejection("Incorrect roles set"))
                else
                  complete {
                    (userCacheProcessor ? setRoles).mapTo[User]
                  }
              }
            } ~ get {
              complete {
                (userCacheProcessor ? GetUser(uId, appName)).mapTo[User]
              }
            }
          }
        }
      }
    }
  }

  def generateRoute(): Route = {

    val appsPath = getCurrentConfig.securityConfig.map(appConfig => appRoute(appConfig.appName)).toSeq

    contextRoute {
      statusRoute ~ pathPrefix("app") {
        concat(appsPath: _*)
      }
    }
  }

}
