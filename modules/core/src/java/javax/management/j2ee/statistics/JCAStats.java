package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JCAStats extends Stats {
    public JCAConnectionStats[] getConnections();

    public JCAConnectionPoolStats[] getConnectionPools();
}