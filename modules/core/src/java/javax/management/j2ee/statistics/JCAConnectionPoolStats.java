package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JCAConnectionPoolStats extends JCAConnectionStats {
    public CountStatistic getCloseCount();

    public CountStatistic getCreateCount();

    public BoundedRangeStatistic getFreePoolSize();

    public BoundedRangeStatistic getPoolSize();

    public RangeStatistic getWaitingThreadCount();
}