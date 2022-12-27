package mods.thecomputerizer.musictriggers.util;

import com.google.common.util.concurrent.ListenableFuture;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import org.apache.logging.log4j.Level;

import java.util.concurrent.ExecutionException;

public class ASyncUtil {

    public static boolean IS_DISPLAY_FOCUSED = false;

    public static void queueDisplayCheck(ListenableFuture<Boolean> future) {
        try {
            IS_DISPLAY_FOCUSED = future.get();
        } catch (InterruptedException | ExecutionException ex) {
            MusicTriggers.logExternally(Level.WARN,"Display status failed to sync from main thread {}",ex);
            Constants.MAIN_LOG.warn(ex);
        }
    }
}
