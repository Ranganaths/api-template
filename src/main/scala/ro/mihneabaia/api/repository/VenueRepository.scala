package ro.mihneabaia.api.repository

import java.time.Instant

import ro.mihneabaia.api.model.base.{Status, Version}
import ro.mihneabaia.api.model.{ArenaId, VenueId, Venue}
import ro.mihneabaia.api.repository.base.{JdbcProfileAware, ModelEntityRepository}

trait VenueRepository extends ModelEntityRepository { this: JdbcProfileAware with ArenaRepository =>

  import profile.api._

  class VenueTable(tag: Tag) extends Table[Venue](tag, "venue") with ModelEntityWithStatusTable[VenueId, Venue] {
    def id = column[VenueId]("id", O.PrimaryKey, O.AutoInc)
    def version = column[Version]("version")
    def created = column[Instant]("created")
    def modified = column[Instant]("modified")
    def idArena = column[ArenaId]("id_arena")
    def status = column[Status]("status")
    def name = column[String]("name", O.Length(50))
    def description = column[String]("description")

    def nameUniqueIdx = index("name_UNIQUE", name, unique = true)
    def fkArena = foreignKey("fk_arena", idArena, arenas)(_.id)

    def * = (id.?, version.?, created.?, modified.?, idArena, status, name, description) <> (Venue.tupled, Venue.unapply)
  }

  object venues extends ModelEntityWithStatusQueries[VenueId, Venue, VenueTable](new VenueTable(_)) {

    override def copyIdVersionAuditAndStatus(venue: Venue, id: Option[VenueId],
                                             version: Option[Version], created: Option[Instant],
                                             modified: Option[Instant], status: Status): Venue =
      venue.copy(id = id, version = version, created = created, modified = modified, status = status)

    def countForArena(idArena: ArenaId): DBIO[Int] = {
      this.filter(v => v.idArena === idArena && v.status === Status.active).length.result
    }
  }

}
