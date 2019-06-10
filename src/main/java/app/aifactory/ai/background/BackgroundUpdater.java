package app.aifactory.ai.background;

import com.google.inject.Singleton;

import java.text.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@Singleton
public class BackgroundUpdater implements Runnable {

    private static final Logger log = Logger.getLogger(BackgroundUpdater.class.getName());
    private static final DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static long SIGNAL_MAIN_TIMEOUT = 1000;
    private final Lock lock;
    private final List<RegisteredBackgroundTask> registeredTasks;
    private final PriorityQueue<ScheduledBackgroundTask> scheduledBackgroundTasks;

    public BackgroundUpdater() {
        lock = new ReentrantLock();
        registeredTasks = new ArrayList<RegisteredBackgroundTask>();
        scheduledBackgroundTasks = new PriorityQueue<ScheduledBackgroundTask>();
    }

    @Override
    public void run() {
        //while (!LifecycleManager.getInstance().isShuttingDown()) {
        while (true) {
            try {
                long currentTime = new Date().getTime();
                long waitingTime = SIGNAL_MAIN_TIMEOUT;

                ScheduledBackgroundTask task = lookupNextTask();
                if (task != null) {
                    waitingTime = Math.max(0, task.scheduledTimeMillis - currentTime);
                }

                if (waitingTime > 0) {
                    synchronized (lock) {
                        lock.wait(waitingTime);
                    }
                    continue; //run it at next loop
                }

                task = getNextTask();
                if (task == null) {
                    continue;
                }
                log.info(MessageFormat.format(
                        "task {0} has been gotten for execution", task.toString()
                ));
                if (task.scheduledTimeMillis > currentTime) {
                    log.severe(MessageFormat.format(
                            "Logic error for BackgroundUpdater raised: scheduled time {0} is more then current time {1}",
                            task.scheduledTimeMillis,
                            currentTime
                    ));
                    continue; // run it at next loop
                }

                BackgroundUpdateTask worker = task.resisteredTask.updateWorker;
                try {
                    worker.doUpdate();
                } catch (Exception e) {
                    log.severe(MessageFormat.format(
                            "Error handled during {0} execution: {1}",
                            worker.getClass().getName(),
                            e.toString()
                    ));
                } finally {
                    scheduleTask(task.resisteredTask);
                }
            } catch (InterruptedException e) {
                log.warning("Signal waiting was interrupted");
            }
        }
    }

    public void registerTask(BackgroundUpdateTask task, long delayMillis) {
        RegisteredBackgroundTask registeredTask = new RegisteredBackgroundTask(task, delayMillis);
        registeredTasks.add(registeredTask);
        scheduleTask(registeredTask);
    }

    private ScheduledBackgroundTask lookupNextTask() {
        synchronized (lock) {
            return scheduledBackgroundTasks.peek();
        }
    }

    private ScheduledBackgroundTask getNextTask() {
        synchronized (lock) {
            return scheduledBackgroundTasks.poll();
        }
    }

    private void scheduleTask(RegisteredBackgroundTask task) {
        long startTime = new Date().getTime() + task.delayMillis;
        log.info(MessageFormat.format(
                "task {0} scheduled to be runned at {1}",
                task.toString(),
                logDateFormat.format(new Date(startTime))
        ));
        synchronized (lock) {
            scheduledBackgroundTasks.add(new ScheduledBackgroundTask(
                    task,
                    startTime)
            );
            lock.notify();
        }
    }

    static class RegisteredBackgroundTask {
        BackgroundUpdateTask updateWorker;
        long delayMillis;

        RegisteredBackgroundTask(BackgroundUpdateTask updateWorker, long delayMillis) {
            this.updateWorker = updateWorker;
            this.delayMillis = delayMillis;
        }
    }

    static class ScheduledBackgroundTask {
        RegisteredBackgroundTask resisteredTask;
        long scheduledTimeMillis;

        ScheduledBackgroundTask(RegisteredBackgroundTask resisteredTask, long scheduledTimeMillis) {
            this.resisteredTask = resisteredTask;
            this.scheduledTimeMillis = scheduledTimeMillis;
        }
    }

    static class ScheduledBackgroundTasksComparator implements Comparator<ScheduledBackgroundTask> {

        @Override
        public int compare(ScheduledBackgroundTask o1, ScheduledBackgroundTask o2) {
            return Long.signum(o1.scheduledTimeMillis - o2.scheduledTimeMillis);
        }
    }
}