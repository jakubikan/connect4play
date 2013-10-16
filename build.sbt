name := "connect4play"

version := "1.0-SNAPSHOT"


resolvers += "webjars" at "http://webjars.github.com/m2"

libraryDependencies ++= Seq(
    javaJdbc,
    javaEbean,
    cache,
    "org.webjars" %% "webjars-play" % "2.2.0",
    "org.webjars" % "webjars-locator" % "0.5",
    "org.webjars" % "bootstrap" % "3.0.0",
    "org.webjars" % "jquery" % "2.0.3-1",
    "org.webjars" % "emberjs" % "1.0.pre",
    "org.webjars" % "coffee-script" % "1.6.3"
)

lazy val root = Project(id = "connect4play", base = file(".")) aggregate(connect4plus)

lazy val connect4plus = Project(id = "connect4plus", base = file("modules/connect4plus"))



play.Project.playJavaSettings
