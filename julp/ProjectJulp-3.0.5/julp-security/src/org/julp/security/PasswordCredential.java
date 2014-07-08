package org.julp.security;

public class PasswordCredential extends PrivateCredential {

    private char[] password;

    public PasswordCredential() {
    }

    public PasswordCredential(char[] password) {
        this.password = password;
    }

    @Override
    public void destroy() throws javax.security.auth.DestroyFailedException {
        password = null;
    }

    @Override
    public boolean isCurrent() {
        return true;
    }

    @Override
    public boolean isDestroyed() {
        if (password == null || password.length == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void refresh() throws javax.security.auth.RefreshFailedException {
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj.getClass().equals(getClass()))) {
            return false;
        }
        if (getPassword() == null || ((PasswordCredential) obj).getPassword() == null) {
            return false;
        }
        if (new String(getPassword()).equals(new String(((PasswordCredential) obj).getPassword()))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (password == null) ? -1 : password.hashCode();
    }
}
