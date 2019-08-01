package itc.usrDir

import akka.http.scaladsl.model.{ ContentTypes, HttpRequest, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.{ Config, ConfigFactory }
import itc.usrDir.config.WSConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }

class WebServiceRoutesTest extends FlatSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with WebServiceRoutes with WSConfig {

  override def rawConfig: Config = ConfigFactory.load().getConfig("usrCatalog")

  private lazy val route = generateRoute(currentConfig)

  behavior of "WebServiceRoutes"

  it should "return status" in {
    val request = HttpRequest(uri = "/status")

    request ~> route ~> check {
      status shouldBe StatusCodes.OK

      contentType shouldBe ContentTypes.`application/json`

      entityAs[String] shouldBe "Ok"
    }
  }

}
