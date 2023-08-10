package com.hokko.alpha

import pureconfig.error.ConfigReaderFailures
import spray.json.DefaultJsonProtocol

object SecurityConfiguration {

    import pureconfig._
    import pureconfig.generic.auto._

    lazy val getConfiguration: Option[SecurityConfiguration] = {
        ConfigSource.resources("security.conf").at("security-config").load[SecurityConfiguration] match {
            case Left(value: ConfigReaderFailures) => {
                println(value.prettyPrint())
                Option.empty[SecurityConfiguration]
            }
            case Right(value: SecurityConfiguration) => {
                Option(value)
            }
        }
    }

}

case class SecurityConfiguration(
        keystoreFile: String,
        keystorePw: String,
        keystoreAlgo: String,
        hashAlgo: String,
        dbSchema: String,
)

object SecurityDomain extends DefaultJsonProtocol{
    sealed trait AuthRequest

    case class Login(username: String, password: String) extends AuthRequest

    case class JwtTokenInRequest(token: String) extends AuthRequest

    case class PrivateKey(pkFile: java.io.File) extends AuthRequest

    implicit val loginJsonFormat = jsonFormat2(Login)

    sealed trait AuthResponse

    case class JwtTokenInResponse(token: String) extends AuthResponse

    object Unauthorized extends AuthResponse

    object TooManyLoginAttempts extends AuthResponse
}