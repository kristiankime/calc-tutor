name := """calc-tutor""" // may need to manually set intellij module name to "root" https://stackoverflow.com/questions/30605802/play-framework-2-4-and-intellij-idea#34433904
organization := "com.artclod"

version := "0.0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.12.2"

// https://www.playframework.com/documentation/2.6.7/CacheMigration26
//libraryDependencies += cacheApi
libraryDependencies += ehcache
libraryDependencies += guice // https://www.playframework.com/documentation/2.6.7/Migration26#Guice-DI-support-moved-to-separate-module
libraryDependencies += ws
libraryDependencies += filters // https://www.playframework.com/documentation/2.5.x/AllowedHostsFilter
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test

// http://central.maven.org/maven2/org/pac4j/pac4j-stormpath/2.0.0/pac4j-stormpath-2.0.0.jar
// === Start Pac4j includes ===, example project here https://github.com/pac4j/play-pac4j-scala-demo
//resolvers += "mvnrepository.com" at "https://mvnrepository.com/artifact" // required to resolve `pac4j1 dependencies.
//resolvers += "Shibboleth Repository" at "https://build.shibboleth.net/nexus/content/repositories/releases"
resolvers += "Shibboleth releases"   at "https://build.shibboleth.net/nexus/content/repositories/releases/"

// https://github.com/pac4j/play-pac4j-scala-demo/blob/master/build.sbt
//resolvers += Resolver.mavenLocal
//resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
//resolvers += "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"

val playPac4jVersion = "6.0.1"
val pac4jVersion = "3.0.0"
libraryDependencies += "org.pac4j"  %% "play-pac4j"      % playPac4jVersion                                  withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-http"      % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-cas"       % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-openid"    % pac4jVersion exclude("xml-apis" , "xml-apis")     withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-oauth"     % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-saml"      % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-oidc"      % pac4jVersion exclude("commons-io" , "commons-io") withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-gae"       % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-jwt"       % pac4jVersion exclude("commons-io" , "commons-io") withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-ldap"      % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-sql"       % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-mongo"     % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  %  "pac4j-sql"       % pac4jVersion                                      withSources() withJavadoc()
libraryDependencies += "commons-io" %  "commons-io"      % "2.5"
// === End Pac4j includes ===

// === Start play slick / db includes === https://www.playframework.com/documentation/2.5.x/PlaySlick
//libraryDependencies += evolutions // Not needed since we have play-slick-evolutions https://www.playframework.com/documentation/2.5.x/Evolutions
libraryDependencies += "com.typesafe.play"            %% "play-slick"             % "3.0.0"           withSources() withJavadoc()
libraryDependencies += "com.typesafe.play"            %% "play-slick-evolutions"  % "3.0.0"           withSources() withJavadoc()
libraryDependencies += "com.h2database"               %  "h2"                     % "1.4.195"         withSources() withJavadoc()
libraryDependencies += "org.postgresql"               %  "postgresql"             % "9.4-1201-jdbc41" withSources() withJavadoc()
libraryDependencies += "org.springframework.security" %  "spring-security-crypto" % "4.2.2.RELEASE"   withSources() withJavadoc() // for Password Encryption
// === End play slick / db includes ===

// === Start Support libraries
libraryDependencies += "org.planet42" %% "laika-core"          % "0.8.0" withSources() withJavadoc() // Markdown lib
// === End Support libraries

// === Start Mail libraries
// TODO https://github.com/playframework/play-mailer
// === End Mail libraries

// === Joda Time libraries ===
libraryDependencies += jodaForms // https://www.playframework.com/documentation/2.6.7/Migration26#Joda-Time-removal
libraryDependencies += "com.typesafe.play" %% "play-json-joda" % "2.6.0" // https://stackoverflow.com/questions/46764755/joda-datetime-format-in-play-2-6-is-not-working  https://github.com/playframework/play-json/tree/a3c7748a2e42290c8a12c8e01fc70bc0a8f54aa3/play-json-joda/src

// === dependency loader ===

// === memcache ===
libraryDependencies += "com.github.mumoshu" %% "play2-memcached-play26" % "0.9.2" // memcache for Play 2.6 https://github.com/mumoshu/play2-memcached
resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

// === Less Filters ===
includeFilter in (Assets, LessKeys.less) := "*.less"
excludeFilter in (Assets, LessKeys.less) := "_*.less"

// Routing support
routesImport += "dao.binders._"
routesImport += "models._"