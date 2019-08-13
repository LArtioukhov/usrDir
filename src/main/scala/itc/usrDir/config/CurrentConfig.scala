package itc.usrDir.config

case class CurrentConfig(interfacesConfig: InterfacesConfig, securityConfig: Set[AppConfig], storeConfig: StoreConfig)
