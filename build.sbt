val scala3Version = "3.2.0"

val zioVersion = "2.0.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "lithium-scala",
    version := "0.1.0-SNAPSHOT",

    scalacOptions ++= Seq("-Xmax-inlines", "70"),

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-json" % "0.3.0-RC8",
      "dev.zio" %% "zio-streams" % zioVersion,
      "io.d11" %% "zhttp" % "2.0.0-RC10",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
    )
  )
