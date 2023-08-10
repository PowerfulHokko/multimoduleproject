package com.hokko.alpha.database

import akka.actor.{Actor, ActorLogging}

import java.sql.Date
import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object AuthDatabase{
    case class RequestUser(username: String)
    case class RequestIfUserExists(username: String)
    case class RegisterUser(username: String, hash: Array[Byte], salt: Array[Byte], algo: String)
}

class AuthDatabase extends Actor with ActorLogging{
    import Tables._
    import com.hokko.alpha.models.database._
    import slick.jdbc.PostgresProfile.api._

    implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(5)
    )

    override def receive: Receive = {

        case AuthDatabase.RequestUser(username) => {
            val query = userTable.filter(_.username === username.toLowerCase)
                .join(userKeyTable).on((t1,t2) => t1.id === t2.userId)

            val result = Connection.db.run(query.result.headOption).mapTo[Option[(User, UserKeys)]]
            sender() ! result
        }

        case AuthDatabase.RequestIfUserExists(username) => {
            val foundUser = Tables.userTable
                .filter(_.username === username.toLowerCase).exists
            val res: Future[Boolean] = Connection.db.run(foundUser.result)
            sender() ! res
        }

        case AuthDatabase.RegisterUser(username, hash, salt, algo) => {
            val createUserQueryPrep = userTable returning userTable.map(_.id) into((item, id) => item.copy(id = id))
            val createKeyQueryPrep = (userKeyTable returning userKeyTable.map(_.userId))

            val action = (createUserQueryPrep += User(0, username.toLowerCase, Date.valueOf(LocalDate.now())))
                .flatMap(q1 => createKeyQueryPrep += UserKeys(q1.id, algo, hash, salt, Date.valueOf(LocalDate.now())))
                .transactionally

            val res = Connection.db.run(action).mapTo[Long]
            val req2 = Await.result(res, 1 second)

            sender() ! req2
        }

    }
}
