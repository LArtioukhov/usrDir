package itc

import scalapb.TypeMapper

package object usrDir {
  type ElementId = String

  implicit val ElementIdMapper: TypeMapper[String, ElementId] = new TypeMapper[String, ElementId] {
    override def toCustom(base: String): ElementId = base
    override def toBase(custom: ElementId): String = custom
  }
}
