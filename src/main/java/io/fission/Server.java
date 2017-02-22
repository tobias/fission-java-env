/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package io.fission;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import io.fission.api.Function;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class Server {

    public Server(final URL artifactURL) {
        this.artifactPath = artifactURL;
    }

    void sendResponse(final RoutingContext ctx, final int code, final String msg) {
        ctx.response().setStatusCode(code).end(msg);
    }

    void specialize(final RoutingContext ctx) {
        if (this.function == null) {
            final long startTime = System.currentTimeMillis();
            final String className = ctx.request().params().get("className");
            if (className == null) {
                sendResponse(ctx, 400, "No className provided");
                return;
            }
            final URL[] urls = {this.artifactPath};
            final ClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());
            final Class clazz;
            try {
                clazz = loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                sendResponse(ctx, 400, "Class " + className + " not found");
                return;
            }
            try {
                final Object fn = clazz.newInstance();
                if (fn instanceof Function) {
                    this.function = FunctionInvoker.of((Function)fn);
                } else {
                    throw new InvalidFunctionException(String.format("Class %s isn't a Function", clazz.getName()));
                }
                System.out.printf("user code loaded in %d ms\n", System.currentTimeMillis() - startTime);
                sendResponse(ctx, 200, "ok");
            } catch (IllegalAccessException | InstantiationException | InvalidFunctionException e) {
                e.printStackTrace();
                // TODO: send back serialized stack trace?
                sendResponse(ctx, 500, e.getMessage());
            }
        } else {
            sendResponse(ctx, 400, "Not a generic container");
        }
    }

    private void handleRequest(final RoutingContext ctx) {
        if (this.function == null) {
            sendResponse(ctx, 500, "Generic container: no requests supported");
        } else {
            this.function.handle(ctx);
        }
    }

    private void start() {
        final Vertx vertx = Vertx.vertx();
        this.router = Router.router(vertx);
        // TODO: use system tmp
        final BodyHandler bodyHandler = BodyHandler.create("/tmp");

        router.post("/specialize")
                .handler(bodyHandler);
        router.post("/specialize")
                .handler(this::specialize);

        router.route("/")
                .handler(ctx -> {
                    if (this.function == null || this.function.hasVoidInput()) {
                        ctx.next();
                    } else {
                        // read the body of the request
                        bodyHandler.handle(ctx);
                    }
                });
        router.route("/")
                .handler(this::handleRequest);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080, __ -> System.out.println("listening on 8080"));
    }

    public static void main( String[] args ) throws Exception {
        final File f = new File(args[0]);
        assert(f.exists());
        new Server(f.toURI().toURL())
                .start();
    }

    final private URL artifactPath;
    private FunctionInvoker function = null;
    private Router router = null;
}
