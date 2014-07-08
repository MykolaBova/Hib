package org.julp.security;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public abstract class AbstarctUsernamePasswordLoginModule implements LoginModule {

    private Callback[] callbacks = new Callback[3];
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;    
    private boolean ok = false;
    private boolean commitSucceeded = false;
    private PrincipalImpl principal;
    private boolean validate = true;
    private boolean useFirstPass;
    private boolean storePassword;
    private final transient Logger logger = Logger.getLogger(getClass().getName());
//    private boolean initialized;

    public AbstarctUsernamePasswordLoginModule() {
    }

    @Override
    public boolean commit() throws javax.security.auth.login.LoginException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] commit()");
        }

        if (ok == false) {
            return false;
        } else {
            // add a Principal (authenticated identity) to the Subject
            principal = new PrincipalImpl(((NameCallback) callbacks[0]).getName());
            if (!subject.getPrincipals().contains(principal)) {
                subject.getPrincipals().add(principal);
            }

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("\t[" + getClass().getName() + "] " + "added Principal " + principal + " to Subject");
            }
            if (storePassword) {
                PasswordCredential ps = new PasswordCredential(((PasswordCallback) callbacks[1]).getPassword());
                if (!subject.getPrivateCredentials().contains(ps)) {
                    subject.getPrivateCredentials().add(ps);
                }
            }
            // in any case, clean out state???
            //((NameCallback) callbacks[0]).setName(null);
            //((PasswordCallback) callbacks[1]).clearPassword();
            commitSucceeded = true;
            return commitSucceeded;
        }
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        //debug = "true".equalsIgnoreCase((String) options.get("debug"));
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] initialize()");
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] "
                    + "\n\t\tsubject:" + subject + "\n\t\tCallbackHandler: " + callbackHandler
                    + "\n\t\tsharedState: " + sharedState + "\n\t\toptions: " + options);
        }

        this.validate = "true".equalsIgnoreCase((String) options.get("validate"));
        this.useFirstPass = "true".equalsIgnoreCase((String) options.get("try_first_pass"));
        this.storePassword = "true".equalsIgnoreCase((String) options.get("store_password"));
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        try {
            callbacks[0] = new NameCallback("Name:");
            callbacks[1] = new PasswordCallback("Password:", true);
            callbacks[2] = new TextOutputCallback(TextOutputCallback.INFORMATION, "Please login...");

//            try{
//                callbackHandler.handle(callbacks);
//            }catch (Exception e){
//                e.printStackTrace();
//                throw new LoginException(e.getMessage());
//            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean login() throws javax.security.auth.login.LoginException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] login()");
        }
//        if (!initialized){
//            initialized = true;
//            ok = true;
//            return ok;
//        }

        try {
            if (useFirstPass && !sharedState.isEmpty()) {
                String name = (String) sharedState.get("javax.security.auth.login.name");
                char[] password = (sharedState.get("javax.security.auth.login.password") == null ? new char[0] : (char[]) sharedState.get("javax.security.auth.login.password"));
                if (validate) {
                    validate();
                }
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("\t[" + getClass().getName() + "] authenticate()");
                }
                ok = authenticate(name, password);
                return ok;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // continue...
        }


        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LoginException(e.getMessage());
        }
        sharedState.put("javax.security.auth.login.name", ((NameCallback) callbacks[0]).getName());
        sharedState.put("javax.security.auth.login.password", ((PasswordCallback) callbacks[1]).getPassword());
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] sharedState: " + sharedState);
        }
        if (validate) {
            validate();
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] authenticate()");
        }
        ok = authenticate(((NameCallback) callbacks[0]).getName(), ((PasswordCallback) callbacks[1]).getPassword());
        return ok;
    }

    @Override
    public boolean logout() throws javax.security.auth.login.LoginException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] logout()");
        }
        subject.getPrincipals().remove(principal);
        ok = false;
        ok = commitSucceeded;
        ((NameCallback) callbacks[0]).setName(null);
        ((PasswordCallback) callbacks[1]).clearPassword();
        principal = null;
        return true;
    }

    private void validate() throws LoginException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] validate()");
        }

        if (useFirstPass && !sharedState.isEmpty()) {
            Object name = sharedState.get("javax.security.auth.login.name");
            if (name == null || name.toString().trim().length() == 0) {
                throw new AccountException("User Name is missing");
            }
            Object pass = sharedState.get("javax.security.auth.login.password");
            if (pass == null || ((char[]) pass).length == 0) {
                throw new CredentialException("Password is missing");
            }
        } else {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof javax.security.auth.callback.NameCallback) {
                    String name = ((NameCallback) callbacks[i]).getName();
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("\t[" + getClass().getName() + "] NameCallback: " + name);
                    }
                    if (name == null || name.trim().length() == 0) {
                        throw new AccountException("User Name is missing");
                    }
                } else if (callbacks[i] instanceof javax.security.auth.callback.PasswordCallback) {
                    char[] pass = ((PasswordCallback) callbacks[i]).getPassword();
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("\t[" + getClass().getName() + "] PasswordCallback: " + (pass == null ? "" : /*new String(pass)*/ "Nooo... can't show..."));
                    }
                    if (pass == null || pass.length == 0) {
                        throw new CredentialException("Password is missing");
                    }
                }
            }
        }
    }

    protected abstract boolean authenticate(String name, char[] password) throws LoginException;

    @Override
    public boolean abort() throws javax.security.auth.login.LoginException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] abort()");
        }
        if (ok == false) {
            return false;
        } else if (ok == true && commitSucceeded == false) {
            // login ok but overall authentication failed
            ok = false;
            ((NameCallback) callbacks[0]).setName(null);
            ((PasswordCallback) callbacks[1]).clearPassword();
            principal = null;
        } else {
            // overall authentication ok and commit ok, but someone else's commit failed?
            logout();
        }
        return true;
    }
}
