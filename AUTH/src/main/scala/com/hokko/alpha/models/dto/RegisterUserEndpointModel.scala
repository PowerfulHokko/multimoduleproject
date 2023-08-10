package com.hokko.alpha.models.dto

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class RegisterUserEndpointModel(username: String, password: String)

trait RegisterUserEndpointModelJson extends DefaultJsonProtocol{
    implicit val registerUserEndpointModelJson: RootJsonFormat[RegisterUserEndpointModel] = jsonFormat2(RegisterUserEndpointModel)
}