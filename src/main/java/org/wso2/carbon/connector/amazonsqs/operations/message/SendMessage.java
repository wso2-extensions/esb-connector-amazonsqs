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
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * Implements send message operation.
 */
public class SendMessage extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            SqsConnection sqsConnection = (SqsConnection) handler
                    .getConnection(Constants.CONNECTOR_NAME, Utils.getConnectionName(messageContext));
            String queueUrl = Utils.createUrl(messageContext, sqsConnection);
            String delaySeconds = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.DELAY_SECONDS);
            String messageAttributes = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MESSAGE_ATTRIBUTES);
            String messageBody = ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MESSAGE_BODY).toString();
            String messageGroupId = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MESSAGE_GROUP_ID);
            String messageDeduplicationId = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MESSAGE_DEDUPLICATION_ID);
            String messageSystemAttributes = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MESSAGE_SYSTEM_ATTRIBUTES);
            String apiCallTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_TIMEOUT);
            String apiCallAttemptTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_ATTEMPT_TIMEOUT);
            SendMessageRequest.Builder sendMessageBuilder = SendMessageRequest.builder().
                    queueUrl(queueUrl).messageBody(messageBody);

            if (StringUtils.isNotBlank(delaySeconds)) {
                sendMessageBuilder.delaySeconds(Integer.valueOf(delaySeconds));
            }
            if (StringUtils.isNotBlank(messageGroupId)) {
                sendMessageBuilder.messageGroupId(messageGroupId);
            }
            if (StringUtils.isNotBlank(messageDeduplicationId)) {
                sendMessageBuilder.messageDeduplicationId(messageDeduplicationId);
            }
            if (StringUtils.isNotBlank(messageAttributes)) {
                sendMessageBuilder.messageAttributes(Utils.addMessageAttributes(
                        Utils.removeDoubleQuotes(messageAttributes.trim())));
            }
            if (StringUtils.isNotBlank(messageSystemAttributes)) {
                sendMessageBuilder.messageSystemAttributes(Utils.addSystemMessageAttributes(
                        Utils.removeDoubleQuotes(messageSystemAttributes.trim())));
            }
            if (StringUtils.isNotBlank(apiCallTimeout) || StringUtils.isNotBlank(apiCallAttemptTimeout)) {
                sendMessageBuilder.overrideConfiguration(
                        Utils.getOverrideConfiguration(apiCallTimeout, apiCallAttemptTimeout).build());
            }
            createResponse(sqsConnection.getSqsClient().sendMessage(sendMessageBuilder.build()), messageContext);
        } catch (SqsException e) {
            Utils.addErrorResponse(messageContext, e);
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

    private void createResponse(SendMessageResponse sendMessageResponse, MessageContext messageContext) {
        OMElement resultElement = Utils.createOMElement("SendMessageResponse", null);
        OMElement result = Utils.createOMElement("SendMessageResult", null);
        result.addChild(Utils.createOMElement(Constants.MESSAGE_ID, sendMessageResponse.messageId()));
        result.addChild(Utils.createOMElement(Constants.MD5_OF_MESSAGE_BODY, sendMessageResponse.md5OfMessageBody()));
        String md5OfMessageAttributes = sendMessageResponse.md5OfMessageAttributes();
        if (StringUtils.isNotBlank(md5OfMessageAttributes)) {
            result.addChild(Utils.createOMElement(Constants.MD5_OF_MESSAGE_ATTRIBUTES, md5OfMessageAttributes));
        }
        String md5OfMessageSystemAttributes = sendMessageResponse.md5OfMessageSystemAttributes();
        if (StringUtils.isNotBlank(md5OfMessageSystemAttributes)) {
            result.addChild(Utils.createOMElement(Constants.MD5_OF_MESSAGE_SYSTEM_ATTRIBUTES,
                    md5OfMessageSystemAttributes));
        }
        resultElement.addChild(result);
        Utils.createResponseMetaDataElement(sendMessageResponse.responseMetadata(), messageContext, resultElement);
        Utils.setStatusCode(messageContext, "200");
    }
}
