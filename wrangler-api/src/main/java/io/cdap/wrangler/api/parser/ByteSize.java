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

public class ByteSize implements Token {
    private final long bytes;
    private final String original;

    public ByteSize(String valueStr) {
        this.original = valueStr;
        valueStr = valueStr.trim().toUpperCase();
        double value = Double.parseDouble(valueStr.replaceAll("[A-Z]+", ""));
        if (valueStr.endsWith("KB")) {
            this.bytes = (long)(value * 1024);
        } else if (valueStr.endsWith("MB")) {
            this.bytes = (long)(value * 1024 * 1024);
        } else if (valueStr.endsWith("GB")) {
            this.bytes = (long)(value * 1024 * 1024 * 1024);
        } else if (valueStr.endsWith("TB")) {
            this.bytes = (long)(value * 1024L * 1024 * 1024 * 1024);
        } else if (valueStr.endsWith("B")) {
            this.bytes = (long)value;
        } else {
            throw new IllegalArgumentException("Invalid byte unit: " + valueStr);
        }
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public Object value() {
        return bytes;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(bytes);
    }
}
