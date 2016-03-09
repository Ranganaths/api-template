package ro.mihneabaia.api.router

import akka.actor.Actor
import ro.mihneabaia.api.boot.ActorContextAware
import ro.mihneabaia.api.repository.base.AppConfigDbAware
import spray.routing._
import spray.http._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiRouterServiceActor extends Actor with ApiRouterService with AppConfigDbAware {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(
    logRequest("Request received") {
      logResponse("Response sent") {
        apiRoutes
      }
    }
  )
}

// this trait defines our service behavior independently from the service actor
trait ApiRouterService extends HttpService with ActorContextAware with ApiRouter
