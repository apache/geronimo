package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface MessageDrivenBeanStats extends EJBStats {
    public CountStatistic getMessageCount();
}
