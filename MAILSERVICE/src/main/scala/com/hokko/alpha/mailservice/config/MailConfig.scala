package com.hokko.alpha.mailservice.config

protected case class MailConfig(
                     sender: String,
                     password: String,
                     mailTransportProtocol: String,
                     mailSmtpHost: String,
                     mailSmptPort: String,
                     mailSmptAuth: String,
                     mailSmtpStarttlsEnable: String,
                     mailSmtpSslTrust: String
                     )

object MailConfig{

    import pureconfig._
    import pureconfig.generic.auto._

    lazy val config: Option[MailConfig] = {
        ConfigSource.resources("mailservice.conf").at("mailservice-config").load[MailConfig] match {
            case Left(value) => {
                println(value.toList)
                Option.empty[MailConfig]
            }
            case Right(value) => Option(value)
        }
    }
}