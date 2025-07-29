package io.github.byzatic.tessera.mcg3.workflowroutine.prometheus_get_data.infrastructure.extractor;

import io.github.byzatic.pqletta.client.dto.response.impl.success.Data;
import io.github.byzatic.pqletta.client.dto.response.impl.success.PrometheusResult;
import io.github.byzatic.pqletta.client.dto.response.impl.success.Value;
import io.github.byzatic.tessera.mcg3.sharedresources.project_common.dto.MetricLabel;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SupportPrometheusResponseExtractor {
    public static List<MetricLabel> extractMetricLabels(PrometheusResult prometheusResult) throws MCg3ApiOperationIncompleteException {
        validate(prometheusResult);
        List<MetricLabel> metricLabels = new ArrayList<>();
        Map<String, String> rawMetricLabels = prometheusResult.getData().getResult().get(0).getMetric();
        for (Map.Entry<String, String> MetricLabelEntry : rawMetricLabels.entrySet()) {
            metricLabels.add(
                    MetricLabel.newBuilder()
                            .setKey(MetricLabelEntry.getKey())
                            .setValue(MetricLabelEntry.getValue())
                            .setSign("=")
                            .build()
            );
        }
        return metricLabels;
    }

    public static Integer extractMetricValue(PrometheusResult prometheusResult) throws MCg3ApiOperationIncompleteException {
        validate(prometheusResult);
        Value value = findLatestValue(prometheusResult.getData().getResult().get(0).getValues());
        return Integer.valueOf(value.getData());
    }

    public static Instant extractMetricCreationTime(PrometheusResult prometheusResult) throws MCg3ApiOperationIncompleteException {
        validate(prometheusResult);
        Value value = findLatestValue(prometheusResult.getData().getResult().get(0).getValues());
        return value.getInstantOfEpochSecond();
    }

    public static Value extractMetricData(PrometheusResult prometheusResult) throws MCg3ApiOperationIncompleteException {
        validate(prometheusResult);
        return findLatestValue(prometheusResult.getData().getResult().get(0).getValues());
    }

    public static Value findLatestValue(List<Value> values) {
        //найти в масиве объектов Value{instantOfEpochSecond=2025-05-27T08:09:19Z, data='0'} Value наиболее свежий по instantOfEpochSecond Instant
        return values.stream()
                .max((v1, v2) -> v1.getInstantOfEpochSecond().compareTo(v2.getInstantOfEpochSecond()))
                .orElse(null);
    }

    private static void validate(PrometheusResult prometheusResult) throws MCg3ApiOperationIncompleteException {
        Data data = prometheusResult.getData();
        if (data == null) {
            throw new MCg3ApiOperationIncompleteException("Prometheus result data is null -> " + prometheusResult);
        }
        if (prometheusResult.getData().getResult().size() > 1) {
            throw new MCg3ApiOperationIncompleteException("Prometheus result data contains more then one metric " + prometheusResult);
        }
        if (prometheusResult.getData().getResult().isEmpty()) {
            throw new MCg3ApiOperationIncompleteException("Prometheus result data contains less then one metric " + prometheusResult);
        }
    }
}
