export enum StateEnum {
    SLEEP = "SLEEP",
    RUN = "RUN",
    HOLD = "HOLD",
    JOG = "JOG",
    ALARM = "ALARM",
    HOME = "HOME",
    CHECK = "CHECK",
    IDLE = "IDLE",

    /**
     * When UGS can't determine the controller state
     */
    UNKNOWN = "UNKNOWN",

    /**
     * When not connected to a controller, but connected to the PendantAPI
     */
    DISCONNECTED = "DISCONNECTED",

    /**
     *  When not connected to the PendantAPI
     */
    UNAVAILABLE = "UNAVAILABLE"
}
