package org.apache.geronimo.security.jacc;

import javax.security.jacc.PolicyContextException;
import java.util.Set;

/**
 */
public interface PrincipalRoleMapper {
    void install(Set contextIds) throws PolicyContextException;

    void uninstall() throws PolicyContextException;
}
