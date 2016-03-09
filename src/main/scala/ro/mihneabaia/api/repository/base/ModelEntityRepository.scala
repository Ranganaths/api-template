package ro.mihneabaia.api.repository.base

import java.sql.{SQLIntegrityConstraintViolationException, Timestamp}
import java.time.Instant

import ro.mihneabaia.api.model.base._
import ro.mihneabaia.api.model.base.exception.{DataConstraintViolation, DataNotFound, StaleDataException}

import scala.concurrent.ExecutionContext
import scala.util.{Try, Success, Failure}

trait ModelEntityRepository { this: JdbcProfileAware =>

  // we are using the JdbcProfile api
  import profile.api._

  implicit lazy val instantMapper = MappedColumnType.base[Instant, Timestamp] (
    instant => if (instant == null) null else Timestamp.from(instant),
    timestamp => if (timestamp == null) null else timestamp.toInstant
  )

  trait BaseModelEntityTable[I <: ModelEntityId, M <: BaseModelEntity[I]] extends Table[M] {
    def id: Rep[I]
  }

  trait VersionAware[M] { this: Table[M] =>
    def version: Rep[Version]
  }

  trait AuditAware[M] { this: Table[M] =>
    def created: Rep[Instant]
    def modified: Rep[Instant]
  }

  trait StatusAware[M] { this: Table[M] =>
    def status: Rep[Status]
  }

  trait ModelEntityTable[I <: ModelEntityId, M <: ModelEntity[I]]
    extends BaseModelEntityTable[I, M] with VersionAware[M] with AuditAware[M]

  trait ModelEntityWithStatusTable[I <: ModelEntityId, M <: ModelEntityWithStatus[I]]
    extends ModelEntityTable[I, M] with StatusAware[M]

  abstract class BaseModelEntityQueries[I <: ModelEntityId: BaseColumnType, M <: BaseModelEntity[I], T <: BaseModelEntityTable[I, M]]
    (cons: Tag => T) extends TableQuery(cons) {

    /**
      * This method needs to be implemented in the concrete class to copy the model entity to a new instance with the
      * id updated.
      */
    def copyId(m: M, id: Option[I]): M

    val table = this

    def list: DBIO[Seq[M]] = (for (m <- table) yield m).result

    def listPaginated(offset: Int, limit: Int)(implicit ec: ExecutionContext): DBIO[ModelEntityPage[I, M]] =
      paginateQuery(for (p <- table) yield p, offset, limit)

    protected def paginateQuery(query: Query[T, M, Seq], offset: Int, limit: Int)
                               (implicit ec: ExecutionContext): DBIO[ModelEntityPage[I, M]] =
      (for {
        entities <- query.sortBy(_.id.desc).drop(offset).take(limit).result
        total <- query.length.result
      } yield ModelEntityPage[I, M](entities, total)).transactionally

    def find(id: I) = (for (m <- table if m.id === id) yield m).result.headOption

    def get(id: I)(implicit ec: ExecutionContext) =
      (for (m <- table if m.id === id) yield m).result.headOption
        .map(_.getOrElse(throw new DataNotFound(s"No data with id $id was found.")))

    def count: DBIO[Int] = (for (m <- table) yield m).length.result

    def create(m: M)(implicit ec: ExecutionContext): DBIO[M] = {
      if (m.isPersisted) throw new IllegalArgumentException(s"Cannot insert an already persisted model entity [$m]")

      ((table returning table.map(_.id) into {
        case(t, id) => copyId(t, Some(id))
      }) += m).asTry.map(handleExceptions)
    }

    def modify(m: M)(implicit ec: ExecutionContext): DBIO[M] = {
      if (!m.isPersisted)
        throw new IllegalArgumentException(s"Cannot update a model entity that was not previously persisted [$m]")

      (for (r <- table if r.id === m.id.get) yield r).update(m).map(_ => m).asTry.map(handleExceptions)
    }

    def delete(id: I)(implicit ec: ExecutionContext): DBIO[Int] =
      (for (m <- table if m.id === id) yield m).delete.asTry.map(handleExceptions)

    protected def handleExceptions[E]: Try[E] => E = {
      case Success(result) => result
      case Failure(t: SQLIntegrityConstraintViolationException) =>
        throw DataConstraintViolation(t.getMessage, t)
      case Failure(t) => throw t
    }
  }

  abstract class ModelEntityQueries[I <: ModelEntityId: BaseColumnType, M <: ModelEntity[I], T <: ModelEntityTable[I, M]]
    (cons: Tag => T) extends BaseModelEntityQueries[I, M, T](cons) {

    /**
      * This method needs to be implemented in the concrete class to copy the model entity to a new instance with the
      * id, version and time audit fields updated
      */
    def copyIdVersionAndAudit(m: M, id: Option[I], version: Option[Version],
                              created: Option[Instant], modified: Option[Instant]): M

    override def copyId(m: M, id: Option[I]) = copyIdVersionAndAudit(m, id, m.version, m.created, m.modified)

    def find(id: I, version: Version) = (for (m <- table if m.id === id && m.version === version) yield m).result.headOption

    def get(id: I, version: Version) = (for (m <- table if m.id === id && m.version === version) yield m).result.head

    override def create(m: M)(implicit ec: ExecutionContext): DBIO[M] = {
      if (m.isPersisted) throw new IllegalArgumentException(s"Cannot insert an already persisted model entity [$m]")

      val now = Instant.now()
      val toInsert = copyIdVersionAndAudit(m, None, Some(Version.first), Some(now), Some(now))
      ((table returning table.map(_.id) into {
        case(t, id) => copyId(t, Some(id))
      }) += toInsert).asTry.map(handleExceptions)
    }

    override def modify(m: M)(implicit ec: ExecutionContext): DBIO[M] = {
      if (!m.isPersisted)
        throw new IllegalArgumentException(s"Cannot update a model entity that was not previously persisted [$m]")

      val query = for (r <- table if r.id === m.id && r.version === m.version) yield r
      (for {
        current <- get(m.id.get)
        toSave = copyIdVersionAndAudit(m, current.id, current.version.map(_.increment), current.created, Some(Instant.now))
        result <- query.update(toSave)
      } yield {
        if (result > 0)
          toSave
        else
          throw StaleDataException(s"Unable to update with stale data. Expected version: ${m.version}", Some(toSave))
      }).asTry.map(handleExceptions).transactionally
    }

    def delete(id: I, version: Version)(implicit ec: ExecutionContext): DBIO[Int] =
      (for (m <- table if m.id === id && m.version === version) yield m).delete
        .map { deleted =>
          if (deleted == 0)
            throw StaleDataException("Unable to delete stale data")
          else
            deleted
        }.asTry.map(handleExceptions)
  }

  abstract class ModelEntityWithStatusQueries[I <: ModelEntityId: BaseColumnType, M <: ModelEntityWithStatus[I],
    T <: ModelEntityWithStatusTable[I, M]] (cons: Tag => T) extends ModelEntityQueries[I, M, T](cons) {

    /**
      * This method needs to be implemented in the concrete class to copy the model entity to a new instance with the
      * id, version and time audit fields updated
      */
    def copyIdVersionAuditAndStatus(m: M, id: Option[I], version: Option[Version],
                              created: Option[Instant], modified: Option[Instant], status: Status): M

    override def copyIdVersionAndAudit(m: M, id: Option[I], version: Option[Version],
                                       created: Option[Instant], modified: Option[Instant]): M =
      copyIdVersionAuditAndStatus(m, id, version, created, modified, m.status)

    val notDeleted = filter(_.status =!= Status.deleted)

    def findNotDeleted(id: I) =
      (for (m <- notDeleted if m.id === id) yield m).result.headOption

    def findNotDeleted(id: I, version: Version) =
      (for (m <- notDeleted if m.id === id && m.version === version) yield m).result.headOption

    def deleteUsingStatus(id: I, version: Version)(implicit ec: ExecutionContext): DBIO[M] = {
      val query = for (r <- table if r.id === id && r.version === version) yield r
      (for {
        current <- get(id)
        toSave = copyIdVersionAuditAndStatus(current, current.id, current.version.map(_.increment), current.created, Some(Instant.now), Status.deleted)
        result <- query.update(toSave)
      } yield {
        if (result > 0)
          toSave
        else
          throw StaleDataException(s"Unable to update with stale data. Expected version: ${version}", Some(toSave))
      }).asTry.map(handleExceptions).transactionally
    }

    def listNotDeleted: DBIO[Seq[M]] = (for (m <- table if m.status =!= Status.deleted) yield m).result

    def listNotDeletedPaginated(offset: Int, limit: Int)(implicit ec: ExecutionContext) =
      paginateQuery(for (m <- table if m.status =!= Status.deleted) yield m, offset, limit)

    def enable(m: M)(implicit ec: ExecutionContext): DBIO[M] =
      modify(copyIdVersionAuditAndStatus(m, m.id, m.version, m.created, m.modified, Status.active))

    def disable(m: M)(implicit ex: ExecutionContext): DBIO[M] =
      modify(copyIdVersionAuditAndStatus(m, m.id, m.version, m.created, m.modified, Status.disabled))
  }
}
