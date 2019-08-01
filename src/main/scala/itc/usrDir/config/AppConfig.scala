package itc.usrDir.config

import itc.usrDir.config.security._

case class AppConfig(appName: String, appRoles: Set[AppRole], securityGroups: Set[SecurityGroup])
