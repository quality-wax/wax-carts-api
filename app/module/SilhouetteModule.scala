package module

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.actions.{SecuredErrorHandler, UnsecuredErrorHandler}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.{AuthInfoRepository, AuthenticatorRepository}
import com.mohiva.play.silhouette.api.services.{AuthenticatorService, AvatarService}
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.oauth2.{FacebookProvider, GoogleProvider, LinkedInProvider}
import com.mohiva.play.silhouette.impl.providers.state.{ CsrfStateItemHandler, CsrfStateSettings }
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{CookieSecretProvider, CookieSecretSettings}
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.services.GravatarService
import com.mohiva.play.silhouette.impl.util.{PlayCacheLayer, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import dao.{OAuth2InfoDAOImpl, PasswordInfoDAOImpl}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.modules.reactivemongo.ReactiveMongoApi
import repository.AuthenticatorRepositoryImpl
import service.{UserService, UserServiceImpl}
import utils.auth.{CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv}

import scala.concurrent.ExecutionContext.Implicits.global

class SilhouetteModule extends AbstractModule with ScalaModule {

  override def configure() = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
    bind[UserService].to[UserServiceImpl]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAOImpl]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAOImpl]
    bind[AuthenticatorRepository[JWTAuthenticator]].to[AuthenticatorRepositoryImpl]
  }

  /**
    * Provides the HTTP layer implementation.
    *
    * @param client Play's WS client.
    * @return The HTTP layer implementation.
    */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
    * Provides the Silhouette environment.
    *
    * @param userService          The user service implementation.
    * @param authenticatorService The authentication service implementation.
    * @param eventBus             The event bus instance.
    * @return The Silhouette environment.
    */
  @Provides
  def provideEnvironment(userService: UserService,
                         authenticatorService: AuthenticatorService[JWTAuthenticator],
                         eventBus: EventBus): Environment[DefaultEnv] =
    Environment[DefaultEnv](userService, authenticatorService, Seq(), eventBus)

  /**
    * Provides the social provider registry.
    *
    * @param facebookProvider The Facebook provider implementation.
    * @param googleProvider The Google provider implementation.
    * @param linkedInProvider The LinkedIn provider implementation.
    * @return The Silhouette environment.
    */
  @Provides
  def provideSocialProviderRegistry(
                                     facebookProvider: FacebookProvider,
                                     googleProvider: GoogleProvider,
                                     linkedInProvider: LinkedInProvider): SocialProviderRegistry = {

    SocialProviderRegistry(Seq(
      googleProvider,
      facebookProvider,
      linkedInProvider
    ))
  }

  /**
    * Provides the cookie signer for the OAuth1 token secret provider.
    *
    * @param configuration The Play configuration.
    * @return The cookie signer for the OAuth1 token secret provider.
    */
  @Provides @Named("oauth1-token-secret-signer")
  def provideOAuth1TokenSecretSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.oauth1TokenSecretProvider.signer")

    new JcaSigner(config)
  }

  /**
    * Provides the crypter for the OAuth1 token secret provider.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the OAuth1 token secret provider.
    */
  @Provides @Named("oauth1-token-secret-crypter")
  def provideOAuth1TokenSecretCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

    new JcaCrypter(config)
  }

  /**
    * Provides the crypter for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the authenticator.
    */
  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
    * Provides the auth info repository.
    *
    * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
    * @return The auth info repository instance.
    */
  @Provides
  def provideAuthInfoRepository(
                                 passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
                                 oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]): AuthInfoRepository = {

    new DelegableAuthInfoRepository(passwordInfoDAO, oauth2InfoDAO)
  }


  /**
    * Provides the signer for the CSRF state item handler.
    *
    * @param configuration The Play configuration.
    * @return The signer for the CSRF state item handler.
    */
  @Provides @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.csrfStateItemHandler.signer")

    new JcaSigner(config)
  }

  /**
    * Provides the signer for the social state handler.
    *
    * @param configuration The Play configuration.
    * @return The signer for the social state handler.
    */
  @Provides @Named("social-state-signer")
  def provideSocialStateSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.socialStateHandler.signer")

    new JcaSigner(config)
  }

  /**
    * Provides the signer for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The signer for the authenticator.
    */
  @Provides @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")

    new JcaSigner(config)
  }


  /**
    * Provides the authenticator service.
    *
    * @param crypter              The crypter implementation.
    * @param idGenerator          The ID generator implementation.
    * @param configuration        The Play configuration.
    * @param clock                The clock instance.
    * @return The authenticator service.
    */
  @Provides
  def provideAuthenticatorService(@Named("authenticator-crypter") crypter: Crypter,
                                  idGenerator: IDGenerator,
                                  configuration: Configuration,
                                  clock: Clock,
                                  reactiveMongoApi: ReactiveMongoApi): AuthenticatorService[JWTAuthenticator] = {
    val settings = JWTAuthenticatorSettings(sharedSecret = configuration.get[String]("play.http.secret.key"))
    val encoder = new CrypterAuthenticatorEncoder(crypter)
    val authenticatorRepository = new AuthenticatorRepositoryImpl(reactiveMongoApi)

    new JWTAuthenticatorService(settings, Some(authenticatorRepository), encoder, idGenerator, clock)
  }

  /**
    * Provides the OAuth1 token secret provider.
    *
    * @param signer The signer implementation.
    * @param crypter The crypter implementation.
    * @param configuration The Play configuration.
    * @param clock The clock instance.
    * @return The OAuth1 token secret provider implementation.
    */
  @Provides
  def provideOAuth1TokenSecretProvider(
                                        @Named("oauth1-token-secret-signer") signer: Signer,
                                        @Named("oauth1-token-secret-crypter") crypter: Crypter,
                                        configuration: Configuration,
                                        clock: Clock): OAuth1TokenSecretProvider = {

    val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
    new CookieSecretProvider(settings, signer, crypter, clock)
  }

  /**
    * Provides the CSRF state item handler.
    *
    * @param idGenerator The ID generator implementation.
    * @param signer The signer implementation.
    * @param configuration The Play configuration.
    * @return The CSRF state item implementation.
    */
  @Provides
  def provideCsrfStateItemHandler(
                                   idGenerator: IDGenerator,
                                   @Named("csrf-state-item-signer") signer: Signer,
                                   configuration: Configuration): CsrfStateItemHandler = {
    val settings = configuration.underlying.as[CsrfStateSettings]("silhouette.csrfStateItemHandler")
    new CsrfStateItemHandler(settings, idGenerator, signer)
  }

  /**
    * Provides the social state handler.
    *
    * @param signer The signer implementation.
    * @return The social state handler implementation.
    */
  @Provides
  def provideSocialStateHandler(
                                 @Named("social-state-signer") signer: Signer,
                                 csrfStateItemHandler: CsrfStateItemHandler): SocialStateHandler = {

    new DefaultSocialStateHandler(Set(csrfStateItemHandler), signer)
  }

  /**
    * Provides the password hasher registry.
    *
    * @return The password hasher registry.
    */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  /**
    * Provides the avatar service.
    *
    * @param httpLayer The HTTP layer implementation.
    * @return The avatar service implementation.
    */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)


  /**
    * Provides the Facebook provider.
    *
    * @param httpLayer The HTTP layer implementation.
    * @param socialStateHandler The social state handler implementation.
    * @param configuration The Play configuration.
    * @return The Facebook provider.
    */
  @Provides
  def provideFacebookProvider(
                               httpLayer: HTTPLayer,
                               socialStateHandler: SocialStateHandler,
                               configuration: Configuration): FacebookProvider = {

    new FacebookProvider(httpLayer, socialStateHandler, configuration.underlying.as[OAuth2Settings]("silhouette.facebook"))
  }

  /**
    * Provides the Google provider.
    *
    * @param httpLayer The HTTP layer implementation.
    * @param socialStateHandler The social state handler implementation.
    * @param configuration The Play configuration.
    * @return The Google provider.
    */
  @Provides
  def provideGoogleProvider(
                             httpLayer: HTTPLayer,
                             socialStateHandler: SocialStateHandler,
                             configuration: Configuration): GoogleProvider = {

    new GoogleProvider(httpLayer, socialStateHandler, configuration.underlying.as[OAuth2Settings]("silhouette.google"))
  }

  /**
    * Provides the Linkedin provider.
    *
    * @param httpLayer The HTTP layer implementation.
    * @param socialStateHandler The social state handler implementation.
    * @param configuration The Play configuration.
    * @return The Linkedin provider.
    */
  @Provides
  def provideLinkedInProvider(
                             httpLayer: HTTPLayer,
                             socialStateHandler: SocialStateHandler,
                             configuration: Configuration): LinkedInProvider = {

    new LinkedInProvider(httpLayer, socialStateHandler, configuration.underlying.as[OAuth2Settings]("silhouette.linkedin"))
  }

}
