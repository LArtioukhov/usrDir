package itc.usrDir

import akka.actor._
import akka.pattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.util.Timeout
import itc.usrDir.config.CurrentConfig
import its.usrDir.commands.GetList
import its.usrDir.data.UsersList
import scala.concurrent.duration._

trait WebServiceRoutes extends JsonSupport {

  def userCacheProcessor: ActorRef
  def getCurrentConfig: CurrentConfig

  implicit lazy val timeout: Timeout = getCurrentConfig.webInterfaceConfig.timeout

  def generateRoute(): Route = {
    pathPrefix("usrDir") {
      pathPrefix("v01") {
        path("status") {
          get {
            complete(getCurrentConfig)
          }
        } ~ path("users") {
          get {
            complete {
              (userCacheProcessor ? GetList()).mapTo[UsersList]
            }
          }
        } ~ pathPrefix("appSettings") {
          path(Segment) { appName =>
            get {
              complete(getCurrentConfig.securityConfig.find(_.appName == appName))
            }
          }
        }
      }
    }
  }
}
