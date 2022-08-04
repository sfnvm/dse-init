package edu.sfnvm.dseinit.constant;

public class TimeMarkConstant {
    private TimeMarkConstant() {
    }

    /**
     * <ul>
     *   <li>24 Hours == 86400000 Milliseconds</li>
     *   <li>7 Hours == 25200000 Milliseconds</li>
     *   <li>1 Hour == 3600000 Milliseconds</li>
     * </ul>
     */
    public static final long NANOS_WITHIN_A_DAY = 25199998;
}
