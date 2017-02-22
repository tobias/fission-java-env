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

package io.fission.api.impl;

import java.nio.charset.Charset;

import io.fission.api.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ContextImpl implements Context {
    public ContextImpl(final RoutingContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public Vertx vertx() {
        return this.delegate.vertx();
    }

    @Override
    public HttpServerRequest request() {
        return this.delegate.request();
    }

    @Override
    public HttpServerResponse response() {
        return this.delegate.response();
    }

    @Override
    public void fail(Throwable t) {
        fail(t, 500);
    }

    @Override
    public void fail(Throwable t, int status) {
        // TODO: include stack trace?
        t.printStackTrace();
        response().setStatusCode(status).end(t.getMessage());
    }

    @Override
    public Context setStatusCode(int code) {
        response().setStatusCode(code);

        return this;
    }

    @Override
    public Context setContentType(String type) {
        response().putHeader("Content-Type", type);

        return this;
    }

    @Override
    public void end() {
        response().setStatusCode(200).end();
    }

    @Override
    public void end(String out) {
        response().setStatusCode(200).end(out);
    }

    @Override
    public void end(String out, Charset enc) {
        response().setStatusCode(200).end(out, enc.name());
    }

    @Override
    public void end(JsonArray out) {
        //TODO: set content-type
        end(out.encode());
    }

    @Override
    public void end(JsonObject out) {
        //TODO: set content-type
        end(out.encode());
    }

    private final RoutingContext delegate;
}
