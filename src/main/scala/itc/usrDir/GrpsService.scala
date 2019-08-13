package itc.usrDir

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import itc.usrDir.commands._
import itc.usrDir.config.InterfacesConfig
import itc.usrDir.data._
import itc.usrDir.service.UserCatalogServiceGrpc
import itc.usrDir.service.UserCatalogServiceGrpc.UserCatalogService

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class GrpsService(interfacesConfig: InterfacesConfig, rootSupervisor: ActorRef, executionContext: ExecutionContext) {

  implicit val to: Timeout          = interfacesConfig.timeout
  private[this] var _server: Server = _

  def start(): Unit = {
    _server = NettyServerBuilder
      .forPort(interfacesConfig.grpcPort)
      .maxConcurrentCallsPerConnection(1024)
      .addService(UserCatalogServiceGrpc.bindService(new UserCatalogServiceGrpcImpl, executionContext))
      .build()
      .start()
  }

  def servicePort: Int = _server.getPort

  private class UserCatalogServiceGrpcImpl extends UserCatalogService {

    private def makeRequest[I <: Command, O: ClassTag](request: I): Future[O] =
      (rootSupervisor ? request).mapTo[O]

    override def checkAppKeyPresent(request: CheckKey): Future[UserKeyPresent] = makeRequest(request)

    override def setAppRoles(request: SetRoles): Future[User] = makeRequest(request)

    override def getAppUser(request: GetUser): Future[User] = makeRequest(request)
  }

}

object GrpsService {
  def apply(interfacesConfig: InterfacesConfig, rootSupervisor: ActorRef)(
      implicit executionContext: ExecutionContext): GrpsService =
    new GrpsService(interfacesConfig, rootSupervisor, executionContext)
}
