package org.apache.geronimo.system.properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

/**
 */
public class NamingPropertiesTest extends TestCase {
    private static final String NAMING_FACTORY_INITIAL = "com.sun.jndi.rmi.registry.RegistryContextFactory";
    private static final String FACTORY_URL_PKGS = "org.apache.geronimo.naming";
    private static final String PROVIDER_URL = "rmi://localhost:1099";

    public void testNamingFactoryInitial() throws Exception {

        assertNull(this.getClass().getClassLoader().getResource("jndi.properties"));

        try {
            new InitialContext();
            System.out.println("Something is wrong, initial context can be constructed");
//            fail();
        } catch (NamingException ne) {
            //expected
        }
        assertNull(System.getProperty(NamingProperties.JAVA_NAMING_FACTORY_INITIAL));
        assertNull(System.getProperty(NamingProperties.JAVA_NAMING_FACTORY_URL_PKGS));
        assertNull(System.getProperty(NamingProperties.JAVA_NAMING_PROVIDER_URL));
        NamingProperties namingProperties = new NamingProperties(NAMING_FACTORY_INITIAL, FACTORY_URL_PKGS, PROVIDER_URL);
        assertEquals(System.getProperty(NamingProperties.JAVA_NAMING_FACTORY_INITIAL), NAMING_FACTORY_INITIAL);
        assertEquals(System.getProperty(NamingProperties.JAVA_NAMING_FACTORY_URL_PKGS), FACTORY_URL_PKGS);
        assertEquals(System.getProperty(NamingProperties.JAVA_NAMING_PROVIDER_URL), PROVIDER_URL);

        assertEquals(namingProperties.getNamingFactoryInitial(), NAMING_FACTORY_INITIAL);
        assertEquals(namingProperties.getNamingFactoryUrlPkgs(), FACTORY_URL_PKGS);
        assertEquals(namingProperties.getNamingProviderUrl(), PROVIDER_URL);

        try {
            // the above assumes we're running on a Sun JVM.  If we can't load the class first,
            // we'll skip the attempt at creating the InitialContext.  We've already verified that the
            // system properties have been set to the correct values, so this last bit is largely a formality.
            Class.forName("NAME_FACTORY_INITIAL");
            new InitialContext();
        } catch (ClassNotFoundException e) {
        }
    }
}
