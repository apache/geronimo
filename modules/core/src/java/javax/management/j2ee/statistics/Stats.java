package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface Stats {
    public Statistic getStatistic(String statisticName);

    public String[] getStatisticNames();

    public Statistic[] getStatistics();
}