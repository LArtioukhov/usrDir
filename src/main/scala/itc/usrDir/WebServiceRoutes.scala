package itc.usrDir

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import itc.usrDir.config.CurrentConfig

trait WebServiceRoutes extends JsonSupport {
  def generateRoute(currentConfig: CurrentConfig): Route = {

    path("status") {
      get {
        complete(currentConfig)
      }
    }
  }
}
