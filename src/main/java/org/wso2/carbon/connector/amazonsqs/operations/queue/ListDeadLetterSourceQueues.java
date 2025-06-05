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
package org.wso2.carbon.connector.amazonsqs.operations.queue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
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
import software.amazon.awssdk.services.sqs.model.ListDeadLetterSourceQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListDeadLetterSourceQueuesResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * Implements list dead letter source queues operation.
 */
public class ListDeadLetterSourceQueues extends AbstractConnectorOperation {

    @Override
    public void execute(MessageContext messageContext, String responseVariable, Boolean overwriteBody)
            throws ConnectException {
        try {
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            SqsConnection sqsConnection = (SqsConnection) handler
                    .getConnection(Constants.CONNECTOR_NAME, Utils.getConnectionName(messageContext));
            String queueUrl = Utils.createUrl(messageContext, sqsConnection);
            String apiCallTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_TIMEOUT);
            String apiCallAttemptTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_ATTEMPT_TIMEOUT);
            String nextToken = (String) ConnectorUtils.lookupTemplateParamater(messageContext, Constants.NEXT_TOKEN);
            String maxResults = (String) ConnectorUtils.lookupTemplateParamater(messageContext, Constants.MAX_RESULT);
            ListDeadLetterSourceQueuesRequest.Builder listDeadLetterSourceQueuesBuilder =
                    ListDeadLetterSourceQueuesRequest.builder();
            if (StringUtils.isNotBlank(nextToken)) {
                listDeadLetterSourceQueuesBuilder.nextToken(nextToken);
            }
            if (StringUtils.isNotBlank(maxResults)) {
                listDeadLetterSourceQueuesBuilder.maxResults(Integer.getInteger(maxResults));
            }
            listDeadLetterSourceQueuesBuilder.queueUrl(queueUrl);
            if (StringUtils.isNotBlank(apiCallTimeout) || StringUtils.isNotBlank(apiCallAttemptTimeout)) {
                listDeadLetterSourceQueuesBuilder.overrideConfiguration(
                        Utils.getOverrideConfiguration(apiCallTimeout, apiCallAttemptTimeout).build());
            }
            ListDeadLetterSourceQueuesResponse listQueueUrlResponse = sqsConnection.getSqsClient().
                    listDeadLetterSourceQueues(listDeadLetterSourceQueuesBuilder.build());

            JsonObject resultJSON = createListDeadLetterSourceQueuesJsonResponse(listQueueUrlResponse);
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
            Utils.setErrorPropertiesToMessage(messageContext, Error.INVALID_CONFIGURATION,
                    Constants.NUMBER_FORMAT_ERROR_MSG + e.getMessage());
            handleException(Constants.NUMBER_FORMAT_ERROR_MSG, e, messageContext);
        } catch (Exception e) {
            Utils.setErrorPropertiesToMessage(messageContext, Error.GENERAL_ERROR, e.getMessage());
            handleException(Constants.GENERAL_ERROR_MSG + e.getMessage(), messageContext);
        }
    }

    private JsonObject createListDeadLetterSourceQueuesJsonResponse(ListDeadLetterSourceQueuesResponse listQueueUrlResponse) {
        JsonObject resultJson = Utils.createResponseMetaDataElement(listQueueUrlResponse.responseMetadata());

        JsonObject listDeadLetterSourceQueuesResult = new JsonObject();

        // Add queue URLs
        JsonArray queueUrlsArray = new JsonArray();
        for (String url : listQueueUrlResponse.queueUrls()) {
            queueUrlsArray.add(url);
        }
        listDeadLetterSourceQueuesResult.add(Constants.QUEUE_URLS, queueUrlsArray);

        // Add pagination token if present
        if (listQueueUrlResponse.nextToken() != null) {
            listDeadLetterSourceQueuesResult.addProperty(Constants.NEXT_TOKEN_KEY, listQueueUrlResponse.nextToken());
        }

        resultJson.add(Constants.LIST_DEAD_LETTER_SOURCE_QUEUES_RESULT, listDeadLetterSourceQueuesResult);
        return resultJson;
    }
}
