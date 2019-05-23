/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.metrics2.reporters;

import com.codahale.metrics.ScheduledReporter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.storm.metrics2.filters.StormMetricsFilter;
import org.apache.storm.utils.ObjectReader;
import org.apache.storm.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ScheduledStormReporter implements StormReporter {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledStormReporter.class);
    protected ScheduledReporter reporter;
    protected long reportingPeriod;
    protected TimeUnit reportingPeriodUnit;

    public static TimeUnit getReportPeriodUnit(Map<String, Object> reporterConf) {
        TimeUnit unit = getTimeUnitForConfig(reporterConf, REPORT_PERIOD_UNITS);
        return unit == null ? TimeUnit.SECONDS : unit;
    }

    private static TimeUnit getTimeUnitForConfig(Map reporterConf, String configName) {
        String rateUnitString = ObjectReader.getString(reporterConf.get(configName), null);
        if (rateUnitString != null) {
            return TimeUnit.valueOf(rateUnitString);
        }
        return null;
    }

    public static long getReportPeriod(Map reporterConf) {
        return ObjectReader.getInt(reporterConf.get(REPORT_PERIOD), 10).longValue();
    }

    public static StormMetricsFilter getMetricsFilter(Map reporterConf) {
        StormMetricsFilter filter = null;
        Map<String, Object> filterConf = (Map) reporterConf.get("filter");
        if (filterConf != null) {
            String clazz = (String) filterConf.get("class");
            if (clazz != null) {
                filter = ReflectionUtils.newInstance(clazz);
                filter.prepare(filterConf);
            }
        }
        return filter;
    }

    @Override
    public void start() {
        if (reporter != null) {
            LOG.debug("Starting...");
            reporter.start(reportingPeriod, reportingPeriodUnit);
        } else {
            throw new IllegalStateException("Attempt to start without preparing " + getClass().getSimpleName());
        }
    }

    @Override
    public void stop() {
        if (reporter != null) {
            LOG.debug("Stopping...");
            reporter.stop();
        } else {
            throw new IllegalStateException("Attempt to stop without preparing " + getClass().getSimpleName());
        }
    }

    @Override
    public void close() {
        if (reporter != null) {
            LOG.debug("Closing...");
            reporter.close();
        }
    }
}
