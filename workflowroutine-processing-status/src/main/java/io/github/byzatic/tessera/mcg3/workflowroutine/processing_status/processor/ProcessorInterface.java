package io.github.byzatic.tessera.mcg3.workflowroutine.processing_status.processor;

import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;

public interface ProcessorInterface {
    void process(String commandLineInput) throws MCg3ApiOperationIncompleteException;
}
