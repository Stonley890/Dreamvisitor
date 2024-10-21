package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.comms.DataSender;

public class ScheduleRestart {

    private static boolean restartScheduled = false;

    public static void setRestartScheduled(boolean state) {
        restartScheduled = state;
        DataSender.sendRestartStatus();
    }

    public static boolean isRestartScheduled() {
        return restartScheduled;
    }

}
