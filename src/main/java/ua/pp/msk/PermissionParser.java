/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk;

import java.util.Arrays;
import java.util.Set;
import org.keycloak.representations.idm.authorization.Permission;

/**
 *
 * @author maskimko
 */
public class PermissionParser {
    private final Permission perm;

    public PermissionParser(Permission perm) {
        this.perm = perm;
    }

    @Override
    public String toString() {
        Set<String> sc = perm.getScopes();
        String scopes = "none";
        if (sc != null && !sc.isEmpty()){
            scopes = Arrays.toString(sc.toArray());
        }
        return "Permission\n\tresource set id: " + perm.getResourceSetId() + "\n\tresource set name: " + perm.getResourceSetName() + "\n\tscopes:" + scopes;
    }
    
    
}
