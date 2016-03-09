package ro.mihneabaia.api.repository

import java.time.Instant

import ro.mihneabaia.api.model.base.{Status, Version}
import ro.mihneabaia.api.model.{ContactId, Arena, ArenaId, Coordinate}
import ro.mihneabaia.api.repository.base.{JdbcProfileAware, ModelEntityRepository}

trait ArenaRepository extends ModelEntityRepository { this: JdbcProfileAware with ContactRepository =>

  import profile.api._

  class ArenaTable(tag: Tag) extends Table[Arena](tag, "arena") with ModelEntityWithStatusTable[ArenaId, Arena] {
    def id = column[ArenaId]("id", O.PrimaryKey, O.AutoInc)
    def version = column[Version]("version")
    def created = column[Instant]("created")
    def modified = column[Instant]("modified")
    def status = column[Status]("status")
    def name = column[String]("name", O.Length(50))
    def description = column[String]("description")
    def idContact = column[ContactId]("id_contact")

    def nameUniqueIdx = index("name_UNIQUE", name, unique = true)
    def fkContact = foreignKey("fk_arena_contact", idContact, contacts)(_.id)

    def * = (id.?, version.?, created.?, modified.?, status, name, description, idContact.?) <> (Arena.tupled, Arena.unapply)
  }

  object arenas extends ModelEntityWithStatusQueries[ArenaId, Arena, ArenaTable](new ArenaTable(_)) {

    override def copyIdVersionAuditAndStatus(arena: Arena, id: Option[ArenaId],
                                             version: Option[Version], created: Option[Instant],
                                             modified: Option[Instant], status: Status): Arena =
      arena.copy(id = id, version = version, created = created, modified = modified, status = status)
  }

}
