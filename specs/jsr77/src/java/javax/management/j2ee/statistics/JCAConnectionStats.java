package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JCAConnectionStats extends Stats {
    public String getConnectionFactory();

    public String getManagedConnectionFactory();

    public TimeStatistic getWaitTime();

    public TimeStatistic getUseTime();
}