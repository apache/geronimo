package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JMSStats extends Stats {
    public JMSConnectionStats[] getConnections();
}