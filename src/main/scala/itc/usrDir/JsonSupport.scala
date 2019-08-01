package itc.usrDir

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.util.Timeout
import itc.usrDir.config._
import itc.usrDir.config.security.{
  AppRole,
  SecurityGroup,
  SecurityKey,
  SimpleSecurityKey
}
import its.usrDir.data._
import spray.json._

import scala.concurrent.duration.{ Duration, FiniteDuration }

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

  implicit object timeoutJsonFormat extends JsonFormat[Timeout] {
    override def write(obj: Timeout): JsValue = JsObject(
      "length" -> JsNumber(obj.duration.length),
      "unit" -> JsString(obj.duration.unit.name))

    override def read(json: JsValue): Timeout =
      json.asJsObject.getFields("length", "unit") match {
        case Seq(JsNumber(length), JsString(unit)) =>
          Timeout(FiniteDuration(length.toLong, unit))
        case _ => deserializationError("Timeout expected")
      }
  }

  implicit val webInterfaceConfigJsonFormat = jsonFormat3(WebInterfaceConfig)
  implicit val appRoleJsonFormat = jsonFormat2(AppRole)
  implicit val securityGroupJsonFormat = jsonFormat2(SecurityGroup)
  implicit val appConfigJsonFormat = jsonFormat3(AppConfig)
  implicit val currentConfigJsonFormat = jsonFormat2(CurrentConfig)

  implicit val userRoleJsonFormat = jsonFormat1(UserRole.apply)
  implicit val appRolesJsonFormat = jsonFormat2(AppRoles.apply)
  implicit val userJsonFormat = jsonFormat10(User.apply)
  implicit val userShortRepresentationJsonFormat = jsonFormat6(
    UserShortRepresentation.apply)
  implicit val usersListJsonFormat = jsonFormat1(UsersList.apply)

}
