package app.aifactory.ai.context;

import app.aifactory.ai.web.DemoServletModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class DemoGuiceServletContextListener extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new DemoServletModule());
    }
}
