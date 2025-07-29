package io.github.byzatic.tessera.mcg3.services.service_prometheus_export.prometheus_exporter;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.commons.base_exceptions.OperationIncompleteException;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.mcg3.services.service_prometheus_export.prometheus_exporter.scheduler.JobDetail;
import io.github.byzatic.tessera.mcg3.services.service_prometheus_export.prometheus_exporter.scheduler.Scheduler;

public class PrometheusExporter implements PrometheusExporterInterface {
    private final static Logger logger = LoggerFactory.getLogger(PrometheusExporter.class);
    private final MetricsUpdateManagerInterface metricsUpdateManager;
    private final String httpServerAddress;
    private final Integer httpServerPort;
    private final String cronMetricUpdateString;
    private final Scheduler scheduler = new Scheduler();
    private final MCg3ServiceApiInterface serviceApiInterface;
    private Integer state = 0;

    public PrometheusExporter(MetricsUpdateManagerInterface metricsUpdateManager, String httpServerAddress, Integer httpServerPort, String cronMetricUpdateString, MCg3ServiceApiInterface serviceApiInterface) {
        this.metricsUpdateManager = metricsUpdateManager;
        this.httpServerAddress = httpServerAddress;
        this.httpServerPort = httpServerPort;
        this.cronMetricUpdateString = cronMetricUpdateString;
        this.serviceApiInterface = serviceApiInterface;
    }

    @Override
    public void run() throws OperationIncompleteException {
        try (HTTPServer server = HTTPServer.builder().hostname(httpServerAddress).port(httpServerPort).buildAndStart()) {
            logger.debug("HTTPServer listening on port http://{}:{}{}", httpServerAddress,  httpServerPort, "/metrics");
            logger.warn("HTTPServer using default location {}", "/metrics");

            StateCollector stateCollector = new StateCollector();
            JobDetail job = new JobDetail(
                    new Process(
                            metricsUpdateManager,
                            stateCollector,
                            serviceApiInterface
                    ),
                    cronMetricUpdateString
            );
            scheduler.addTask(job);
            do {
                scheduler.runAllTasks(true);
                if (stateCollector.healthStatus == 1) {
                    logger.error("Update metrics process finished with error", stateCollector.errorReason);
                    throw new OperationIncompleteException(stateCollector.errorReason);
                }
                Thread.sleep(10L);
            } while (state == 0);

        } catch (Exception e) {
            throw new OperationIncompleteException(e);
        }
    }

    @Override
    public void terminate() {
        state = 1;
    }

    private class StateCollector {
        public Integer healthStatus = null;
        public Throwable errorReason = null;

    }

    private class Process implements Runnable{
        private final MetricsUpdateManagerInterface metricsUpdateManager;

        private final StateCollector stateCollector;
        private final MCg3ServiceApiInterface serviceApiInterface;

        public Process(MetricsUpdateManagerInterface metricsUpdateManager, StateCollector stateCollector, MCg3ServiceApiInterface serviceApiInterface) {
            this.metricsUpdateManager = metricsUpdateManager;
            this.stateCollector = stateCollector;
            this.stateCollector.healthStatus = 0;
            this.serviceApiInterface = serviceApiInterface;
        }

        @Override
        public void run() {
            try (AutoCloseable ignored = serviceApiInterface.getExecutionContext().getMdcContext().use()) {
                this.stateCollector.healthStatus = 0;
                this.metricsUpdateManager.updateMetrics();
                logger.debug("Stopping scheduled task due complete");
            } catch (Exception e) {
                this.stateCollector.errorReason = e;
                this.stateCollector.healthStatus = 1;
                logger.error("Error updating metrics", e);
                throw new RuntimeException("Stopping scheduled task due to exception in updateMetrics", e);
            }
        }
    }

}
