package ro.mihneabaia.api.router

import ro.mihneabaia.api.boot.{ActorContextAware, LoggerAware}
import ro.mihneabaia.api.domain.Arena
import ro.mihneabaia.api.repository.DataAccess
import ro.mihneabaia.api.router.base.RouteBuilder

import scala.concurrent.ExecutionContext
import ro.mihneabaia.api.domain.ArenaJsonProtocol

trait ArenaRouter extends ArenaJsonProtocol {

  implicit def nonDbExecutionContext: ExecutionContext

  implicit def dataAccess: DataAccess

  val arenaRoutesBuilder =
    RouteBuilder[Arena]("arena")
      .withCreateRoute(Arena)
      .withUpdateRoute(Arena)
      .withReadRoute(Arena)
      .withReadAllRoute(Arena)
      .withReadPageRoute(Arena)
      .withDeleteRoute(Arena)
      .withPartialUpdateRoute(Arena)
}
