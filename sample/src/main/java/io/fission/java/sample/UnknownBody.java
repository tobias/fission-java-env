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

package io.fission.java.sample;

import io.fission.api.Context;
import io.fission.api.Function;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UnknownBody implements Function<Void> {
    @Override
    public void call(Void input, Context context) {
        // this should probably be a convenience method on the Context
        final String contentType = context.request().getHeader("Content-Type");

        // bodyHandler will get called when the body is fully read.
        // There are other methods for reading the body in chunks as it comes in
        context.request().bodyHandler(body -> {

            switch (contentType) {
                case "text/plain":
                    final String str = body.toString();
                    // do something with the data
                    break;
                case "application/json":
                    try {
                        final JsonObject json = new JsonObject(body.toString());
                        // do something with the data
                    } catch (DecodeException ex) {
                        final JsonArray json = new JsonArray(body.toString());
                        // do something with the data
                    }
                    break;
                default:
                    context.setStatusCode(400).end("Unexpected content-type " + contentType);
                    return;
            }

            context.end("ok");
        });
    }
}
