package org.apache.geronimo.j2ee.deployment.j2eeobjectnames;

/**
 */
public interface J2eeContext {

    String getJ2eeDomainName();

    String getJ2eeServerName();

    String getJ2eeApplicationName();

    String getJ2eeModuleName();

    String getJ2eeName();

    String getJ2eeType();

    //these override methods return the argument it if is non-null, otherwise the same value as
    //the corresponding method above.

    String getJ2eeDomainName(String override);

    String getJ2eeServerName(String override);

    String getJ2eeApplicationName(String override);

    String getJ2eeModuleName(String override);

    String getJ2eeName(String override);

    String getJ2eeType(String override);

}
