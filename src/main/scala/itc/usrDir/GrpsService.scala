package itc.usrDir

import akka.actor.ActorRef
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

class GrpsService(interfacesConfig: InterfacesConfig, rootSupervisor: ActorRef, executionContext: ExecutionContext) {

  implicit val to: Timeout = interfacesConfig.timeout
  private[this] var _server: Server = _

  def start(): Unit = {
    _server = NettyServerBuilder
      .forPort(interfacesConfig.grpcPort)
      .maxConcurrentCallsPerConnection(1024)
      .addService(UserCatalogServiceGrpc.bindService(new UserCatalogServiceGrpcImpl, executionContext))
      .build()
      .start()
  }

  def stop(): Unit = {
    if (_server != null) _server.shutdown()
  }

  def servicePort: Int = _server.getPort

  private class UserCatalogServiceGrpcImpl extends UserCatalogService {

    override def checkAppKeyPresent(request: CheckKey): Future[UserKeyPresent] = (rootSupervisor ? request).mapTo[UserKeyPresent]

    override def setAppRoles(request: SetRoles): Future[User] = (rootSupervisor ? request).mapTo[User]

    override def getAppUser(request: GetUser): Future[User] = (rootSupervisor ? request).mapTo[User]
  }

}

object GrpsService {
  def apply(interfacesConfig: InterfacesConfig, rootSupervisor: ActorRef)(implicit
                                                                          executionContext: ExecutionContext): GrpsService =
    new GrpsService(interfacesConfig, rootSupervisor, executionContext)
}
