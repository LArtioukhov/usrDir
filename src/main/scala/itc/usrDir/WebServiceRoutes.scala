package itc.usrDir

import akka.actor._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern._
import akka.util.Timeout
import itc.usrDir.config.CurrentConfig
import its.usrDir.commands.GetList
import its.usrDir.data.UsersList

trait WebServiceRoutes extends JsonSupport {

  def userCacheProcessor: ActorRef
  def getCurrentConfig: CurrentConfig

  implicit lazy val timeout: Timeout =
    getCurrentConfig.webInterfaceConfig.timeout

  def generateRoute(): Route = {
    pathPrefix("usrDir") {
      pathPrefix("v01") {
        path("status") {
          get {
            complete(getCurrentConfig)
          }
        } ~ path("users") {
          get {
            parameters('sP.as[Int] ? 0, 'aP.as[Int] ? 40) { (sP, aP) =>
              complete {
                (userCacheProcessor ? GetList(sP, aP)).mapTo[UsersList]
              }
            }
          }
        } ~ pathPrefix("app") {
          path(Segment) { appName =>
            complete(appName)
          }
        }
      }
    }
  }
}
