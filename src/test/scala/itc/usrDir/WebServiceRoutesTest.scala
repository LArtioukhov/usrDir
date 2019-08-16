package itc.usrDir

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.{Config, ConfigFactory}
import itc.usrDir.config.{CurrentConfig, WSConfig}
import itc.usrDir.core.UserCache
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import spray.json._

class WebServiceRoutesTest extends {
  override val serviceName: String = "userKeysCatalog"
} with FlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with WebServiceRoutes with WSConfig
  with BeforeAndAfterAll {

  var actorSystem: ActorSystem = _

  override def beforeAll(): Unit = {
    actorSystem = ActorSystem(serviceName)
  }

  override def rawConfig: Config = ConfigFactory.load().getConfig(serviceName)

  override def getCurrentConfig: CurrentConfig = currentConfig

  override def userCacheProcessor: ActorRef = actorSystem.actorOf(UserCache.props(getCurrentConfig), "RootSupervisor")

  private lazy val route = generateRoute()

  behavior of "WebServiceRoutes"

  it should "return status" in {
    val request = HttpRequest(uri = s"/$serviceName/v01/status")

    request ~> route ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      entityAs[String] shouldBe currentConfig.toJson.compactPrint
    }
  }

}
