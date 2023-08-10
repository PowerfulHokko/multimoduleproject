package com.hokko.alpha.db

import akka.actor.{Actor, ActorLogging}
import com.hokko.alpha.db.DbAsk.GetAllCustomers

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object DbAsk{
    case object GetAllCustomers
}

class DatabaseService extends Actor with ActorLogging{

    import Connection._
    import Tables._
    import slick.jdbc.PostgresProfile.api._

    implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(5)
    )

    override def receive: Receive = {
        case GetAllCustomers => {
            val query = Tables.customerTable.result
            sender() ! Connection.db.run(query)
        }
    }
}
