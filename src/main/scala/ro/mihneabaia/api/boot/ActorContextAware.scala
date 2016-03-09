package ro.mihneabaia.api.boot

import akka.actor.ActorContext

trait ActorContextAware {
  implicit val context: ActorContext
}
