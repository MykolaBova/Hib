package org.julp.security;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class UsernamePasswordCallbackHandler implements CallbackHandler {

    private String username;
    private char[] password;
    private boolean ignoreUnrecognizedCallback;

    public UsernamePasswordCallbackHandler(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    public UsernamePasswordCallbackHandler() {
    }

    @Override
    public void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException {
        //System.out.println("UsernamePasswordCallbackHandler::handle() callbacks 1: " + Arrays.asList(callbacks));
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                // System.out.println("UsernamePasswordCallbackHandler::handle() username: " + username);
                ((NameCallback) callbacks[i]).setName(username);
            } else if (callbacks[i] instanceof PasswordCallback) {
                //System.out.println("UsernamePasswordCallbackHandler::handle() password: " + password);
                ((PasswordCallback) callbacks[i]).setPassword(password);
            }
        }
    }

    public boolean isIgnoreUnrecognizedCallback() {
        return ignoreUnrecognizedCallback;
    }

    public void setIgnoreUnrecognizedCallback(boolean ignoreUnrecognizedCallback) {
        this.ignoreUnrecognizedCallback = ignoreUnrecognizedCallback;
    }

    /**
     * Clears out password state.
     */
    public void clearPassword() {
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }
}
