package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface StatefulSessionBeanStats extends SessionBeanStats {
    public RangeStatistic getPassiveCount();
}