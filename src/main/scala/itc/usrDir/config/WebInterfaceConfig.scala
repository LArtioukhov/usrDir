package itc.usrDir.config

import akka.util.Timeout

case class WebInterfaceConfig(host: String, port: Int, timeout: Timeout)
