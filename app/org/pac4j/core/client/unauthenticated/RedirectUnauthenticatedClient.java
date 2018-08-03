package org.pac4j.core.client.unauthenticated;

import com.google.common.collect.ImmutableMap;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.AnonymousCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.callback.QueryParameterCallbackUrlResolver;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.core.redirect.RedirectActionBuilder;
import org.pac4j.core.util.CommonHelper;

/**
 * Unauthenticated users are redirected to login options page
 */
public class RedirectUnauthenticatedClient extends IndirectClient<AnonymousCredentials, CommonProfile> {
    private final String url;

    public RedirectUnauthenticatedClient(String url){
//        String newURL = CommonHelper.addParameter(url, Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, RedirectUnauthenticatedClient.class.getSimpleName());
        this.url = url;
    }

    @Override
    public RedirectAction getRedirectAction(WebContext context) throws HttpAction {
        return RedirectAction.redirect(url);
    }

//    @Override
//    protected void clientInit(WebContext context) {
//        // NOOP no initialization is needed we just redirect
//    }

    @Override
    protected void clientInit() {

//        ImmutableMap.of(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, RedirectUnauthenticatedClient.class.getSimpleName());
//        setCallbackUrlResolver(new QueryParameterCallbackUrlResolver(
//                ImmutableMap.of(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, RedirectUnauthenticatedClient.class.getSimpleName())
//        ));

        setRedirectActionBuilder(new RedirectActionBuilder() {
            @Override
            public RedirectAction redirect(WebContext context) {
                return RedirectAction.redirect(url);
            }
        });

        setCredentialsExtractor(new CredentialsExtractor<AnonymousCredentials>() {
            @Override
            public AnonymousCredentials extract(WebContext context) {
                return AnonymousCredentials.INSTANCE;
            }
        });

        setAuthenticator(new Authenticator<AnonymousCredentials>() {
            @Override
            public void validate(AnonymousCredentials credentials, WebContext context) {
                // NOOP
            }
        });
    }

}
