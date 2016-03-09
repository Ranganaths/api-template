package ro.mihneabaia.api.repository

import ro.mihneabaia.api.model.{Coordinate, ContactId, Contact}
import ro.mihneabaia.api.repository.base.{JdbcProfileAware, ModelEntityRepository}

trait ContactRepository extends ModelEntityRepository { this: JdbcProfileAware =>

  import profile.api._

  class ContactTable(tag: Tag) extends Table[Contact](tag, "contact") with BaseModelEntityTable[ContactId, Contact] {
    def id = column[ContactId]("id", O.PrimaryKey, O.AutoInc)
    def email = column[Option[String]]("email", O.Length(128))
    def address = column[Option[String]]("address", O.Length(256))
    def phone = column[Option[String]]("phone", O.Length(50))
    def latitude = column[Option[Coordinate]]("latitude")
    def longitude = column[Option[Coordinate]]("longitude")

    override def * = (id.?, email, address, phone, latitude, longitude) <> (Contact.tupled, Contact.unapply)
  }

  object contacts extends BaseModelEntityQueries[ContactId, Contact, ContactTable](new ContactTable(_)) {

    override def copyId(contact: Contact, id: Option[ContactId]): Contact = contact.copy(id = id)
  }

}
