package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface EJBStats extends Stats {
    public CountStatistic getCreateCount();

    public CountStatistic getRemoveCount();
}