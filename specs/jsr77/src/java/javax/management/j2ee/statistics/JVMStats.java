package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface JVMStats extends Stats {
    public CountStatistic getUpTime();

    public BoundedRangeStatistic getHeapSize();
}