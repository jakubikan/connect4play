name := "connect4play"

version := "1.0-SNAPSHOT"


resolvers += "webjars" at "http://webjars.github.com/m2"

resolvers += "db4omaverepositories" at "http://source.db4o.com/maven"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    javaJdbc,
    javaEbean,
    cache,
    "org.webjars" %% "webjars-play" % "2.2.0",
    "org.webjars" % "webjars-locator" % "0.5",
    "org.webjars" % "bootstrap" % "2.3.2",
    "org.webjars" % "jquery" % "2.0.3-1",
    "com.google.inject" % "guice" % "4.0-beta",
    "com.novocode" % "junit-interface" % "0.9" % "test",
    "com.db4o" % "db4o-full-java5" % "8.1-SNAPSHOT",
    "org.hibernate" % "hibernate-core" % "4.2.+",
    "mysql" % "mysql-connector-java" % "5.1.+",
    "org.ektorp" % "org.ektorp" % "1.4.1"
)


play.Project.playJavaSettings
