package itc.usrDir.config

sealed trait StoreConfig extends Product

case class FileStorageConfig(path: String) extends StoreConfig
