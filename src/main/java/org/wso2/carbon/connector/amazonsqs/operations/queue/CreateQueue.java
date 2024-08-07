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

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.amazonsqs.connection.SqsConnection;
import org.wso2.carbon.connector.amazonsqs.constants.Constants;
import org.wso2.carbon.connector.amazonsqs.exception.SqsInvalidConfigurationException;
import org.wso2.carbon.connector.amazonsqs.utils.Error;
import org.wso2.carbon.connector.amazonsqs.utils.Utils;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements create queue operation.
 */
public class CreateQueue extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            SqsConnection sqsConnection = (SqsConnection) handler
                    .getConnection(Constants.CONNECTOR_NAME, Utils.getConnectionName(messageContext));
            CreateQueueRequest.Builder createQueueRequest = CreateQueueRequest.builder()
                    .queueName((String) ConnectorUtils.lookupTemplateParamater(messageContext,
                            Constants.QUEUE_NAME));
            String attributeEntries = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.ATTRIBUTE_ENTRIES);
            String apiCallTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_TIMEOUT);
            String apiCallAttemptTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_ATTEMPT_TIMEOUT);
            if (StringUtils.isNotBlank(attributeEntries)) {
                createQueueRequest.attributes(Utils.addAttributes(Utils.removeDoubleQuotes(attributeEntries.trim())));
            }
            String tags = (String) ConnectorUtils.lookupTemplateParamater(messageContext, Constants.TAGS);
            if (StringUtils.isNotBlank(tags)) {
                createQueueRequest.tags(Utils.addTags(Utils.removeDoubleQuotes(tags.trim())));
            }
            if (StringUtils.isNotBlank(apiCallTimeout) || StringUtils.isNotBlank(apiCallAttemptTimeout)) {
                createQueueRequest.overrideConfiguration(
                        Utils.getOverrideConfiguration(apiCallTimeout, apiCallAttemptTimeout).build());
            }
            CreateQueueResponse createQueueResponse = sqsConnection.getSqsClient().
                    createQueue(createQueueRequest.build());
            OMElement resultElement = Utils.createOMElement("CreateQueueResponse", null);
            resultElement.addChild(Utils.createOMElement("CreateQueueResult",
                    Utils.createOMElement(Constants.QUEUE_URL_KEY, createQueueResponse.queueUrl())));
            Utils.createResponseMetaDataElement(createQueueResponse.responseMetadata(), messageContext, resultElement);
        } catch (SqsException e) {
            Utils.addErrorResponse(messageContext, e);
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
}
