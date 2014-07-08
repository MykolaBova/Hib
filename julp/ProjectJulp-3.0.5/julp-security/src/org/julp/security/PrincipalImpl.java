package org.julp.security;

import java.security.Principal;

public class PrincipalImpl implements java.security.Principal, java.io.Serializable {

    private static final long serialVersionUID = -1787876894018210542L;

    private String name;

    public PrincipalImpl() {
    }

    public PrincipalImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Principal)) {
            return false;
        }
        String anotherName = ((Principal) obj).getName();
        boolean equals = false;
        if (name == null) {
            equals = anotherName == null;
        } else {
            equals = name.equals(anotherName);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return (name == null ? -1 : name.hashCode());
    }

    @Override
    public String toString() {
        return name;
    }
}
