package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface BoundaryStatistic extends Statistic {
    public long getUpperBound();

    public long getLowerBound();
}
