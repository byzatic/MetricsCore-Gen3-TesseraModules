package io.github.byzatic.tessera.mcg3.workflowroutine.prometheus_get_data.domain.service;

import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;

public interface ProcessorInterface {
    void process(String commandLineInput) throws MCg3ApiOperationIncompleteException;
}
