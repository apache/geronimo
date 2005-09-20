package example.ejb;

import org.apache.cactus.ServletTestCase;

/**
 * This tests the facade methods using the Cactus framework.
 *
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: SanityWithCactusTest.java,v 1.1 2004/03/07 00:21:19 evenisse Exp $
 */
public class SanityWithCactusTest extends ServletTestCase {

    public void testGetLocalHome() throws Exception {
        ExampleFacadeUtil.getLocalHome();
    }

    public void testGetFacade() throws Exception {
        ExampleFacadeUtil.getLocalHome().create();
    }

    public void testSetName() throws Exception {
        ExampleFacadeLocal facade = ExampleFacadeUtil.getLocalHome().create();
        Integer id = facade.getId("Foo");
        assertEquals("Foo", facade.getName(id));
        facade.setName(id,  "Bar");
        assertEquals(id,facade.getId("Bar"));
        assertEquals("Bar", facade.getName(id));
        facade.setName(id,  "Foo");
        assertEquals("Foo", facade.getName(id));
    }
}
