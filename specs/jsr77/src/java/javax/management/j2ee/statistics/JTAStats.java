package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JTAStats extends Stats {
    public CountStatistic getActiveCount();

    public CountStatistic getCommittedCount();

    public CountStatistic getRolledbackCount();
}