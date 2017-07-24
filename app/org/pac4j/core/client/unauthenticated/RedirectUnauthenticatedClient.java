package org.pac4j.core.client.unauthenticated;

import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.AnonymousCredentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

/**
 * Unauthenticated users are redirected to login options page
 */
public class RedirectUnauthenticatedClient extends IndirectClient<AnonymousCredentials, CommonProfile> {
    private final String url;

    public RedirectUnauthenticatedClient(String url){
        this.url = url;
    }

    @Override
    public RedirectAction getRedirectAction(WebContext context) throws HttpAction {
        return RedirectAction.redirect(url);
    }

    @Override
    protected void clientInit(WebContext context) {
        // NOOP no initialization is needed we just redirect
    }

}
