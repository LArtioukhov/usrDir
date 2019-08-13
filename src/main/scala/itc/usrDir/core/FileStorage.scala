package itc.usrDir.core

import java.io.{File, FileInputStream, FileOutputStream}

import akka.actor._
import itc.globals.exceptions.InternalApiError
import itc.usrDir.commands._
import itc.usrDir.config._
import itc.usrDir.data.User

case class FileStorage(storeConfig: StoreConfig) extends Actor with ActorLogging {

  private val userCache = context.actorSelection("../UserCache")

  private val fileStorageConfig = storeConfig.asInstanceOf[FileStorageConfig]

  override def preStart(): Unit = {
    super.preStart()
    val storageDir = new File(fileStorageConfig.path)
    if (!storageDir.canWrite || !storageDir.isDirectory) {
      log.error("Do not have enough rights in directory {}", fileStorageConfig.path)
      throw InternalApiError(s"Do not have enough rights in directory ${fileStorageConfig.path} ")
    }
  }

  override def receive: Receive = {
    case SaveUser(user) =>
      val datFileName = user.uId + ".dat"
      val bacFileName = user.uId + ".bac"
      val dirName = fileStorageConfig.path + File.separator + user.uId.hashCode.toString.reverse
        .take(2) + File.separator

      val dir = new File(dirName)

      if (!dir.exists()) dir.mkdir()

      val datFile = new File(dir, datFileName)
      val bacFile = new File(dir, bacFileName)

      if (datFile.exists()) {
        if (bacFile.exists()) bacFile.delete()
        datFile.renameTo(bacFile)
      }
      if (user.appRoles.nonEmpty) {
        val out = new FileOutputStream(datFile)
        user.writeTo(out)
        out.close()
      }

    case Load =>
      var _count = 0
      log.info("Loading users from the storage is started")
      val storageRoot = new File(fileStorageConfig.path)
      storageRoot.listFiles(_.isDirectory).toVector.foreach { dir =>
        dir.listFiles(_.getName.endsWith(".dat")).foreach { datFile =>
          val iS = new FileInputStream(datFile)
          val usr = User.parseFrom(iS)
          _count += 1
          userCache ! InsertUser(usr)
          iS.close()
        }
      }
      log.info("Loading users from the storage is finished. Loaded {} users", _count)
  }
}

object FileStorage {
  def props(storeConfig: StoreConfig) = Props(new FileStorage(storeConfig))
}
