package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface RangeStatistic extends Statistic {
    public long getHighWaterMark();

    public long getLowWaterMark();

    public long getCurrent();
}