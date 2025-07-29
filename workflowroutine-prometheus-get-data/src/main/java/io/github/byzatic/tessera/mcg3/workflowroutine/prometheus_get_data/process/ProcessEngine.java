package io.github.byzatic.tessera.mcg3.workflowroutine.prometheus_get_data.process;

import io.github.byzatic.pqletta.client.dto.response.PrometheusResponse;
import io.github.byzatic.pqletta.client.dto.response.ResponseFailure;
import io.github.byzatic.pqletta.client.dto.response.ResponseSuccess;
import io.github.byzatic.pqletta.client.dto.response.impl.error.GenericClientTransportError;
import io.github.byzatic.pqletta.client.dto.response.impl.error.GenericPrometheusApiError;
import io.github.byzatic.pqletta.client.dto.response.impl.success.PrometheusResult;
import io.github.byzatic.pqletta.client.dto.response.impl.success.Value;
import io.github.byzatic.pqletta.p_q_leta.PQletaInterface;
import io.github.byzatic.pqletta.p_q_leta.QueryDescription;
import io.github.byzatic.pqletta.p_q_leta.impl.PQleta;
import io.github.byzatic.tessera.mcg3.sharedresources.project_common.dto.DataItem;
import io.github.byzatic.tessera.mcg3.sharedresources.project_common.dto.MetricLabel;
import io.github.byzatic.tessera.mcg3.sharedresources.project_common.storage.LocalKeyValueStorage;
import io.github.byzatic.tessera.mcg3.sharedresources.project_common.storage.LocalKeyValueStorageInterface;
import io.github.byzatic.tessera.mcg3.workflowroutine.prometheus_get_data.domain.model.Argument;
import io.github.byzatic.tessera.mcg3.workflowroutine.prometheus_get_data.domain.service.ProcessEngineInterface;
import io.github.byzatic.tessera.mcg3.workflowroutine.prometheus_get_data.infrastructure.extractor.SupportPrometheusResponseExtractor;
import io.github.byzatic.tessera.mcg3.workflowroutine.prometheus_get_data.service.SupportArgsPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.byzatic.commons.ObjectsUtils;
import io.github.byzatic.commons.base_exceptions.OperationIncompleteException;
import io.github.byzatic.tessera.storageapi.dto.DataValueInterface;
import io.github.byzatic.tessera.storageapi.dto.StorageItem;
import io.github.byzatic.tessera.storageapi.exceptions.MCg3ApiOperationIncompleteException;
import io.github.byzatic.tessera.storageapi.storageapi.StorageApiInterface;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.configuration.ConfigurationParameter;
import io.github.byzatic.tessera.workflowroutine.execution_context.GraphPathInterface;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class ProcessEngine implements ProcessEngineInterface {
    private final static Logger logger = LoggerFactory.getLogger(ProcessEngine.class);
    private final StorageApiInterface workflowRoutineStorageApi;
    private final LocalKeyValueStorageInterface<String, DataValueInterface> localStore = new LocalKeyValueStorage<>();
    private final PQletaInterface pqleta;
    private final GraphPathInterface graphPath;

    public ProcessEngine(MCg3WorkflowRoutineApiInterface workflowRoutineApi) throws MCg3ApiOperationIncompleteException {
        try {
            Path promqlConfigurationDAOfilePath = null;
            Path promqlTemplateFilePath = null;
            for (ConfigurationParameter configurationParameter : workflowRoutineApi.getConfigurationParameters()) {
                logger.debug("Process configurationParameter - {}", configurationParameter);
                String key = configurationParameter.getParameterKey();
                Path value = Path.of(configurationParameter.getParameterValue());
                if (key.equals("promqlConfigurationDAOfilePath")) {
                    promqlConfigurationDAOfilePath = value;
                } else if (key.equals("promqlTemplateFilePath")) {
                    promqlTemplateFilePath = value;
                }
            }

            if (promqlConfigurationDAOfilePath == null) {
                throw new MCg3ApiOperationIncompleteException("Routine param promqlConfigurationDAOfilePath not set");
            }

            if (promqlTemplateFilePath == null) {
                throw new MCg3ApiOperationIncompleteException("Routine param promqlTemplateFilePath not set");
            }

            this.workflowRoutineStorageApi = workflowRoutineApi.getStorageApi();
            this.pqleta = new PQleta(promqlConfigurationDAOfilePath, promqlTemplateFilePath);

            this.graphPath = workflowRoutineApi.getExecutionContext().getPipelineExecutionInfo().getCurrentNodeExecutionGraphPath();
            logger.debug("Requested CurrentNodeExecutionGraphPath graphPath= {}", graphPath);

        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void processData(String functionName, List<String> args, String resultId) throws MCg3ApiOperationIncompleteException {
        try {
            switch (functionName) {
                case "GetData" -> preprocessorGetData(resultId, args);
                case "RemoveServiceLabel" -> preprocessorRemoveServiceLabel(resultId, args);
                case "ProcessReason" -> preprocessorProcessReason(resultId, args);
                case "PruneLabelsExceptBy" -> preprocessorPruneLabelsExceptBy(resultId, args);
                default -> throw new MCg3ApiOperationIncompleteException("No such function " + functionName);
            }
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    private void preprocessorPruneLabelsExceptBy(String resultId, List<String> args) {
        Argument argDataId = getArg(args, "DataId", Boolean.FALSE);
        List<Argument> argsRemoveLabelByName = getArgs(args, "ExceptedLabel", Boolean.TRUE);
        DataItem dataItem = (DataItem) localStore.get(argDataId.getValue());

        DataItem newDataItem = null;

        List<MetricLabel> labelsList = new ArrayList<>(dataItem.getMetricLabels());

        String[] values = argsRemoveLabelByName.stream()
                .map(Argument::getValue)
                .toArray(String[]::new);

        retainLabelsByKeys(labelsList, values);

        newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();

        if (localStore.containsKey(resultId)) {
            localStore.delete(resultId);
            localStore.put(resultId, newDataItem);
        } else {
            localStore.put(resultId, newDataItem);
        }
    }

    private void retainLabelsByKeys(List<MetricLabel> labelsList, String[] allowedKeys) {
        Set<String> allowedKeySet = new HashSet<>(Arrays.asList(allowedKeys));
        labelsList.removeIf(label -> !allowedKeySet.contains(label.getKey()));
    }

    private Argument getArg(List<String> args, String argName, Boolean isNullable){
        Argument arg = SupportArgsPreProcessor.searchArg(args, argName);
        if (!isNullable) ObjectsUtils.requireNonNull(arg, new IllegalArgumentException("Argument "+ argName + " not found!"));
        return arg;
    }

    private List<Argument> getArgs(List<String> args, String argName, Boolean isNullable) {
        List<Argument> result = SupportArgsPreProcessor.searchArgs(args, argName);
        if (!isNullable) ObjectsUtils.requireNonNull(result, new IllegalArgumentException("Arguments " + argName + " not found!"));
        return result;
    }

    private MetricLabel getReasonMetricLabel(DataItem dataItem, String reasonMsg) {
        MetricLabel reason= null;
        MetricLabel reasonMetricLabel= findByKey(dataItem, "reason");
        if (reasonMetricLabel == null) {
            reason= MetricLabel.newBuilder()
                    .setKey("reason")
                    .setSign("=")
                    .setValue(graphPath.getGraphPath()+" => "+reasonMsg)
                    .build();
        } else {
            reason= MetricLabel.newBuilder()
                    .setKey("reason")
                    .setSign("=")
                    .setValue(reasonMetricLabel.getKey())
                    .build();
        }
        return reason;
    }

    private MetricLabel findByKey(DataItem dataItem, String key) {
        List<MetricLabel> labels = dataItem.getMetricLabels();
        if (labels == null || key == null) return null;
        for (MetricLabel label : labels) {
            if (key.equals(label.getKey())) {
                return label;
            }
        }
        return null;
    }

    private void preprocessorProcessReason(String resultId, List<String> args) {
        Argument argGlobalReasonMessage = getArg(args, "GlobalReasonMessage", Boolean.TRUE);
        Argument argOkReasonMessage = getArg(args, "OkReasonMessage", Boolean.TRUE);
        Argument argWarningReasonMessage = getArg(args, "WarningReasonMessage", Boolean.TRUE);
        Argument argAlarmReasonMessage = getArg(args, "AlarmReasonMessage", Boolean.TRUE);
        Argument argPasteReasonWhenOk = getArg(args, "PasteReasonWhenOk", Boolean.FALSE);
        Argument argPrometheusEmptyData = getArg(args, "PrometheusEmptyData", Boolean.FALSE);
        Argument argIgnoreExistsReason = getArg(args, "IgnoreExistsReason", Boolean.TRUE);
        Argument argDataId = getArg(args, "DataId", Boolean.FALSE);

        DataItem dataItem = (DataItem) localStore.get(argDataId.getValue());
        Integer value = Integer.valueOf(dataItem.getMetricValue());

        DataItem newDataItem = null;

        if (argIgnoreExistsReason != null && Boolean.valueOf(argIgnoreExistsReason.getValue()) == Boolean.FALSE) {
            boolean hasReasonLabel = dataItem.getMetricLabels().stream()
                    .anyMatch(label -> "reason".equals(label.getKey()));
            if (hasReasonLabel == Boolean.FALSE) throw new RuntimeException("arg IgnoreExistsReason is not null and equals True, but reasons' label was not found");
            newDataItem = DataItem.newBuilder(dataItem).build();
        } else if (argGlobalReasonMessage == null) {
            logger.debug("GlobalReasonMessage is null");

            ObjectsUtils.requireNonNull(argWarningReasonMessage, new IllegalArgumentException("Argument "+ "WarningReasonMessage" + " not found!"));
            ObjectsUtils.requireNonNull(argAlarmReasonMessage, new IllegalArgumentException("Argument "+ "AlarmReasonMessage" + " not found!"));

            if (value == 0 && !Boolean.parseBoolean(argPasteReasonWhenOk.getValue())) {
                logger.debug("value is 0 and PasteReasonWhenOk is False");
                newDataItem = DataItem.newBuilder(dataItem).build();
            } else if (value == 0 && Boolean.parseBoolean(argPasteReasonWhenOk.getValue())) {
                logger.debug("value is 0 and PasteReasonWhenOk is True");
                ObjectsUtils.requireNonNull(argOkReasonMessage, new IllegalArgumentException("Argument "+ "OkReasonMessage" + " not found!"));
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argOkReasonMessage.getValue()));
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 1) {
                logger.debug("value is 1");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argWarningReasonMessage.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 2) {
                logger.debug("value is 2");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argAlarmReasonMessage.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 3) {
                logger.debug("value is 3");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argPrometheusEmptyData.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).setMetricValue("1").build();
            } else {
                throw new IllegalArgumentException("Wrong value " + value + " to generate reason message.");
            }

        } else {
            logger.debug("GlobalReasonMessage is not null");

            if (value == 0 && !Boolean.parseBoolean(argPasteReasonWhenOk.getValue())) {
                logger.debug("value is 0 and PasteReasonWhenOk is False");
                newDataItem = DataItem.newBuilder(dataItem).build();
            } else if (value == 0 && Boolean.parseBoolean(argPasteReasonWhenOk.getValue())) {
                logger.debug("value is 0 and PasteReasonWhenOk is True");
                if (argOkReasonMessage != null) {
                    logger.debug("OkReasonMessage is not null");
                    List<MetricLabel> labelsList = dataItem.getMetricLabels();
                    labelsList.add(getReasonMetricLabel(dataItem, argOkReasonMessage.getValue())
                    );
                    newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
                } else {
                    logger.debug("OkReasonMessage is null");
                    newDataItem = DataItem.newBuilder(dataItem).build();
                }
            } else if (value == 1) {
                logger.debug("value is 1");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argGlobalReasonMessage.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 2) {
                logger.debug("value is 2");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argGlobalReasonMessage.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).build();
            } else if (value == 3) {
                logger.debug("value is 3");
                List<MetricLabel> labelsList = dataItem.getMetricLabels();
                labelsList.add(getReasonMetricLabel(dataItem, argPrometheusEmptyData.getValue())
                );
                newDataItem = DataItem.newBuilder(dataItem).setMetricLabels(labelsList).setMetricValue("1").build();
            } else {
                throw new IllegalArgumentException("Wrong value " + value + " to generate reason message.");
            }
        }

        if (localStore.containsKey(resultId)) {
            localStore.delete(resultId);
            localStore.put(resultId, newDataItem);
        } else {
            localStore.put(resultId, newDataItem);
        }
    }

    private void preprocessorRemoveServiceLabel(String resultId, List<String> args) {
        String argName = "FromDataId";
        Argument dataId = SupportArgsPreProcessor.searchArg(args, argName);
        ObjectsUtils.requireNonNull(dataId, new IllegalArgumentException("Argument "+ argName + " not found!"));
        String dataIdName = dataId.getValue();


        DataItem dataItem = (DataItem) localStore.get(dataIdName);

        List<MetricLabel> labels = new ArrayList<>(dataItem.getMetricLabels());
        labels.removeIf(label -> {
            String val = label.getValue();
            return val != null && val.matches("__.*__");
        });

        labels.removeIf(label -> {
            String val = label.getKey();
            return val != null && val.matches("__.*__");
        });

        DataItem newDataItem = DataItem.newBuilder()
                .setMetricName(dataItem.getMetricName())
                .setMetricLabels(labels)
                .setMetricValue(dataItem.getMetricValue())
                .setMetricCreationTime(dataItem.getMetricCreationTime())
                .build();

        localStore.delete(dataIdName);
        localStore.put(dataIdName, newDataItem);
    }

    @Override
    public void getData(String childName, String storageId, Boolean isGlobal, String dataId, String alias) throws MCg3ApiOperationIncompleteException {
        try {
            StorageItem.ScopeType scopeType = StorageItem.ScopeType.LOCAL;
            if (childName != null) {
                scopeType = StorageItem.ScopeType.DOWNSTREAM;
            } else {
                if (isGlobal) scopeType = StorageItem.ScopeType.GLOBAL;

            }
            StorageItem requestedStorageItem = workflowRoutineStorageApi.getStorageObject(
                    StorageItem.newBuilder()
                            .setScope(scopeType)
                            .setDownstreamName(childName)
                            .setStorageId(storageId)
                            .setDataId(dataId)
                            .setDataValue(null)
                            .build()
            );
            localStore.put(alias, requestedStorageItem.getDataValue());
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }

    @Override
    public void putData(String localDataId, String storageId, Boolean isGlobal, String dataId) throws MCg3ApiOperationIncompleteException {
        try {
            logger.debug("putData localDataId -> {} storageId -> {} isGlobal -> {} dataId -> {}", localDataId, storageId, isGlobal, dataId);
            StorageItem.ScopeType scopeType = StorageItem.ScopeType.LOCAL;
            if (isGlobal) scopeType = StorageItem.ScopeType.GLOBAL;
            StorageItem storageItem = StorageItem.newBuilder()
                    .setScope(scopeType)
                    .setDownstreamName(null)
                    .setStorageId(storageId)
                    .setDataId(dataId)
                    .setDataValue(localStore.get(localDataId))
                    .build();
            workflowRoutineStorageApi.putStorageObject(
                    storageItem
            );
            logger.debug("putData localDataId -> {} storageId -> {} isGlobal -> {} dataId -> {} is complete", localDataId, storageId, isGlobal, dataId);
        } catch (Exception e) {
            throw new MCg3ApiOperationIncompleteException(e);
        }
    }


    private void preprocessorGetData(String resultId, List<String> args) throws OperationIncompleteException, MCg3ApiOperationIncompleteException {

        String argName = "PQletaQueryId";
        Argument pqletaQueryId = SupportArgsPreProcessor.searchArg(args, argName);
        ObjectsUtils.requireNonNull(pqletaQueryId, new IllegalArgumentException("Argument "+ argName + " not found!"));

        PrometheusResponse prometheusResponse = pqleta.processQuery(
                QueryDescription.newBuilder()
                        .setQueryIdentifier(pqletaQueryId.getValue())
                        .build()
        );

        if ( prometheusResponse instanceof ResponseSuccess) {
            PrometheusResult prometheusResult = (PrometheusResult) prometheusResponse;
            Value metricData = SupportPrometheusResponseExtractor.extractMetricData(prometheusResult);
            localStore.put(resultId, DataItem.newBuilder()
                    .setMetricName(resultId)
                    .setMetricLabels(SupportPrometheusResponseExtractor.extractMetricLabels((PrometheusResult) prometheusResponse))
                    .setMetricValue(
                            String.valueOf(
                                    metricData.getData()
                            )
                    )
                    .setMetricCreationTime(metricData.getInstantOfEpochSecond())
                    .build()
            );
        } else if (prometheusResponse instanceof ResponseFailure) {
            MetricLabel reason = null;
            if (prometheusResponse instanceof GenericClientTransportError) {
                reason = MetricLabel.newBuilder()
                        .setKey("reason")
                        .setSign("=")
                        .setValue(graphPath.getGraphPath()+" => "+((GenericClientTransportError) prometheusResponse).getMessage())
                        .build();
            } else if (prometheusResponse instanceof GenericPrometheusApiError) {
                reason = MetricLabel.newBuilder()
                        .setKey("reason")
                        .setSign("=")
                        .setValue(graphPath.getGraphPath()+" => "+((GenericPrometheusApiError) prometheusResponse).getErrorMessage())
                        .build();
            } else {
                throw new MCg3ApiOperationIncompleteException("prometheusResponse not instanceof GenericClientTransportError or GenericPrometheusApiError");
            }
            List<MetricLabel> metricLabels = new ArrayList<>();
            metricLabels.add(reason);
            localStore.put(resultId, DataItem.newBuilder()
                    .setMetricName(resultId)
                    .setMetricLabels(metricLabels)
                    .setMetricValue("1")
                    .setMetricCreationTime(Instant.now())
                    .build()
            );
        } else {
            throw new MCg3ApiOperationIncompleteException("prometheusResponse not instanceof ResponseSuccess or ResponseFailure");
        }
    }


}
