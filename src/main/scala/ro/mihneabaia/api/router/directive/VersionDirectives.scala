package ro.mihneabaia.api.router.directive

import ro.mihneabaia.api.domain.base.{Resource, Version}
import spray.http.HttpHeaders
import spray.http.HttpHeaders.ETag
import spray.routing.{Directive0, Directive1, Directives}

trait VersionDirectives extends Directives {

  def addResourceVersionToResponse(r: Resource): Directive0 =
    respondWithHeader(ETag(r.version.map(_.value.toString).getOrElse("")))

  def resourceVersion: Directive1[Version] =
    headerValueByType[HttpHeaders.`If-Match`](()).flatMap { header =>
      provide(Version(header.value.replaceAll("^\"|\"$", "").toLong))
    }

  def optionalResourceVersion: Directive1[Option[Version]] =
    optionalHeaderValueByType[HttpHeaders.`If-None-Match`](()).flatMap { header =>
      provide(header.map(h => Version(h.value.replaceAll("^\"|\"$", "").toLong)))
    }

}
