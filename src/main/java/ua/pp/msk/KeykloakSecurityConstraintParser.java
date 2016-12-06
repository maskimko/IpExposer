/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk;
import org.keycloak.AuthorizationContext;
import org.keycloak.KeycloakSecurityContext;

/**
 *
 * @author maskimko
 */
public class KeykloakSecurityConstraintParser {
    private final KeycloakSecurityContext context;

    public KeykloakSecurityConstraintParser(KeycloakSecurityContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Keycloak Security Context:\n");
        sb.append("\tID Token: ");
        sb.append(context.getIdTokenString()).append("\n");
        sb.append("\tRealm: ");
        sb.append(context.getRealm()).append("\n");
        sb.append("\tToken: ");
        sb.append(context.getTokenString()).append("\n");
        AuthorizationContext authorizationContext = context.getAuthorizationContext();
        sb.append(authorizationContext.toString().replaceAll("\t", "\t\t"));
        return sb.toString();
    }
    
    
    public String toHtmlString(){
        return toString().replaceAll("\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
    }
    
    
}
