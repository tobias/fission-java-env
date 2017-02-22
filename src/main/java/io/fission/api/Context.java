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

package io.fission.api;

import java.nio.charset.Charset;

import io.fission.api.impl.ContextImpl;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface Context {
    static Context from(final RoutingContext ctx) {
        return new ContextImpl(ctx);
    }

    Vertx vertx();

    HttpServerRequest request();

    HttpServerResponse response();

    void fail(Throwable t);

    void fail(Throwable t, int status);

    Context setStatusCode(int code);

    Context setContentType(String type);

    void end();

    void end(String out);

    void end(String out, Charset encoding);

    void end(JsonArray out);

    void end(JsonObject out);

//
//    Context setHeader(String header, String value);

    // TODO: cookies? header accessors? pathParams?

}
