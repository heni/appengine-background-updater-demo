package app.aifactory.ai;

import app.aifactory.ai.background.BackgroundUpdateTask;
import app.aifactory.ai.web.DemoServlet;

public class DemoLoopCounterUpdateTask implements BackgroundUpdateTask {
    private final DemoServlet demoServlet;
    private long counter = 0;

    public DemoLoopCounterUpdateTask(DemoServlet demoServlet) {
        this.demoServlet = demoServlet;
    }

    @Override
    public void doUpdate() throws Exception {
        ++counter;
        demoServlet.updateLoopCounter(counter);
    }
}
