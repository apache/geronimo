package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface Statistic {
    public String getName();

    public String getUnit();

    public String getDescription();

    public long getStartTime();

    public long getLastSampleTime();
}