name := "connect4play"

version := "1.0-SNAPSHOT"


resolvers += "webjars" at "http://webjars.github.com/m2"

resolvers += "stejack-connect4" at "http://lenny2.in.htwg-konstanz.de:8081/artifactory/libs-snapshot"

resolvers += "db4omaverepositories" at "http://source.db4o.com/maven"

libraryDependencies ++= Seq(
    javaJdbc,
    javaEbean,
    cache,
    "org.webjars" %% "webjars-play" % "2.2.0",
    "org.webjars" % "webjars-locator" % "0.5",
    "org.webjars" % "bootstrap" % "2.3.2",
    "org.webjars" % "jquery" % "2.0.3-1",
    "de.stejack" % "connect4plus" % "1.0-SNAPSHOT"
)


play.Project.playJavaSettings
