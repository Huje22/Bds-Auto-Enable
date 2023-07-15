package me.indian.bds.watchdog;

import me.indian.bds.util.ThreadUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchDog {

    private final ExecutorService service;

    public WatchDog() {
        this.service = Executors.newScheduledThreadPool(ThreadUtil.getThreadsCount(), new ThreadUtil("Watchdog "));


    }


}
