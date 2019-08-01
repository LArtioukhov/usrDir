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

  // TODO: Здеся должен быть не сам обработчик а маршрутизатор. И он один для всего решения
  def userCacheProcessor: ActorRef
  def currentConfig: CurrentConfig

  implicit lazy val timeout: Timeout = currentConfig.webInterfaceConfig.timeout

  def generateRoute(): Route = {
    pathPrefix("usrDir") {
      pathPrefix("v01") {
        path("status") {
          get {
            complete(currentConfig)
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
              complete(currentConfig.securityConfig.find(_.appName == appName))
            }
          }
        }
      }
    }
  }
}
