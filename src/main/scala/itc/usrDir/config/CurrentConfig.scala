package itc.usrDir.config

case class CurrentConfig(
  systemName: String,
  version: String,
  interfacesConfig: InterfacesConfig,
  securityConfig: Set[AppConfig],
  storeConfig: StoreConfig)
