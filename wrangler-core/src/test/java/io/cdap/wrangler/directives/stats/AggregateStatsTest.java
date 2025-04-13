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
package com.io.cdap.wrangler.directives.stats;

import com.io.cdap.wrangler.TestingRig;
import com.io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AggregateStatsTest {

    @Test
    public void testAggregateStatsTotalSizeAndTime() throws Exception {
        // Create sample input rows
        List<Row> rows = Arrays.asList(
                new Row("data_transfer_size", "1MB").add("response_time", "1000ms"),   // 1 MB, 1 sec
                new Row("data_transfer_size", "512KB").add("response_time", "2s"),     // 0.5 MB, 2 sec
                new Row("data_transfer_size", "2MB").add("response_time", "500ms")     // 2 MB, 0.5 sec
        );

        // Define the recipe
        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        // Execute recipe using TestingRig
        List<Row> results = TestingRig.execute(recipe, rows);

        // Verify result contains exactly one row
        Assert.assertEquals(1, results.size());

        // Expected size = (1 + 0.5 + 2) MB = 3.5 MB
        double expectedTotalSizeMB = 3.5;

        // Expected time = (1 + 2 + 0.5) seconds = 3.5 seconds
        double expectedTotalTimeSec = 3.5;

        // Validate outputs with small tolerance
        Assert.assertEquals(expectedTotalSizeMB,
                (Double) results.get(0).getValue("total_size_mb"), 0.001);

        Assert.assertEquals(expectedTotalTimeSec,
                (Double) results.get(0).getValue("total_time_sec"), 0.001);
    }
}
