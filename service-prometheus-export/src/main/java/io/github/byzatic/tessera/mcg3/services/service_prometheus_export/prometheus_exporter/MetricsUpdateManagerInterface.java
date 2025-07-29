package io.github.byzatic.tessera.mcg3.services.service_prometheus_export.prometheus_exporter;

import io.github.byzatic.commons.base_exceptions.OperationIncompleteException;

public interface MetricsUpdateManagerInterface {
    void updateMetrics() throws OperationIncompleteException;
}
