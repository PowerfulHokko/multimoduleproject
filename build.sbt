ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

//versions
val akkaVersion = "2.8.3"
val akkaHttpVersion = "10.5.2"
val scalaTestVersion = "3.2.16"

//dependencies
val projDependencies = Seq(
    // akka streams
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    // akka http
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
    // testing
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion,

    // JWT
    "com.pauldijou" %% "jwt-spray-json" % "5.0.0",

    //auth
    "com.nulab-inc" %% "scala-oauth2-core" % "1.5.0",
    "com.nulab-inc" %% "akka-http-oauth2-provider" % "1.4.0",

    //database
    "com.typesafe.slick" %% "slick" % "3.5.0-M3",
    "org.postgresql" % "postgresql" % "42.6.0",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.5.0-M3",

    "com.github.tminglei" %% "slick-pg" % "0.22.0-M3",
    "com.github.tminglei" %% "slick-pg_play-json" % "0.22.0-M3",

    //spark
    "org.apache.spark" %% "spark-core" % "3.4.1" % "provided",
    "org.apache.spark" %% "spark-sql" % "3.4.1",
    "org.apache.spark" %% "spark-mllib" % "3.4.1",
    "org.apache.spark" %% "spark-streaming" % "3.4.1",

    //config
    "com.github.pureconfig" %% "pureconfig" % "0.17.4",

    //chimney
    "io.scalaland" % "chimney_2.13" % "0.8.0-M1",

    //mail
    "org.eclipse.angus" % "angus-mail" % "2.0.2"
)

lazy val global = project
    .in(file("."))
    //.disablePlugins(AssemblyPlugin)
    .settings(
        libraryDependencies ++= projDependencies,
        dependencyOverrides += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    )
    .aggregate(
        root,
        authService,
    )

lazy val root = (project in file("COMMON"))
  .settings(
        libraryDependencies ++= projDependencies,
        dependencyOverrides += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
        name := "Project-Alpha"
  )

lazy val mailService = (project in file("MAILSERVICE"))
    .settings(
        libraryDependencies ++= projDependencies,
        dependencyOverrides += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
        name := "Project-Alpha"
    )

lazy val authService = (project in file("AUTH"))
    .settings(
        name := "Project-Alpha AuthService",
        dependencyOverrides += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
        libraryDependencies ++= projDependencies,
    ).dependsOn(mailService)
