package io.github.byzatic.tessera.mcg3.services.service_prometheus_export.prometheus_exporter;

import io.github.byzatic.commons.base_exceptions.OperationIncompleteException;

public interface PrometheusExporterInterface {
    void run() throws OperationIncompleteException;

    void terminate();
}
