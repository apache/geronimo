package example.ejb;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;

/**
 * This is a example of a CMP entity bean.
 * @ejb.bean
 *     name="Example"
 *     cmp-version="2.x"
 *     primkey-field="id"
 * @ejb.transaction
 *     type="Required"
 * @ejb.finder
 *     signature="Example findByName(java.lang.String name)"
 *     query="SELECT DISTINCT e.id FROM Example AS e WHERE e.name = ?1"
 *
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: ExampleBean.java,v 1.1 2004/03/07 00:21:19 evenisse Exp $
 */
public abstract class ExampleBean implements EntityBean {
    /**
     * The primary key of the table is a number which the developer has to
     * guarantee to be unique.
     * @ejb.pk-field
     * @ejb.interface-method
     * @ejb.persistence
     *
     * @return an integer representing an ID on the keys table
     */
    public abstract Integer getId();

    /**
     * This sets the primary key value.  Not an actual interface method, but
     * needed during bean creation.
     * @param id the new primary key value
     */
    public abstract void setId(final Integer id);

    /**
     * Name is a field value, you can change or add on more fields as needed.
     * @ejb.persistence
     * @ejb.interface-method
     * @return the name
     */
    public abstract String getName();

    /**
     * Sets the name field
     * @ejb.interface-method
     * @param name new name
     */
    public abstract void setName(final String name);

    /**
     * The required EJB Creation method
     * @ejb.create-method
     * @param id a unique ID for the primary key
     * @param name the value associated with the key
     * @throws CreateException thrown when there is a problem creating 
     * @return the primary key
     */
    public Integer ejbCreate(final Integer id, final String name)
        throws CreateException {
        setName(name);
        setId(id);

        return getId();
    }
}
