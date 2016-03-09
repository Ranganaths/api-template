package ro.mihneabaia.api.domain

import ro.mihneabaia.api.domain.base._
import ro.mihneabaia.api.model.{Arena => ModelArena, Contact => ModelContact, ArenaJsonProtocol => ModelArenaJsonProtocol, ContactJsonProtocol => ModelContactJsonProtocol, ArenaId}
import ro.mihneabaia.api.model.base.{Version => ModelVersion}
import ro.mihneabaia.api.repository.DataAccess
import ro.mihneabaia.api.router.exception.ResourceNotFoundException
import spray.json._

import ro.mihneabaia.api.utils.json.JsUtils._

import scala.concurrent.{Future, ExecutionContext}

case class Arena(modelArena: ModelArena, modelContact: ModelContact, venuesCount: Option[Int]) extends Resource {
  val id = modelArena.id.map(_.value)
  val version = modelArena.version.map(v => Version(v.value))
}

object Arena extends ResourceCRUD[Arena] {

  override def delete(id: Long, version: Version)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Unit] = {
    import dataAccess.profile.api._
    dataAccess.db.run(
      (for {
        arena <- dataAccess.arenas.find(ArenaId(id))
        deletedArena <- dataAccess.arenas.delete(
          arena.flatMap(_.id).
            getOrElse(throw new ResourceNotFoundException("arena", "delete", s"The resource with id $id was not found")),
          ModelVersion(version.value))
        deleted <- dataAccess.contacts.delete(
          arena.flatMap(_.idContact)
            .getOrElse(throw new ResourceNotFoundException("arena", "delete", s"The resource with id $id was not found")))
      } yield ()).transactionally
    )
  }

  override def readPage(offset: Int, limit: Int)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[ResourcePage[Arena]] = {
    import dataAccess.profile.api._
    def arenasPage =
      (for {
        (arena, venue) <- dataAccess.arenas.sortBy(_.id.desc).drop(offset).take(limit) joinLeft dataAccess.venues on (_.id === _.idArena)
        contact <- dataAccess.contacts if arena.idContact === contact.id
      } yield (arena, contact, venue)).groupBy(x => (x._1, x._2)).map {
        case ((arena, contact), values) => (arena, contact, values.length)
      }.sortBy(_._1.id.desc).result.map(_.map {
        case (arena, contact, venuesCount) => Arena(arena, contact, Some(venuesCount))
      })

    dataAccess.db.run(
      for {
        arenas <- arenasPage
        total <- dataAccess.arenas.length.result
      } yield {
        ResourcePage(total, offset, limit, arenas)
      }
    )
  }

  override def create(arena: Arena)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Arena] = {
    import dataAccess.profile.api._
    dataAccess.db.run(
      (for {
        contact <- dataAccess.contacts.create(arena.modelContact)
        arena <- dataAccess.arenas.create(arena.modelArena.copy(idContact = contact.id))
      } yield {
        Arena(arena, contact, Some(0))
      }).transactionally)
  }

  override def read(id: Long)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Option[Arena]] = {
    import dataAccess.profile.api._
    dataAccess.db.run(
      (for {
        (arena, venue) <- dataAccess.arenas filter (_.id === ArenaId(id)) joinLeft dataAccess.venues on (_.id === _.idArena)
        contact <- dataAccess.contacts if arena.idContact === contact.id
      } yield (arena, contact, venue)).groupBy(x => (x._1, x._2)).map {
        case ((arena, contact), values) => (arena, contact, values.length)
      }.result.map(_.headOption.map {
        case (arena, contact, venuesCount) => Arena(arena, contact, Some(venuesCount))
      })
    )
  }

  override def readAll(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Seq[Arena]] = {
    import dataAccess.profile.api._
    dataAccess.db.run(
      (for {
        (arena, venue) <- dataAccess.arenas.notDeleted joinLeft dataAccess.venues on (_.id === _.idArena)
        contact <- dataAccess.contacts if arena.idContact === contact.id
      } yield (arena, contact, venue)).groupBy(x => (x._1, x._2)).map {
        case ((arena, contact), values) => (arena, contact, values.length)
      }.sortBy {
        case (arena, contact, venuesCount) => arena.id.desc
      }.result.map(_.map {
        case (arena, contact, venuesCount) => Arena(arena, contact, Some(venuesCount))
      })
    )
  }

  override def update(arena: Arena, id: Long, version: Version)(implicit dataAccess: DataAccess, ec: ExecutionContext): Future[Arena] = {
    import dataAccess.profile.api._
    val arenaId = ArenaId(id)
    dataAccess.db.run(
      (for {
        existingModelArena <- dataAccess.arenas.find(arenaId).map(_.getOrElse(
          throw new ResourceNotFoundException("arena", "update", s"The resource with id $id was not found")
        ))
        updatedArena <- dataAccess.arenas.modify(
          arena.modelArena.copy(id = Some(arenaId), idContact = existingModelArena.idContact, version = Some(ModelVersion(version.value))))
        updatedContact <- dataAccess.contacts.modify(arena.modelContact.copy(id = updatedArena.idContact))
        venuesCount <- dataAccess.venues.countForArena(arenaId)
      } yield {
        Arena(updatedArena, updatedContact, Some(venuesCount))
      }).transactionally)
  }
}

trait ArenaJsonProtocol extends DefaultJsonProtocol with ModelArenaJsonProtocol with ModelContactJsonProtocol {
  implicit object ArenaFormat extends RootJsonFormat[Arena] {
    def read(json: JsValue): Arena = {
      val jsObj = json.asJsObject
      Arena(jsObj.convertTo[ModelArena], jsObj.fields("contact").asJsObject.convertTo[ModelContact], None)
    }

    def write(obj: Arena): JsValue =
      arenaFormat.write(obj.modelArena).asJsObject ++ JsObject(
      "contact" -> obj.modelContact.toJson,
      "venuesCount" -> obj.venuesCount.toJson
    )
  }

  implicit val arenaPageFormat = new ResourcePageJsonProtocol[Arena].resourcePageFormat
}
