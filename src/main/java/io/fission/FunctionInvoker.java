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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.fission.api.Context;
import io.fission.api.Function;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class FunctionInvoker implements Handler<RoutingContext> {
    FunctionInvoker(final Function fn, final Class input) {
        this.fn = fn;
        this.inputType = input;
    }

    @Override
    public void handle(RoutingContext ctx) {
        final long start = System.currentTimeMillis();
        final Context fCtx = Context.from(ctx);
        final BodyReader reader = legalInputTypes.get(this.inputType);
        final Object input;

        try {
            input = reader.read(ctx);
        } catch (Exception e) {
            fCtx.fail(e, 400);

            return;
        }
        try {
            this.fn.call(input, fCtx);
        } catch (Exception e) {
            fCtx.fail(e);
        }
        System.out.printf("user code called in %d ms\n", System.currentTimeMillis() - start);
    }

    boolean hasVoidInput() {
        return this.inputType.equals(Void.class);
    }

    static FunctionInvoker of(final Function fn) throws InvalidFunctionException {
        final Method[] methods = fn.getClass().getMethods();
        Class inputType = null;
        for (final Method m : methods) {
            Class[] paramTypes = m.getParameterTypes();
            if ("call".equals(m.getName()) &&
                    paramTypes.length == 2 &&
                    paramTypes[1].equals(Context.class)) {
                inputType = paramTypes[0];
                break;
            }
        }

        if (inputType == null) {
            // shouldn't happen, since clazz must already extend Function
            throw new InvalidFunctionException(String.format("Class %s is missing a call method", fn.getClass().getName()));
        }

        validateInputType(inputType);

        return new FunctionInvoker(fn, inputType);
    }

    private static final Map<Class, BodyReader> legalInputTypes = new HashMap<Class, BodyReader>() {{
        put(Void.class,       c -> null);
        put(String.class,     RoutingContext::getBodyAsString);
        put(JsonArray.class,  RoutingContext::getBodyAsJsonArray);
        put(JsonObject.class, RoutingContext::getBodyAsJson);
        //TODO: handle File, etc?
    }};

    private static void validateInputType(final Class t) throws InvalidFunctionException {
        for (final Class c : legalInputTypes.keySet()) {
            if (c.isAssignableFrom(t)) {
                return;
            }
        }

        throw new InvalidFunctionException(String.format("%s isn't a legal input type", t.getName()));
    }



    private final Class inputType;
    private final Function fn;

    interface BodyReader {
        Object read(RoutingContext ctx);
    }
}
