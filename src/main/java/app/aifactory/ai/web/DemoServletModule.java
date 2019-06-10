package app.aifactory.ai.web;

import app.aifactory.ai.background.BackgroundUpdater;
import com.google.appengine.api.ThreadManager;
import com.google.inject.servlet.ServletModule;

public class DemoServletModule extends ServletModule {
    private static Thread backgroundUpdaterThread;

    @Override
    protected void configureServlets() {
        BackgroundUpdater backgroundUpdater = new BackgroundUpdater();
        bind(BackgroundUpdater.class).toInstance(backgroundUpdater);

        serve("/demo").with(DemoServlet.class);

        backgroundUpdaterThread = ThreadManager.createBackgroundThread(backgroundUpdater);
        backgroundUpdaterThread.start();
    }

}
