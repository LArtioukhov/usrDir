package itc.usrDir

import akka.Done
import akka.actor.SupervisorStrategy.Stop
import akka.actor.{
  Actor,
  ActorRef,
  ActorSystem,
  CoordinatedShutdown,
  OneForOneStrategy,
  Props
}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import itc.usrDir.config.WSConfig

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

class RootSupervisor extends Actor {
  import itc.usrDir.RootSupervisor._

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _: Exception => Stop
    }

  override def receive: Receive = {
    case DoStart => sender ! Started
    case DoStop => sender ! Stopped
  }
}

object RootSupervisor extends WebServiceRoutes with WSConfig {

  case object DoStart
  case object DoStop
  case object Started
  case object Stopped
  case object AlreadyStared
  case object AlreadyStopped

  implicit private var _actorSystem: ActorSystem = _
  implicit private var _actorMaterializer: ActorMaterializer = _
  implicit private var _executionContext: ExecutionContext = _

  private var _instance: ActorRef = _
  private var _binding: Future[Http.ServerBinding] = _
  private lazy val log = Logging(_actorSystem, this.getClass)
  private lazy val route = generateRoute(currentConfig)

  private def props = Props[RootSupervisor]
  override def rawConfig: Config =
    _actorSystem.settings.config.getConfig(serviceName)

  def init(): Unit = {
    _actorSystem = ActorSystem(serviceName)
    log.info("RootSupervisor init")
    log.debug("Actor system created")
    _actorMaterializer = ActorMaterializer()
    log.debug("Actor materializer created")
    _executionContext = _actorSystem.dispatcher
    log.info("RootSupervisor initiated")
  }

  def start(): Unit = {
    log.info("RootSupervisor starting")
    _instance = _actorSystem.actorOf(props)
    _binding = Http().bindAndHandle(route, webInterfaceConfig.host, webInterfaceConfig.port)
    _binding.onComplete {
      case Failure(exception) =>
      case Success(bound) =>
        import akka.pattern.ask
        implicit val to: Timeout = Timeout(5.seconds)
        log.info(
          s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
        log.info(
          s"Server status available at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/status")
        log.debug("Apps config - {}", currentConfig)
        CoordinatedShutdown(_actorSystem).addTask(
          CoordinatedShutdown.PhaseBeforeServiceUnbind,
          "RootSupervisorStopping") { () =>
            (_instance ? DoStop).map(_ => Done)
          }
        _instance ? DoStart onComplete {
          case Failure(exception) =>
          case Success(Started | AlreadyStared) =>
            log.info("Success started")
          case Success(any) =>
        }
    }
    log.info("RootSupervisor started")
  }

  def stop(): Unit = {

    val done: Future[Done] = CoordinatedShutdown(_actorSystem).run(CoordinatedShutdown.UnknownReason)

  }

}
