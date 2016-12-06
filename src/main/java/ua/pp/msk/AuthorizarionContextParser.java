/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk;

import java.util.Iterator;
import org.keycloak.AuthorizationContext;
import org.keycloak.representations.idm.authorization.Permission;

/**
 *
 * @author maskimko
 */
public class AuthorizarionContextParser {

    private final AuthorizationContext context;

    public AuthorizarionContextParser(AuthorizationContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Authorization Context:\n");
        sb.append("\tGranted: ");
        sb.append(context.isGranted());
        if (context.getPermissions() != null && !context.getPermissions().isEmpty()) {
            sb.append("\tPermissions:\n");
            Iterator<Permission> pi = context.getPermissions().iterator();
            while (pi.hasNext()) {
                Permission p = pi.next();
               if (p!=null) sb.append("\t").append(p.toString().replaceAll("\t", "\t\t")).append("\n");
            }
        }
        return sb.toString();
    }
}
