package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface EntityBeanStats extends EJBStats {
    public RangeStatistic getReadyCount();

    public RangeStatistic getPooledCount();
}