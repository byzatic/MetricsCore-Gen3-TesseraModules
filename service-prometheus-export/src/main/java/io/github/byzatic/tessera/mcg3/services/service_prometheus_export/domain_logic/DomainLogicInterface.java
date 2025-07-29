package io.github.byzatic.tessera.mcg3.services.service_prometheus_export.domain_logic;

import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;

public interface DomainLogicInterface {
    void process() throws MCg3ApiOperationIncompleteException;
}
