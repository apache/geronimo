package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JDBCConnectionStats extends Stats {
    public String getJdbcDataSource();

    public TimeStatistic getWaitTime();

    public TimeStatistic getUseTime();
}