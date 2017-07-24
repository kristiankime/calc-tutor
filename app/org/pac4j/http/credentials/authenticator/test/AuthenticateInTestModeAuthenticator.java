package org.pac4j.http.credentials.authenticator.test;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;

/**
 * This class is intended to allow authentication for testing
 *
 * This class is a simple test authenticator: password must match username.
 *
 */
public class AuthenticateInTestModeAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    private final boolean testMode;

    public AuthenticateInTestModeAuthenticator(boolean testMode) {
        this.testMode = testMode;
    }

    @Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) throws HttpAction, CredentialsException {
        if(!testMode) {
            throwsException("Not in testing mode this authentication method is turned off");
        }

        if (credentials == null) {
            throwsException("No credential");
        }
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (CommonHelper.isBlank(username)) {
            throwsException("Username cannot be blank");
        }
        if (CommonHelper.isBlank(password)) {
            throwsException("Password cannot be blank");
        }
        if (CommonHelper.areNotEquals(username, password)) {
            throwsException("Username : '" + username + "' does not match password");
        }
        final CommonProfile profile = new CommonProfile();
        profile.setId(username);
        profile.addAttribute(Pac4jConstants.USERNAME, username);
        credentials.setUserProfile(profile);
    }

    protected void throwsException(final String message) throws CredentialsException {
        throw new CredentialsException(message);
    }
}
