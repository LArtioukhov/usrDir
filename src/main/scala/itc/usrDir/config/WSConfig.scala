package itc.usrDir.config

import akka.util.Timeout
import com.typesafe.config.Config
import itc.globals.exceptions.ReadConfigException
import itc.usrDir.config.security.{AppRole, SecurityGroup, SecurityKey, SimpleSecurityKey}

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

trait WSConfig {

  val serviceName = "usrCatalog"

  def rawConfig: Config

  lazy val currentConfig = CurrentConfig(interfacesConfig, applications, storage)

  def interfacesConfig: InterfacesConfig = {
    val iConfig = rawConfig.getConfig("interfaces")
    InterfacesConfig(iConfig.getString("host"), iConfig.getInt("webPort"), iConfig.getInt("grpcPort"), Timeout.create(iConfig.getDuration("timeout")))
  }

  def applications: Set[AppConfig] = {
    rawConfig.getStringList("applications").asScala.toSet.map { app: String =>
      {
        val securityGroups: mutable.Set[SecurityGroup] = mutable.Set.empty
        val rawAppConfig = rawConfig.getConfig(app)
        val roles = rawAppConfig.getStringList("roles").asScala.toSet
        val appRoles: Set[AppRole] = roles.map { role =>
          val rawRoleConfig = rawAppConfig.getConfig(role)
          val description =
            if (rawRoleConfig.hasPath("description")) rawRoleConfig.getString("description")
            else role
          val sGroups = {
            rawRoleConfig
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
          AppRole(role, description, sGroups.map(_.groupName).toSet)
        }
        AppConfig(app, appRoles, securityGroups.toSet)
      }
    }
  }

  def storage: StoreConfig = {
    val rawStorageConfig = rawConfig.getConfig("storage")
    rawStorageConfig.getString("type") match {
      case "file" => FileStorageConfig(rawStorageConfig.getString("path"))
      case other => throw ReadConfigException(s"Unexpected storage type - $other")
    }
  }

}
