package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface TimeStatistic extends Statistic {
    public long getCount();

    public long getMaxTime();

    public long getMinTime();

    public long getTotalTime();
}