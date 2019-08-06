package itc.usrDir

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.{Config, ConfigFactory}
import itc.usrDir.config.{CurrentConfig, WSConfig}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class WebServiceRoutesTest extends FlatSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with WebServiceRoutes with WSConfig {

  override def rawConfig: Config = ConfigFactory.load().getConfig("usrCatalog")

  override def userCacheProcessor: ActorRef = ???

  override def getCurrentConfig: CurrentConfig = currentConfig

  private lazy val route = generateRoute()

  behavior of "WebServiceRoutes"

  it should "return status" in {
    val request = HttpRequest(uri = "/usrDir/v01/status")

    request ~> route ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      entityAs[String] shouldBe currentConfig.toJson.compactPrint
    }
  }

}
