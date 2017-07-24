package modules

import com.google.inject.AbstractModule
import controllers.auth.{CustomAuthorizer, DemoHttpActionAdapter, RoleAdminAuthGenerator}
import controllers.auth.DemoHttpActionAdapter
import org.pac4j.cas.client.{CasClient, CasProxyReceptor}
import org.pac4j.core.client.Clients
import org.pac4j.core.client.unauthenticated.RedirectUnauthenticatedClient
import org.pac4j.http.client.direct.{DirectBasicAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.{FormClient, IndirectBasicAuthClient}
import org.pac4j.http.credentials.authenticator.test.{AuthenticateInTestModeAuthenticator, SimpleTestUsernamePasswordAuthenticator}
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.oauth.client.{FacebookClient, TwitterClient}
import org.pac4j.oidc.client.OidcClient
import org.pac4j.play.{CallbackController, LogoutController}
import org.pac4j.saml.client.SAML2ClientConfiguration
import play.api.{Configuration, Environment}
import java.io.File

import org.h2.jdbcx.JdbcConnectionPool
import org.pac4j.cas.config.{CasConfiguration, CasProtocol}
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.config.Config
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.saml.client.SAML2Client
import org.pac4j.sql.profile.service.DbProfileService
import org.h2.jdbcx.JdbcConnectionPool
import org.pac4j.sql.profile.service.DbProfileService
import javax.sql.DataSource

import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder
import org.springframework.security.crypto.password.{NoOpPasswordEncoder, PasswordEncoder}

/**
  * Guice DI module to be included in application.conf
  */
class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {

    // ====== Variables from Configuration ======
    val baseUrl = configuration.getString("baseUrl").get
    val googleClientId = configuration.getString("googleClientId").get
    val googleSecret = configuration.getString("googleSecret").get

    val dbUrl = configuration.getString("slick.dbs.default.db.url").get
    val dbUser = configuration.getString("slick.dbs.default.db.user").get
    val dbPassword = configuration.getString("slick.dbs.default.db.password").get

    // ====== DB Connection
    val dataSource = JdbcConnectionPool.create(dbUrl, dbUser, dbPassword) // http://www.pac4j.org/2.0.x/docs/authenticators/sql.html
    val dbProfileService = new DbProfileService(dataSource) // https://github.com/pac4j/pac4j/pull/848
    dbProfileService.setUsersTable("logins")
    dbProfileService.setIdAttribute("id")
    dbProfileService.setUsernameAttribute("user_name")
    dbProfileService.setPasswordAttribute("password")
    dbProfileService.setPasswordEncoder(new SpringSecurityPasswordEncoder(NoOpPasswordEncoder.getInstance())) // TODO put real encoder here
    bind(classOf[DbProfileService]).toInstance(dbProfileService) // Expose dbProfileService for use in controllers

    // ====== Pac4j Clients ======
//    val formClient = new FormClient(baseUrl + "/auth/loginForm", new SimpleTestUsernamePasswordAuthenticator())
    val formClient = new FormClient(baseUrl + "/auth/loginForm", dbProfileService)

    // OpenID Connect in this case Google
    val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId(googleClientId)
    oidcConfiguration.setSecret(googleSecret)
    oidcConfiguration.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration")
    oidcConfiguration.addCustomParam("prompt", "consent")
    val oidcClient = new OidcClient[OidcProfile](oidcConfiguration)
    oidcClient.addAuthorizationGenerator(new RoleAdminAuthGenerator)
//    oidcClient.setAuthenticator(dbProfileService) TODO how to store google logins in db for access

    val redirectUnauthenticatedClient = new RedirectUnauthenticatedClient("/auth/signIn")

    // HTTP - this is only used in testing
    val indirectBasicAuthClient = new IndirectBasicAuthClient(new AuthenticateInTestModeAuthenticator(configuration.getBoolean("testAuth").get ))

    val clients = new Clients(baseUrl + "/callback", formClient, oidcClient, redirectUnauthenticatedClient, indirectBasicAuthClient)

    val config = new Config(clients)
    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer[Nothing]("ROLE_ADMIN"))
    config.addAuthorizer("custom", new CustomAuthorizer)
    config.setHttpActionAdapter(new DemoHttpActionAdapter())


    bind(classOf[Config]).toInstance(config)

    // ====== Pac4j Support ======
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