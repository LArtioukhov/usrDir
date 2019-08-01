package itc.usrDir.config

import itc.usrDir.config.security.{ AppRole, SecurityGroup }

case class AppConfig(appName: String, appRoles: Set[AppRole], securityGroups: Set[SecurityGroup])
