package com.hokko.alpha

import akka.actor.{ActorSystem, Props}
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.hokko.alpha.database.AuthDatabase
import com.hokko.alpha.models.database.{User, UserKeys}
import com.hokko.alpha.models.dto.{RegisterUserEndpointModel, RegisterUserEndpointModelJson}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.{existentials, postfixOps}
import spray.json._

import java.nio.charset.Charset
import java.time.Instant
import java.util.Date

object SecurityApp extends App with SprayJsonSupport with RegisterUserEndpointModelJson{
    import SecurityConfiguration._

    /*
        TODO:
        - JWT Content
        - JWT verification endpoint
        - clean up code (split db and dto)
        - simplify
        - write tests
        - complete analysis doc
     */

    implicit val system: ActorSystem = ActorSystem("Sec")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val dispatcher: MessageDispatcher = system.dispatchers.lookup("dedicated-dispatcher")

    val authDbActor = system.actorOf(Props(new AuthDatabase))
    implicit val timeout = Timeout(2 second)

    val hashfunction: (String, String, Array[Byte] ) => (String) => (String) = {
        (username, algo, salt) =>
            (pw) => {
                val hashed = HashUtil.createHashForUserWhenLogginAttempt(username, salt, algo, pw)
                new String(hashed, Charset.forName("UTF-8"))
            }
    }

    def authenticator(credentials: Credentials) : Future[Option[(String,String)]] = {
        credentials match {
            case Credentials.Missing => Future(Option.empty[(String,String)])
            case p @ Credentials.Provided(identifier) => {
                //check if user exists
                val existsFuture =  (authDbActor ? AuthDatabase.RequestIfUserExists(identifier)).mapTo[Future[Boolean]].flatten
                val exist = Await.result(existsFuture, 1 second)
                if(!exist){
                    Future(Option.empty[(String,String)])
                } else {
                    //get user from db
                    (authDbActor ? AuthDatabase.RequestUser(identifier)).mapTo[Future[Option[(User, UserKeys)]]]
                        .flatten
                        .map {
                            case None => Option.empty[(String,String)]
                            case Some(dbResponse) => {
                                val hashFromDb = dbResponse._2.hash
                                val isValid = p.verify(
                                    new String(hashFromDb, Charset.forName("UTF-8")),
                                    hashfunction(dbResponse._1.username, dbResponse._2.algo, dbResponse._2.salt)
                                )

                                if(isValid){
                                    //todo: create JwtToken and return username + token
                                    val jwtToken = HashUtil.createJwtToken(dbResponse._1.username)
                                    Option((dbResponse._1.username,jwtToken))
                                } else {
                                    Option.empty[(String,String)]
                                }
                            }
                        }
                    }
                }
            }
    }

    val serviceRoute: Route = {
        Route.seal{
            pathPrefix("authservice") {
                path("register") {
                    pathEndOrSingleSlash {
                        (post & extractRequest) { req =>
                            val createUserRequestFuture = req.entity.toStrict(1 second)
                                .map(_.data.utf8String)
                                .map(s => s.parseJson.convertTo[RegisterUserEndpointModel])

                            val createUserRequest = Await.result(createUserRequestFuture, 1 second)

                            val userExistFuture = (authDbActor ? AuthDatabase.RequestIfUserExists(createUserRequest.username))
                                .mapTo[Future[Boolean]].flatten

                            val userExists = Await.result(userExistFuture, 1 second)

                            if(userExists){
                                complete(
                                    StatusCodes.Unauthorized,
                                    HttpEntity("Username already registered")
                                )
                            } else {
                                val (hash, salt, algo) = HashUtil.createHashForUserWhenRegistering(createUserRequest.username, createUserRequest.password)

                                val userFuture = (authDbActor ? AuthDatabase.RegisterUser(createUserRequest.username, hash, salt, algo))
                                    .mapTo[Long]

                                val res = Await.result(userFuture, 1 second)

                                complete(StatusCodes.Created, HttpEntity(s"User: ${createUserRequest.username} created, ID = ${res}"))
                            }

                        } ~ get {
                            complete(StatusCodes.OK)
                        }
                    }
                } ~
                pathEndOrSingleSlash {
                    get {
                        complete(
                            HttpEntity(ContentTypes.`text/html(UTF-8)`, {
                                """
                                  |<div>
                                  |<h1>AuthService online.</h1>
                                  |</div>
                                  |""".stripMargin
                            }
                            )
                        )
                    } ~ post {
                        authenticateBasicAsync("localhost",authenticator){ s =>
                            respondWithHeader(RawHeader("auth-cookie",s"$s:${Date.from(Instant.now())}")){
                                complete(StatusCodes.OK)
                            }
                        }
                    }
                }
            }
        }

    }

        val server : Future[akka.http.scaladsl.Http.ServerBinding] = Http().newServerAt("localhost",8086).enableHttps(HttpsContext.httpsConnectionContext).bind(serviceRoute)

}
