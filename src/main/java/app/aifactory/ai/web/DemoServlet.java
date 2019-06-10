/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.aifactory.ai.web;

import app.aifactory.ai.DemoLoopCounterUpdateTask;
import app.aifactory.ai.background.BackgroundUpdater;
import com.google.inject.Singleton;

import javax.inject.Inject;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class DemoServlet extends HttpServlet {
    private AtomicLong counter = new AtomicLong(0);

    @Inject
    public DemoServlet(BackgroundUpdater backgroundUpdater) {
        DemoLoopCounterUpdateTask task = new DemoLoopCounterUpdateTask(this);
        backgroundUpdater.registerTask(task, 10000);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println(
                "{ \"name\": \"World\", \"counter\": "
                + counter.get()
                + " }"
        );
    }

    public void updateLoopCounter(long counter) {
        this.counter.set(counter);
    }
}
