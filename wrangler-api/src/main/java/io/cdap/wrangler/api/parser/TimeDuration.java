/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class TimeDuration implements Token {
    private final long millis;
    private final String original;

    public TimeDuration(String valueStr) {
        this.original = valueStr;
        valueStr = valueStr.trim().toLowerCase();
        double value = Double.parseDouble(valueStr.replaceAll("[a-z]+", ""));
        if (valueStr.endsWith("ms")) {
            this.millis = (long)(value);
        } else if (valueStr.endsWith("s")) {
            this.millis = (long)(value * 1000);
        } else if (valueStr.endsWith("m")) {
            this.millis = (long)(value * 60 * 1000);
        } else if (valueStr.endsWith("h")) {
            this.millis = (long)(value * 60 * 60 * 1000);
        } else {
            throw new IllegalArgumentException("Invalid time unit: " + valueStr);
        }
    }

    public long getMilliseconds() {
        return millis;
    }

    @Override
    public Object value() {
        return millis;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(millis);
    }
}
