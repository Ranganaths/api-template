package ro.mihneabaia.api.domain.base

import ro.mihneabaia.api.repository.DataAccess
import ro.mihneabaia.api.validation.Validator

import scala.concurrent.{ExecutionContext, Future}

trait ResourceCreate[R <: Resource] {
  def validateForCreate(resource: R)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Validator] =
    Future.successful(Validator())

  def create(resource: R)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[R]
}

trait ResourceRead[R <: Resource] {
  def read(id: Long)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Option[R]]
}

trait ResourceReadAll[R <: Resource] {
  def readAll(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Seq[R]]
}

trait ResourceReadPage[R <: Resource] {
  def readPage(offset: Int, limit: Int)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[ResourcePage[R]]
}

trait ResourceUpdate[R <: Resource] {
  def validateForUpdate(resource: R, version: Version)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Validator] =
    Future.successful(Validator())

  def update(resource: R, id: Long, version: Version)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[R]
}

trait ResourceDelete {
  def delete(id: Long, version: Version)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Unit]
}

trait ResourceCRUD[R <: Resource]
  extends ResourceCreate[R]
    with ResourceRead[R]
    with ResourceUpdate[R]
    with ResourceDelete
    with ResourceReadAll[R]
    with ResourceReadPage[R]