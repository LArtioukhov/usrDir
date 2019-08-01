package itc

import its.usrDir.data.{ User, UserShortRepresentation }
import scalapb.TypeMapper

package object usrDir {
  type ElementId = String

  implicit val ElementIdMapper: TypeMapper[String, ElementId] =
    new TypeMapper[String, ElementId] {
      override def toCustom(base: String): ElementId = base
      override def toBase(custom: ElementId): String = custom
    }

  implicit class UserExt(user: User) {
    def toUserShortRepresentation: UserShortRepresentation =
      UserShortRepresentation(
        user.uId,
        user.fN,
        user.sN,
        user.lN,
        user.lgn,
        user.eml)
  }
}
