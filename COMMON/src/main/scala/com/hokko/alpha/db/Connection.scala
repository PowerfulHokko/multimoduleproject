package com.hokko.alpha.db

object Connection {
    import slick.jdbc.PostgresProfile.api._
    val db = Database.forConfig("postgres")
}
