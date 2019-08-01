package itc.usrDir

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import itc.usrDir.config._
import itc.usrDir.config.security.{ AppRole, SecurityGroup, SecurityKey, SimpleSecurityKey }
import its.usrDir.data._
import spray.json._

//noinspection TypeAnnotation
trait JsonSupport extends SprayJsonSupport {
  import spray.json.DefaultJsonProtocol._

  implicit object securityKeyJson extends JsonFormat[SecurityKey] {

    override def read(json: JsValue): SecurityKey = json match {
      case JsString(n) => SimpleSecurityKey(n)
      case _ => deserializationError("Security key expected")
    }

    override def write(obj: SecurityKey): JsValue = obj match {
      case SimpleSecurityKey(name) => JsString(name)
    }
  }

  implicit val webInterfaceConfigJsonFormat = jsonFormat2(WebInterfaceConfig)
  implicit val appRoleJsonFormat = jsonFormat2(AppRole)
  implicit val securityGroupJsonFormat = jsonFormat2(SecurityGroup)
  implicit val appConfigJsonFormat = jsonFormat3(AppConfig)
  implicit val currentConfigJsonFormat = jsonFormat2(CurrentConfig)

  implicit val userRoleJsonFormat = jsonFormat1(UserRole.apply)
  implicit val appRolesJsonFormat = jsonFormat2(AppRoles.apply)
  implicit val userJsonFormat = jsonFormat10(User.apply)

}
