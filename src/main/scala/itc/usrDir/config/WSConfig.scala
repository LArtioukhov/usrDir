package itc.usrDir.config

import scala.jdk.CollectionConverters._
import com.typesafe.config.Config

import scala.language.postfixOps

import scala.collection.mutable

trait WSConfig {

  val serviceName = "usrCatalog"

  def rawConfig: Config

  lazy val currentConfig = CurrentConfig(webInterfaceConfig, applications)

  def webInterfaceConfig: WebInterfaceConfig = {
    val wIConfig = rawConfig.getConfig("webInterface")
    WebInterfaceConfig(wIConfig.getString("host"), wIConfig.getInt("port"))
  }

  def applications: Set[AppConfig] = {
    rawConfig.getStringList("applications").asScala.toSet.map { app: String =>
      {
        val securityGroups: mutable.Set[SecurityGroup] = mutable.Set.empty
        val rawAppConfig = rawConfig.getConfig(app)
        val roles = rawAppConfig.getStringList("roles").asScala.toSet
        val appRoles: Set[AppRole] = roles.map { role =>
          val sGroups = {
            rawAppConfig
              .getConfig(role)
              .getStringList("groups")
              .asScala
              .map { group: String =>
                securityGroups.find(_.groupName == group).getOrElse {
                  val securityKeys: Set[SecurityKey] = rawAppConfig
                    .getStringList("groups." + group + ".keys")
                    .asScala
                    .map(SimpleSecurityKey)
                    .toSet
                  val sGroup = SecurityGroup(group, securityKeys)
                  securityGroups += sGroup
                  sGroup
                }
              }
          }
          AppRole(role, sGroups.map(_.groupName).toSet)
        }
        AppConfig(app, appRoles, securityGroups.toSet)
      }
    }
  }

}
