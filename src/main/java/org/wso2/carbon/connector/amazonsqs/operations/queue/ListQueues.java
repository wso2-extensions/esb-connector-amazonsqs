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
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * Implements list queues operation.
 */
public class ListQueues extends AbstractConnectorOperation {

    @Override
    public void execute(MessageContext messageContext, String responseVariable, Boolean overwriteBody)
            throws ConnectException {
        try {
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            SqsConnection sqsConnection = (SqsConnection) handler
                    .getConnection(Constants.CONNECTOR_NAME, Utils.getConnectionName(messageContext));
            String queueNamePrefix = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.QUEUE_NAME_PREFIX);
            String apiCallTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_TIMEOUT);
            String apiCallAttemptTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_ATTEMPT_TIMEOUT);
            String nextToken = (String) ConnectorUtils.lookupTemplateParamater(messageContext, Constants.NEXT_TOKEN);
            String maxResults = (String) ConnectorUtils.lookupTemplateParamater(messageContext, Constants.MAX_RESULT);
            ListQueuesRequest.Builder listQueuesRequestBuilder = ListQueuesRequest.builder();
            if (StringUtils.isNotBlank(queueNamePrefix)) {
                listQueuesRequestBuilder.queueNamePrefix(queueNamePrefix);
            }
            if (StringUtils.isNotBlank(nextToken)) {
                listQueuesRequestBuilder.nextToken(nextToken);
            }
            if (StringUtils.isNotBlank(maxResults)) {
                listQueuesRequestBuilder.maxResults(Integer.getInteger(maxResults));
            }
            if (StringUtils.isNotBlank(apiCallTimeout) || StringUtils.isNotBlank(apiCallAttemptTimeout)) {
                listQueuesRequestBuilder.overrideConfiguration(
                        Utils.getOverrideConfiguration(apiCallTimeout, apiCallAttemptTimeout).build());
            }
            ListQueuesResponse listQueueUrlResponse = sqsConnection.getSqsClient().
                    listQueues(listQueuesRequestBuilder.build());

            JsonObject resultJSON = createListQueuesJsonResponse(listQueueUrlResponse);
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

    private JsonObject createListQueuesJsonResponse(ListQueuesResponse listQueuesResponse) {
        JsonObject resultJson = Utils.createResponseMetaDataElement(listQueuesResponse.responseMetadata());

        JsonObject listQueuesResult = new JsonObject();
        JsonArray queueUrlsArray = new JsonArray();

        for (String url : listQueuesResponse.queueUrls()) {
            queueUrlsArray.add(url);
        }

        listQueuesResult.add(Constants.QUEUE_URLS, queueUrlsArray);
        if (listQueuesResponse.nextToken() != null) {
            listQueuesResult.addProperty(Constants.NEXT_TOKEN_KEY, listQueuesResponse.nextToken());
        }

        resultJson.add(Constants.LIST_QUEUES_RESULT, listQueuesResult);

        return resultJson;
    }
}
