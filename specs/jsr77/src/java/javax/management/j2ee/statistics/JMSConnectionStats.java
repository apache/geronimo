package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JMSConnectionStats extends Stats {
    public JMSSessionStats[] getSessions();

    public boolean isTransactional();
}