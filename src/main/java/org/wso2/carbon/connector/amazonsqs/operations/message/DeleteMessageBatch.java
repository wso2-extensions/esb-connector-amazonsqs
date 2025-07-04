/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.connector.amazonsqs.operations.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.connector.amazonsqs.connection.SqsConnection;
import org.wso2.carbon.connector.amazonsqs.constants.Constants;
import org.wso2.carbon.connector.amazonsqs.exception.SqsInvalidConfigurationException;
import org.wso2.carbon.connector.amazonsqs.utils.Error;
import org.wso2.carbon.connector.amazonsqs.utils.Utils;
import org.wso2.integration.connector.core.AbstractConnectorOperation;
import org.wso2.integration.connector.core.ConnectException;
import org.wso2.integration.connector.core.connection.ConnectionHandler;
import org.wso2.integration.connector.core.util.ConnectorUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchResultEntry;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implements delete message operation.
 */
public class DeleteMessageBatch extends AbstractConnectorOperation {

    @Override
    public void execute(MessageContext messageContext, String responseVariable, Boolean overwriteBody)
            throws ConnectException {
        try {
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            SqsConnection sqsConnection = (SqsConnection) handler
                    .getConnection(Constants.CONNECTOR_NAME, Utils.getConnectionName(messageContext));
            String queueUrl = Utils.createUrl(messageContext, sqsConnection);
            String messageRequestEntries = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MESSAGE_REQUEST_ENTRIES);
            String apiCallTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_TIMEOUT);
            String apiCallAttemptTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_ATTEMPT_TIMEOUT);
            DeleteMessageBatchRequest.Builder deleteMessageBatchRequest =
                    DeleteMessageBatchRequest.builder().queueUrl(queueUrl);
            List<DeleteMessageBatchRequestEntry> entries = new ArrayList<>();
            JSONArray messageEntries = new JSONArray(messageRequestEntries);
            for (int i = 0; i < messageEntries.length(); i++) {
                JSONObject entryInJson = (JSONObject) messageEntries.get(i);
                Set<String> keySet = entryInJson.keySet();
                DeleteMessageBatchRequestEntry.Builder batchEntryBuilder = DeleteMessageBatchRequestEntry.builder();
                if (keySet.contains(Constants.ID)) {
                    batchEntryBuilder.id(entryInJson.get(Constants.ID).toString());
                } else {
                    String msg = "Missing required parameter: `id` in the message visibility batch entry.";
                    Utils.setErrorPropertiesToMessage(messageContext, Error.INVALID_CONFIGURATION, msg);
                    handleException(Constants.RUN_TIME_EXCEPTION_MSG, new SqsInvalidConfigurationException(msg),
                            messageContext);
                }

                if (keySet.contains(Constants.RECEIPT_HANDLE_CONFIG)) {
                    batchEntryBuilder.receiptHandle(entryInJson.get(Constants.RECEIPT_HANDLE_CONFIG).toString());
                } else {
                    String msg = "Missing required parameter: `receiptHandle` in the message visibility batch entry.";
                    Utils.setErrorPropertiesToMessage(messageContext, Error.INVALID_CONFIGURATION, msg);
                    handleException(Constants.RUN_TIME_EXCEPTION_MSG, new SqsInvalidConfigurationException(msg),
                            messageContext);
                }
                entries.add(batchEntryBuilder.build());
            }
            deleteMessageBatchRequest.entries(entries);
            if (StringUtils.isNotBlank(apiCallTimeout) || StringUtils.isNotBlank(apiCallAttemptTimeout)) {
                deleteMessageBatchRequest.overrideConfiguration(
                        Utils.getOverrideConfiguration(apiCallTimeout, apiCallAttemptTimeout).build());
            }
            DeleteMessageBatchResponse response = sqsConnection.getSqsClient().deleteMessageBatch(
                    deleteMessageBatchRequest.build());

            JsonObject resultJSON = createDeleteMessageBatchJsonResponse(response);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, resultJSON, null, null);
        } catch (SqsException e) {
            JsonObject errResult = Utils.generateErrorResponse(e);
            handleConnectorResponse(messageContext, responseVariable, overwriteBody, errResult, null, null);
        } catch (SdkClientException e) {
            Utils.setErrorPropertiesToMessage(messageContext, Error.CLIENT_SDK_ERROR, e.getMessage());
            handleException(Constants.CLIENT_EXCEPTION_MSG, e, messageContext);
        } catch (SqsInvalidConfigurationException e) {
            Utils.setErrorPropertiesToMessage(messageContext, Error.INVALID_CONFIGURATION, e.getMessage());
            handleException(Constants.GENERAL_ERROR_MSG, e, messageContext);
        } catch (NumberFormatException e) {
            Utils.setErrorPropertiesToMessage(messageContext, Error.INVALID_CONFIGURATION, e.getMessage());
            handleException(Constants.NUMBER_FORMAT_ERROR_MSG, e, messageContext);
        } catch (Exception e) {
            Utils.setErrorPropertiesToMessage(messageContext, Error.GENERAL_ERROR, e.getMessage());
            handleException(Constants.GENERAL_ERROR_MSG + e.getMessage(), messageContext);
        }
    }

    private JsonObject createDeleteMessageBatchJsonResponse(DeleteMessageBatchResponse response) {
        JsonObject resultJson = Utils.createResponseMetaDataElement(response.responseMetadata());

        JsonObject deleteMessageBatchResult = new JsonObject();

        // Add successful entries
        JsonArray successfulArray = new JsonArray();
        for (DeleteMessageBatchResultEntry entry : response.successful()) {
            JsonObject successfulEntry = new JsonObject();
            successfulEntry.addProperty("Id", entry.id());
            successfulArray.add(successfulEntry);
        }
        deleteMessageBatchResult.add(Constants.SUCCESSFUL, successfulArray);

        // Add failed entries
        JsonArray failedArray = Utils.createBatchResultErrorEntryJsonArray(response.failed());
        deleteMessageBatchResult.add(Constants.FAILED, failedArray);

        resultJson.add(Constants.DELETE_MESSAGE_BATCH_RESULT, deleteMessageBatchResult);
        resultJson.addProperty(Constants.SUCCESS, true);

        return resultJson;
    }
}
