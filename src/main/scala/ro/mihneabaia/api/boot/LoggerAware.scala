package ro.mihneabaia.api.boot

import akka.event.{Logging, LoggingAdapter}

trait LoggerAware { self: ActorContextAware =>

  implicit val logger: LoggingAdapter = Logging(context.system, self.getClass)

}
