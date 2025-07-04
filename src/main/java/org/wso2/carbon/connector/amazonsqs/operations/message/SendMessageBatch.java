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
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResultEntry;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implements send message batch operation.
 */
public class SendMessageBatch extends AbstractConnectorOperation {

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
            SendMessageBatchRequest.Builder sendMessageBatchBuilder = SendMessageBatchRequest.builder().
                    queueUrl(queueUrl);
            List<SendMessageBatchRequestEntry> entries = new ArrayList<>();
            JSONArray messageEntries = new JSONArray(messageRequestEntries);
            for (int i = 0; i < messageEntries.length(); i++) {
                JSONObject entryInJson = (JSONObject) messageEntries.get(i);
                Set<String> keySet = entryInJson.keySet();
                SendMessageBatchRequestEntry.Builder SendMessageBatchEntryBuilder = SendMessageBatchRequestEntry.
                        builder();
                if (keySet.contains(Constants.ID)) {
                    SendMessageBatchEntryBuilder.id(entryInJson.get(Constants.ID).toString());
                } else {
                    String msg = "Missing required parameter: `id` in the batch message entry.";
                    Utils.setErrorPropertiesToMessage(messageContext, Error.INVALID_CONFIGURATION, msg);
                    handleException(Constants.RUN_TIME_EXCEPTION_MSG, new SqsInvalidConfigurationException(msg),
                            messageContext);
                }

                if (keySet.contains(Constants.MESSAGE_BODY)) {
                    SendMessageBatchEntryBuilder.messageBody(entryInJson.get(Constants.MESSAGE_BODY).toString());
                } else {
                    String msg = "Missing required parameter: `messageBody` in the batch message entry.";
                    Utils.setErrorPropertiesToMessage(messageContext, Error.INVALID_CONFIGURATION, msg);
                    handleException(Constants.RUN_TIME_EXCEPTION_MSG, new SqsInvalidConfigurationException(msg),
                            messageContext);
                }

                if (keySet.contains(Constants.DELAY_SECONDS)) {
                    SendMessageBatchEntryBuilder.delaySeconds(Integer.valueOf(entryInJson.
                            get(Constants.DELAY_SECONDS).toString()));
                }

                if (keySet.contains(Constants.MESSAGE_DEDUPLICATION_ID)) {
                    SendMessageBatchEntryBuilder.messageDeduplicationId(entryInJson.get(
                            Constants.MESSAGE_DEDUPLICATION_ID).toString());
                }

                if (keySet.contains(Constants.MESSAGE_GROUP_ID)) {
                    SendMessageBatchEntryBuilder.messageGroupId(entryInJson.get(Constants.MESSAGE_GROUP_ID).
                            toString());
                }

                if (keySet.contains(Constants.MESSAGE_ATTRIBUTES)) {
                    SendMessageBatchEntryBuilder.messageAttributes(
                            Utils.addMessageAttributes(entryInJson.get(Constants.MESSAGE_ATTRIBUTES).toString()));
                }
                if (keySet.contains(Constants.MESSAGE_SYSTEM_ATTRIBUTES)) {
                    SendMessageBatchEntryBuilder.messageSystemAttributes(
                            Utils.addSystemMessageAttributes(entryInJson.get(Constants.MESSAGE_SYSTEM_ATTRIBUTES).toString()));
                }
                entries.add(SendMessageBatchEntryBuilder.build());
            }
            sendMessageBatchBuilder.entries(entries);
            if (StringUtils.isNotBlank(apiCallTimeout) || StringUtils.isNotBlank(apiCallAttemptTimeout)) {
                sendMessageBatchBuilder.overrideConfiguration(
                        Utils.getOverrideConfiguration(apiCallTimeout, apiCallAttemptTimeout).build());
            }
            SendMessageBatchResponse sendMessageBatchResponse = sqsConnection.getSqsClient().
                    sendMessageBatch(sendMessageBatchBuilder.build());
            JsonObject resultJSON = createSendMessageBatchJsonResponse(sendMessageBatchResponse);
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

    private JsonObject createSendMessageBatchJsonResponse(SendMessageBatchResponse sendMessageBatchResponse) {
        JsonObject resultJson = Utils.createResponseMetaDataElement(sendMessageBatchResponse.responseMetadata());

        JsonObject sendMessageBatchResult = new JsonObject();

        // Add successful entries
        JsonArray successfulArray = new JsonArray();
        for (SendMessageBatchResultEntry entry : sendMessageBatchResponse.successful()) {
            JsonObject successfulEntry = new JsonObject();
            successfulEntry.addProperty("Id", entry.id());
            if (entry.md5OfMessageAttributes() != null) {
                successfulEntry.addProperty(Constants.MD5_OF_MESSAGE_ATTRIBUTES, entry.md5OfMessageAttributes());
            }
            successfulEntry.addProperty(Constants.MD5_OF_MESSAGE_BODY, entry.md5OfMessageBody());
            if (entry.md5OfMessageSystemAttributes() != null) {
                successfulEntry.addProperty(Constants.MD5_OF_MESSAGE_SYSTEM_ATTRIBUTES, entry.md5OfMessageSystemAttributes());
            }
            if (entry.sequenceNumber() != null) {
                successfulEntry.addProperty(Constants.SEQUENCE_NUMBER, entry.sequenceNumber());
            }
            successfulEntry.addProperty(Constants.MESSAGE_ID, entry.messageId());
            successfulArray.add(successfulEntry);
        }
        sendMessageBatchResult.add(Constants.SUCCESSFUL, successfulArray);

        // Add failed entries
        JsonArray failedArray = Utils.createBatchResultErrorEntryJsonArray(sendMessageBatchResponse.failed());
        sendMessageBatchResult.add(Constants.FAILED, failedArray);

        resultJson.add(Constants.SEND_MESSAGE_BATCH_RESULT, sendMessageBatchResult);
        resultJson.addProperty(Constants.SUCCESS, true);

        return resultJson;
    }
}
