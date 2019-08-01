package itc.usrDir.config

sealed trait SecurityKey {
  def name: String
  def isSimpleKey: Boolean = true
}

case class SimpleSecurityKey(override val name: String) extends SecurityKey
