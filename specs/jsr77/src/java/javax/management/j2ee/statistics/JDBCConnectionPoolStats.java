package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JDBCConnectionPoolStats extends JDBCConnectionStats {
    public CountStatistic getCreateCount();

    public CountStatistic getCloseCount();

    public BoundedRangeStatistic getPoolSize();

    public BoundedRangeStatistic getFreePoolSize();

    public RangeStatistic getWaitingThreadCount();
}