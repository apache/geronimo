package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JDBCStats extends Stats {
    public JDBCConnectionStats[] getConnections();

    public JDBCConnectionPoolStats[] getConnectionPools();
}