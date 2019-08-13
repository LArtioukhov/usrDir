package itc.usrDir.config

import akka.util.Timeout

case class InterfacesConfig(host: String, webPrt: Int, grpcPort: Int, timeout: Timeout)
