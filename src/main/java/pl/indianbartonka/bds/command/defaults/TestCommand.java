package pl.indianbartonka.bds.command.defaults;

import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.util.ServerUtil;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", " tescik");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) return false;

//        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
//        final long[] threadIds = threadMXBean.getAllThreadIds();
//
//        new ThreadUtil("Nygger").newThread(() -> {
//
//            Thread.currentThread().interrupt();
//
//            final ThreadInfo threadInfo = threadMXBean.getThreadInfo(Thread.currentThread().getId());
//            this.sendMessage("Wątek ID: " + threadInfo.getThreadId());
//            this.sendMessage("Nazwa: " + threadInfo.getThreadName());
//            this.sendMessage("Stan: " + threadInfo.getThreadState());
//            this.sendMessage("Demon: " + threadInfo.isDaemon());
//            this.sendMessage("Jest zawieszony: " + threadInfo.isSuspended());
//            this.sendMessage("Jest przerwany: " + Thread.currentThread().isInterrupted());
//
//        }).start();
//
//        for (final Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
//            final Thread thread = entry.getKey();
//            final ThreadInfo threadInfo = threadMXBean.getThreadInfo(thread.getId());
//
//            this.sendMessage("Wątek ID: " + thread.getId());
//            this.sendMessage("Nazwa: " + thread.getName());
//            this.sendMessage("Stan: " + thread.getState());
//            this.sendMessage("Demon: " + thread.isDaemon());
//            this.sendMessage("Jest zawieszony: " + thread.isInterrupted());
//            this.sendMessage("Jest aktywny: " + thread.isAlive());
//
//            if (threadInfo != null) {
//                this.sendMessage("Jest natywny: " + threadInfo.isInNative());
//            }
//
////            this.sendMessage("Jest zawieszony: " + thread.);
//            this.sendMessage("------------------------");
//        }

        this.sendMessage(ServerUtil.sendCommandAsPlayer(this.player.getPlayerName(), "say @s chuj"));
        this.sendMessage(ServerUtil.sendCommandAsPlayer(this.player.getPlayerName(), "kill"));

        return false;
    }
}
