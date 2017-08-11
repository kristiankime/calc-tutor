package modules

import com.google.inject.AbstractModule
import controllers.auth.{AccessAuthorizer, DemoHttpActionAdapter, RoleAdminAuthGenerator}
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.client.Clients
import org.pac4j.core.client.unauthenticated.RedirectUnauthenticatedClient
import org.pac4j.core.config.Config
import org.pac4j.http.client.indirect.{FormClient, IndirectBasicAuthClient}
import org.pac4j.http.credentials.authenticator.test.AuthenticateInTestModeAuthenticator
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import org.pac4j.play.{CallbackController, LogoutController}
import play.api.{Configuration, Environment, Logger}
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import com.google.inject.Provides
import org.pac4j.sql.profile.service.DbProfileService


/**
  * Guice DI module to be included in application.conf (// https://www.playframework.com/documentation/2.5.x/PluginsToModules)
  *
  * this module is based on play-pac4j normal setup https://github.com/pac4j/play-pac4j#2-define-the-configuration-config--client--authorizer--playsessionstore
  * +
  * Adding a sql backend http://www.pac4j.org/2.0.x/docs/authenticators/sql.html
  * +
  * Figuring out how to "inject" (really provide) the Database the rest of the system was using into this module https://groups.google.com/forum/#!topic/play-framework/Odr0dPxJn8I
  */
class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {
  val dbName = "default" // This should match the configuration strings below
  val dbUrl = configuration.getString("slick.dbs.default.db.url").get
  val dbUser = configuration.getString("slick.dbs.default.db.user").get
  val dbPassword = configuration.getString("slick.dbs.default.db.password").get

  val baseUrl = configuration.getString("baseUrl").get

  val googleClientId = configuration.getString("googleClientId").get
  val googleSecret = configuration.getString("googleSecret").get

  // https://groups.google.com/forum/#!topic/play-framework/Odr0dPxJn8I
  // We need the Database to set up the DbProfileService
  @Provides def dbProfileService(databases: play.api.db.DBApi) : DbProfileService = {
    val dataSource = databases.database("default").dataSource
    val dbProfileService = new DbProfileService(dataSource)

    // http://www.pac4j.org/2.0.x/docs/authenticators/sql.html
    dbProfileService.setUsersTable("name_pass_login")
    dbProfileService.setIdAttribute("id")
    dbProfileService.setUsernameAttribute("user_name")
    dbProfileService.setPasswordAttribute("password")
    dbProfileService.setPasswordEncoder(new SpringSecurityPasswordEncoder(NoOpPasswordEncoder.getInstance())) // TODO put real encoder here
    dbProfileService
  }

  @Provides def config(dbProfileService: DbProfileService) : Config = {
    val formClient = new FormClient(baseUrl + "/auth/loginForm", dbProfileService)

    val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId(googleClientId)
    oidcConfiguration.setSecret(googleSecret)
    oidcConfiguration.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration")
    oidcConfiguration.addCustomParam("prompt", "consent")
    val oidcClient = new OidcClient[OidcProfile](oidcConfiguration)
    oidcClient.addAuthorizationGenerator(new RoleAdminAuthGenerator)
    //    oidcClient.setAuthenticator(dbProfileService) // TODO how to store google logins in db for access

    // Redirect if Unauthenticated
    val redirectUnauthenticatedClient = new RedirectUnauthenticatedClient("/auth/signIn")

    // HTTP - this is only used in testing
    val indirectBasicAuthClient = new IndirectBasicAuthClient(new AuthenticateInTestModeAuthenticator(configuration.getBoolean("testAuth").get ))

    val clients = new Clients(baseUrl + "/callback", formClient, oidcClient, redirectUnauthenticatedClient, indirectBasicAuthClient)

    val config = new Config(clients)
    config.addAuthorizer("Access", new AccessAuthorizer)
    config.setHttpActionAdapter(new DemoHttpActionAdapter())
    config
  }

  override def configure(): Unit = {
    // Play Cache
    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/secure")
    callbackController.setMultiProfile(true)
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[LogoutController]).toInstance(logoutController)
  }

}