package itc.usrDir

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.util.Timeout
import itc.usrDir.commands._
import itc.usrDir.config._
import itc.usrDir.config.security._
import itc.usrDir.data._
import spray.json._

import scala.concurrent.duration.FiniteDuration

//noinspection TypeAnnotation
trait JsonSupport extends SprayJsonSupport {
  import spray.json.DefaultJsonProtocol._

  // Key
  implicit object securityKeyJson extends JsonFormat[SecurityKey] {

    override def read(json: JsValue): SecurityKey = json match {
      case JsString(n) => SimpleSecurityKey(n)
      case _ => deserializationError("Security key expected")
    }

    override def write(obj: SecurityKey): JsValue = obj match {
      case SimpleSecurityKey(name) => JsString(name)
    }
  }

  // Timeout
  implicit object timeoutJsonFormat extends JsonFormat[Timeout] {
    override def write(obj: Timeout): JsValue =
      JsObject("length" -> JsNumber(obj.duration.length), "unit" -> JsString(obj.duration.unit.name))

    override def read(json: JsValue): Timeout =
      json.asJsObject.getFields("length", "unit") match {
        case Seq(JsNumber(length), JsString(unit)) =>
          Timeout(FiniteDuration(length.toLong, unit))
        case _ => deserializationError("Timeout expected")
      }
  }

  // Configs
  implicit val webInterfaceConfigJsonFormat = jsonFormat4(InterfacesConfig)
  implicit val appRoleJsonFormat = jsonFormat3(AppRole)
  implicit val securityGroupJsonFormat = jsonFormat2(SecurityGroup)
  implicit val appConfigJsonFormat = jsonFormat3(AppConfig)

  implicit object storeConfigJsonFormat extends RootJsonFormat[StoreConfig] {

    override def write(obj: StoreConfig): JsValue = obj match {
      case FileStorageConfig(path) => JsObject("type" -> JsString("file"), "path" -> JsString(path))
    }

    override def read(json: JsValue): StoreConfig = serializationError("Reading storage config is not supported.")
  }

  implicit val currentConfigJsonFormat = jsonFormat5(CurrentConfig)

  // Data
  implicit val appRolesJsonFormat = jsonFormat2(AppRoles.apply)
  implicit val userJsonFormat = jsonFormat2(User.apply)
  implicit val userKeyPresentJsonFormat = jsonFormat1(UserKeyPresent.apply)

  // Commands
  implicit val setRolesJsonFormat = jsonFormat3(SetRoles.apply)
  implicit val checkKeyJsonFormat = jsonFormat3(CheckKey.apply)
}
