package com.hokko.alpha

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.hokko.alpha.db.{DatabaseService, DbAsk}
import org.apache.spark.sql._
import org.apache.spark.sql.types._

import java.util.concurrent.Executors
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.language.postfixOps

object HelloWorld extends App {

    // testSpark()
    //testDB()

    private def testChimney() = {
        import io.scalaland.chimney.dsl._

        case class InitialSession(id: Int, user: String, initializedAt: Int)
        case class Session(id: Int, user: String, initializedAt: Int)

        val initialSession = InitialSession(1 ,"Alice", )

    }

    private def testDB(): Unit = {
        implicit val system: ActorSystem = ActorSystem(this.getClass.getSimpleName.replace("$", ""))
        implicit val mat: ActorMaterializer = ActorMaterializer()
        implicit val exc: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))
        val dbService: ActorRef = system.actorOf(Props(new DatabaseService))

        implicit val timeout = Timeout(2 second)

        val res = (dbService ? DbAsk.GetAllCustomers)
            .mapTo[Future[Seq[com.hokko.alpha.db.model.Customer]]]
            .flatten

        val finalRes = Await.result(res, 2 second)

        finalRes.foreach(println)
    }

    private def testSpark(): Unit = {
        val spark = SparkSession
            .builder()
            .appName("HelloWorld")
            .master("local[*]")
            .getOrCreate()

        val read = spark.read.textFile("C:\\Users\\Jurgen Rutten\\IdeaProjects\\Project-Alpha\\src\\main\\resources\\data\\hello.txt")
        read.show()
        spark.stop()
    }

}
