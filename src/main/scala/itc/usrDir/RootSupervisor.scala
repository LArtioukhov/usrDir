package itc.usrDir

import akka.Done
import akka.actor.SupervisorStrategy._
import akka.actor.{CoordinatedShutdown => CS, _}
import akka.http.scaladsl.Http
import akka.pattern._
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import itc.globals.actorMessages._
import itc.globals.exceptions.ErrorAppNotInitialized
import itc.usrDir.commands.Load
import itc.usrDir.config.{CurrentConfig, FileStorageConfig, WSConfig}
import itc.usrDir.core.{FileStorage, UserCache}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class RootSupervisor extends Actor with ActorLogging {

  import itc.usrDir.RootSupervisor.getCurrentConfig

  private var userCache: ActorRef = _
  private var userStorage: ActorRef = _

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _: Exception => Stop
    }

  override def receive: Receive = {
    case DoStart =>
      getCurrentConfig.storeConfig match {
        case FileStorageConfig(path) =>
          userStorage = context.actorOf(FileStorage.props(getCurrentConfig.storeConfig), "UserStorage")
      }
      userCache = context.actorOf(UserCache.props(getCurrentConfig), "UserCache")
      userStorage ! Load
      sender ! Started
    case DoStop => sender ! Stopped
    case msg: Command => userCache.tell(msg, sender)
    case msg => log.info(msg.toString)
  }
}

object RootSupervisor extends WebServiceRoutes with WSConfig {

  override val serviceName: String = "userKeysCatalog"

  implicit private val _actorSystem: ActorSystem = ActorSystem(serviceName)
  implicit private val _actorMaterializer: ActorMaterializer = ActorMaterializer()
  implicit private val _executionContext: ExecutionContext = _actorSystem.dispatcher

  private var _instance: ActorRef = _
  private var _bindingFuture: Future[Http.ServerBinding] = _
  private var _grpcService: GrpsService = _

  override def rawConfig: Config = _actorSystem.settings.config.getConfig(serviceName)

  override def userCacheProcessor: ActorRef = _instance

  override def getCurrentConfig: CurrentConfig = currentConfig

  private lazy val log =
    akka.event.Logging(_actorSystem, classOf[RootSupervisor])
  private lazy val route = generateRoute()

  private def props = Props[RootSupervisor]

  def init(): Unit = {
    log.info("RootSupervisor init")
    _instance = _actorSystem.actorOf(props, "RootSupervisor")
    _bindingFuture = Http().bindAndHandle(route, getCurrentConfig.interfacesConfig.host, getCurrentConfig.interfacesConfig.webPrt)
    log.info("RootSupervisor initiated")
  }

  def start(): Unit = {
    log.info("RootSupervisor starting")
    _bindingFuture.flatMap { bound =>
      log.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
      log.info(
        s"Server status available at http://{}:{}/{}/{}/status",
        bound.localAddress.getHostString,
        bound.localAddress.getPort,
        getCurrentConfig.systemName,
        getCurrentConfig.version)
      log.debug("Apps config - {}", currentConfig)
      CS(_actorSystem)
        .addTask(CS.PhaseBeforeServiceUnbind, "RootSupervisorStopping") { () =>
          (_instance ? DoStop).map { response =>
            log.info(response.toString)
            Done
          }
        }
      _instance ? DoStart
    } onComplete {
      case Success(Started | AlreadyStarted) =>
        log.info("RootSupervisor started")
      case Success(any) =>
        log.error("Microservice {} can't start on cause: unexpected message from RootSupervisor - {}", serviceName, any)
        CS(_actorSystem).run(CS.UnknownReason)
      case Failure(exception) =>
        log.error("Service {} can't start on cause: {}", serviceName, exception)
        CS(_actorSystem).run(CS.UnknownReason)
    }

    _grpcService = GrpsService(getCurrentConfig.interfacesConfig, _instance)
    _grpcService.start()
    log.info("grpcService started on {}", _grpcService.servicePort)
  }

  def stop(): Unit = {
    if (_instance == null)
      throw ErrorAppNotInitialized(s"$serviceName not initialised")
    else {
      _bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => CS(_actorSystem).run(CS.JvmExitReason))
    }
  }

  def destroy(): Unit =
    if (_instance == null)
      throw ErrorAppNotInitialized(s"$serviceName not initialised")
    else {
      _bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => CS(_actorSystem).run(CS.JvmExitReason))
    }

}
