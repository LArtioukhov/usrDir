package itc.usrDir.config.security

case class AppRole(roleName: String, description: String, securityGroups: Set[String])
