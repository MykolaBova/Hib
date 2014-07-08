package org.julp;

import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;

public class CGLibNamingPolicy implements NamingPolicy {

    private String proxyClassNameSuffix = "Proxy";

    public CGLibNamingPolicy() {
        
    }    
    
    public String getProxyClassNameSuffix() {
        return proxyClassNameSuffix;
    }

    public void setProxyClassNameSuffix(String proxyClassNameSuffix) {
        this.proxyClassNameSuffix = proxyClassNameSuffix;
    }    
    
    @Override
    public String getClassName(String origClassName, String source, Object key, Predicate names) {
        return origClassName + proxyClassNameSuffix;
    }
    
}
