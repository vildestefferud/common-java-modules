package no.nav.metrics;

import no.nav.metrics.handlers.InfluxHandler;
import no.nav.metrics.handlers.SensuHandler;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static no.nav.metrics.MetricsFactory.DISABLE_METRICS_REPORT_KEY;


public class MetricsClient {
    private static final Boolean DISABLE_METRICS_REPORT = parseBoolean(getProperty(DISABLE_METRICS_REPORT_KEY, "false"));
    private final Map<String, String> tags = new HashMap<>();

    public MetricsClient() {
        addSystemPropertiesToTags();
    }

    private void addSystemPropertiesToTags() {
        tags.put("application", System.getProperty("applicationName"));
        tags.put("hostname", System.getProperty("node.hostname"));
        tags.put("environment", System.getProperty("environment.name"));
    }

    void report(String metricName, Map<String, Object> fields, long timestampInSeconds) {
        if (!DISABLE_METRICS_REPORT) {
            String output = InfluxHandler.createLineProtocolPayload(metricName, tags, fields, timestampInSeconds);
            JSONObject jsonObject = SensuHandler.createJSON(tags.get("application"), output);
            SensuHandler.report(jsonObject);
        }
    }
}