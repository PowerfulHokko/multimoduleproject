package com.hokko.alpha.db
import com.hokko.alpha.db.model.Customer
import slick.lifted.ProvenShape

object Tables {

    import slick.jdbc.PostgresProfile
    import slick.jdbc.PostgresProfile.api._
    import slick.relational._

    private val schema = Some("public")

    private val customerModel = com.hokko.alpha.db.model.Customer.tupled

    object Tables{
        lazy val customerTable = TableQuery[CustomerTable]
    }

    class CustomerTable(tag: Tag) extends Table[com.hokko.alpha.db.model.Customer](tag, schema, "customer"){
        private def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        private def groupId = column[Long]("id_customergroup", O.Default(1))
        private def name = column[String]("name", O.Unique)
        private def isActive = column[Boolean]("isactive", O.Default(true))

        override def * : ProvenShape[Customer] = {
            (id, groupId, name, isActive) <> (customerModel, com.hokko.alpha.db.model.Customer.unapply)
        }
    }
}
