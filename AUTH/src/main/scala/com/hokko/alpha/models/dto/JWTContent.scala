package com.hokko.alpha.models.dto

import spray.json.DefaultJsonProtocol

object JWTContent extends DefaultJsonProtocol{
    case class JwtContent(hello: String)
    implicit val jwtContentFormat = jsonFormat1(JwtContent)
}