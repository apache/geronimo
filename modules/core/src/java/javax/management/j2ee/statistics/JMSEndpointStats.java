package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JMSEndpointStats extends Stats {
    public CountStatistic getMessageCount();

    public CountStatistic getPendingMessageCount();

    public CountStatistic getExpiredMessageCount();

    public TimeStatistic getMessageWaitTime();
}