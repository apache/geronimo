package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JMSSessionStats extends Stats {
    public JMSProducerStats[] getProducers();

    public JMSConsumerStats[] getConsumers();

    public CountStatistic getMessageCount();

    public CountStatistic getPendingMessageCount();

    public CountStatistic getExpiredMessageCount();

    public TimeStatistic getMessageWaitTime();

    public CountStatistic getDurableSubscriptionCount();
}