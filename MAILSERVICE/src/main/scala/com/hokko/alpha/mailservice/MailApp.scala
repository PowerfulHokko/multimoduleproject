package com.hokko.alpha.mailservice

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object MailApp {
    private implicit val system: ActorSystem = ActorSystem("Sec")
    private implicit val materializer: ActorMaterializer = ActorMaterializer()
    private implicit val timeout = Timeout(2 second)

    private val mailActor = system.actorOf(Props(new MailService))

    def send(to: Option[String] = Option.empty[String], subject: String, message: String) = {
        mailActor ! MailService.SendSimpleMail(to,subject, message)
    }

    def terminate() = {
        system.terminate()
    }

    //todo: refactor mailservice and use config
}
