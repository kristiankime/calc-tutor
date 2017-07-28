name := """calc-tutor"""
organization := "com.artclod"

version := "0.0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.11"

libraryDependencies += filters
libraryDependencies += cache
libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test

// === Start Pac4j includes ===, example project here https://github.com/pac4j/play-pac4j-scala-demo
libraryDependencies += "org.pac4j"  % "play-pac4j"      % "3.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-http"      % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-cas"       % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-openid"    % "2.0.0" exclude("xml-apis" , "xml-apis")     withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-oauth"     % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-saml"      % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-oidc"      % "2.0.0" exclude("commons-io" , "commons-io") withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-gae"       % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-jwt"       % "2.0.0" exclude("commons-io" , "commons-io") withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-ldap"      % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-sql"       % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-mongo"     % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-stormpath" % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "org.pac4j"  % "pac4j-sql"       % "2.0.0"                                      withSources() withJavadoc()
libraryDependencies += "commons-io" % "commons-io"      % "2.5"
// === End Pac4j includes ===

// === Start play slick / db  includes === https://www.playframework.com/documentation/2.5.x/PlaySlick
//libraryDependencies += evolutions // Not needed since we have play-slick-evolutions https://www.playframework.com/documentation/2.5.x/Evolutions
libraryDependencies += "com.typesafe.play" %% "play-slick"            % "2.1.0"   withSources() withJavadoc()
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "2.1.0"   withSources() withJavadoc()
libraryDependencies += "com.h2database"    %  "h2"                    % "1.4.195" withSources() withJavadoc()
libraryDependencies += "org.postgresql"    %  "postgresql"            % "9.4-1201-jdbc41" withSources() withJavadoc()
libraryDependencies += "org.springframework.security" % "spring-security-crypto" % "4.2.2.RELEASE" withSources() withJavadoc() // for Password Encryption
// === End play slick / db  includes ===


includeFilter in (Assets, LessKeys.less) := "*.less"
excludeFilter in (Assets, LessKeys.less) := "_*.less"