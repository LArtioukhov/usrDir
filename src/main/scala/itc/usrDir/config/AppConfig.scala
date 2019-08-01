package itc.usrDir.config

case class AppConfig(appName: String, appRoles: Set[AppRole], securityGroups: Set[SecurityGroup])
