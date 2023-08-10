package com.hokko.alpha.database
import com.hokko.alpha.models.database.{User, UserKeys}
import slick.lifted.ProvenShape

import java.sql.Date

object Tables {

    import slick.jdbc.PostgresProfile
    import slick.jdbc.PostgresProfile.api._
    import slick.relational._

    private val schema = Some("project_alpha")

    private val userModel = com.hokko.alpha.models.database.User.tupled
    private val userKeyModel = com.hokko.alpha.models.database.UserKeys.tupled

    lazy val userTable = TableQuery[UserTable]
    lazy val userKeyTable = TableQuery[UserKeyTable]

    class UserTable(tag: Tag) extends Table[com.hokko.alpha.models.database.User](tag,schema, "users" ){
        def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def username = column[String]("username", O.Unique)
        def createdAt = column[Date]("created_at")
        override def * : ProvenShape[User] = (id, username, createdAt) <> (userModel, com.hokko.alpha.models.database.User.unapply)
    }

    class UserKeyTable(tag: Tag) extends Table[com.hokko.alpha.models.database.UserKeys](tag, schema, "keytable"){
        // user_id, algo, hash, salt, lastupdate
        def userId = column[Long]("user_id", O.PrimaryKey)
        def algo = column[String]("algo")
        def hash = column[Array[Byte]]("hash")
        def salt = column[Array[Byte]]("salt")
        def lastUpdate = column[Date]("lastupdate")
        override def * : ProvenShape[UserKeys] = (userId, algo, hash, salt, lastUpdate) <> (userKeyModel, com.hokko.alpha.models.database.UserKeys.unapply)
    }

}
