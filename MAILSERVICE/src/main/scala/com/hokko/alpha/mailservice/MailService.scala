package com.hokko.alpha.mailservice

import akka.actor.{Actor, ActorLogging}
import com.hokko.alpha.mailservice.MailService.SendSimpleMail
import jakarta.mail._
import jakarta.mail.internet.{InternetAddress, MimeMessage}

import java.util.Properties

private class MailService extends Actor with ActorLogging{
    override def receive: Receive = {
        case simpleMail @ SendSimpleMail(to, subject, message) => {
            SimpleMailProtocol.send(simpleMail)
        }
    }
}

private object MailService{
    case class SendSimpleMail(to: Option[String], subject: String, message: String)
}
private object SimpleMailProtocol {
    private val config = com.hokko.alpha.mailservice.config.MailConfig.config

    def send(simpleMail: SendSimpleMail) = this.synchronized{
        config match {
            case None => {
                println("mail-service is offline")
            }
            case Some(mc) => {
                val sender: String = mc.sender
                val password: String = mc.password
                val receiver: String = simpleMail.to match {
                    case Some(value) => value
                    case None => mc.sender
                }

                val properties: Properties = new Properties()

                properties.put("mail.transport.protocol", mc.mailTransportProtocol)
                properties.put("mail.smtp.host", mc.mailSmtpHost)
                properties.put("mail.smtp.port", mc.mailSmptPort)
                properties.put("mail.smtp.auth", mc.mailSmptAuth)
                properties.put("mail.smtp.user", sender)
                properties.put("mail.smtp.password", password)
                properties.put("mail.smtp.starttls.enable", mc.mailSmtpStarttlsEnable)
                properties.put("mail.smtp.ssl.trust", mc.mailSmtpSslTrust)

                val mailSession = Session.getInstance(properties, new Authenticator() {
                    override protected def getPasswordAuthentication: PasswordAuthentication = {
                        new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.password"))
                    }
                })

                val message: MimeMessage = new MimeMessage(mailSession)
                val addressTo: InternetAddress = new InternetAddress(receiver)
                message.setRecipient(Message.RecipientType.TO, addressTo)
                message.setFrom(new InternetAddress(sender))
                message.setSubject(simpleMail.subject)
                message.setContent(simpleMail.message, "text/html")

                Transport.send(message)
            }
        }
    }



}
