package org.julp.security;

import javax.security.auth.login.LoginException;

public class UsernamePasswordLoginModule extends AbstarctUsernamePasswordLoginModule {

    public UsernamePasswordLoginModule() {
    }

    @Override
    protected boolean authenticate(String name, char[] password) throws LoginException {
        try {
            if (1 == 2) {
                throw new LoginException("TEST-TEST-TEST");
            }
        } catch (LoginException e) {
            throw new LoginException(e.getMessage());
        }
        return true;
    }
}
