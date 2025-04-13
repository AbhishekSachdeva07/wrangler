/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */
package io.cdap.wrangler.directives.stats;

import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.annotations.PublicEvolving;
import io.cdap.wrangler.api.parser.*;

import java.util.*;

/**
 * A directive that aggregates byte size and time duration values across multiple rows.
 */
@PublicEvolving
public class AggregateStats implements Directive {

    private String sourceSizeColumn;
    private String sourceTimeColumn;
    private String targetSizeColumn;
    private String targetTimeColumn;

    private String sizeUnit = "MB";
    private String timeUnit = "seconds";
    private String aggregationType = "total";

    private long totalBytes = 0L;
    private long totalTime = 0L;
    private int rowCount = 0;

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder("aggregate-stats")
                .define("sourceSize", TokenType.COLUMN_NAME)
                .define("sourceTime", TokenType.COLUMN_NAME)
                .define("targetSize", TokenType.COLUMN_NAME)
                .define("targetTime", TokenType.COLUMN_NAME)
                .defineOptional("sizeUnit", TokenType.TEXT)
                .defineOptional("timeUnit", TokenType.TEXT)
                .defineOptional("aggregationType", TokenType.TEXT)
                .build();
    }

    @Override
    public void initialize(Arguments arguments) throws DirectiveParseException {
        Token sizeToken = arguments.value("sourceSize");
        Token timeToken = arguments.value("sourceTime");
        Token targetSizeToken = arguments.value("targetSize");
        Token targetTimeToken = arguments.value("targetTime");

        if (!(sizeToken instanceof ColumnName) || !(timeToken instanceof ColumnName)
                || !(targetSizeToken instanceof ColumnName) || !(targetTimeToken instanceof ColumnName)) {
            throw new DirectiveParseException("Invalid column arguments.");
        }

        sourceSizeColumn = ((ColumnName) sizeToken).value();
        sourceTimeColumn = ((ColumnName) timeToken).value();
        targetSizeColumn = ((ColumnName) targetSizeToken).value();
        targetTimeColumn = ((ColumnName) targetTimeToken).value();

        try {
            Token unitToken = arguments.value("sizeUnit");
            if (unitToken instanceof Text) {
                sizeUnit = ((Text) unitToken).value().toUpperCase();
            }
        } catch (Exception ignored) {}

        try {
            Token timeUnitToken = arguments.value("timeUnit");
            if (timeUnitToken instanceof Text) {
                timeUnit = ((Text) timeUnitToken).value().toLowerCase();
            }
        } catch (Exception ignored) {}

        try {
            Token aggToken = arguments.value("aggregationType");
            if (aggToken instanceof Text) {
                aggregationType = ((Text) aggToken).value().toLowerCase();
            }
        } catch (Exception ignored) {}
    }


    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        for (Row row : rows) {
            Object sizeObj = row.getValue(sourceSizeColumn);
            Object timeObj = row.getValue(sourceTimeColumn);

            if (sizeObj instanceof String) {
                ByteSize byteSize = new ByteSize((String) sizeObj);
                totalBytes += byteSize.getBytes();
            }

            if (timeObj instanceof String) {
                TimeDuration timeDuration = new TimeDuration((String) timeObj);
                totalTime += timeDuration.getMilliseconds();
            }

            rowCount++;
        }

        double finalSize = aggregationType.equals("average") && rowCount > 0
                ? (double) totalBytes / rowCount
                : (double) totalBytes;

        double finalTime = aggregationType.equals("average") && rowCount > 0
                ? (double) totalTime / rowCount
                : (double) totalTime;

        finalSize = convertBytes(finalSize, sizeUnit);
        finalTime = convertTime(finalTime, timeUnit);

        Row result = new Row();
        result.add(targetSizeColumn, finalSize);
        result.add(targetTimeColumn, finalTime);

        return Collections.singletonList(result);
    }

    private double convertBytes(double bytes, String unit) {
        switch (unit) {
            case "GB":
                return bytes / (1024 * 1024 * 1024);
            case "MB":
                return bytes / (1024 * 1024);
            case "KB":
                return bytes / 1024;
            default:
                return bytes;
        }
    }

    private double convertTime(double millis, String unit) {
        switch (unit) {
            case "seconds":
                return millis / 1000.0;
            case "minutes":
                return millis / (1000.0 * 60);
            default:
                return millis;
        }
    }

    @Override
    public void destroy() {
        // no-op
    }
}
