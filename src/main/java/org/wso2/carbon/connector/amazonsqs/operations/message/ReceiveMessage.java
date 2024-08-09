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
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements receive message operation.
 */
public class ReceiveMessage extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            SqsConnection sqsConnection = (SqsConnection) handler.getConnection(Constants.CONNECTOR_NAME,
                    Utils.getConnectionName(messageContext));
            String queueUrl = Utils.createUrl(messageContext, sqsConnection);
            String maxNumberOfMessages = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MAX_NUMBER_OF_MESSAGES);
            String waitTimeSeconds = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.WAIT_TIME_SECONDS);
            String messageAttributeNames = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MESSAGE_ATTRIBUTE_NAMES);
            String visibilityTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.VISIBILITY_TIME_OUT);
            String messageSystemAttributes = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.MESSAGE_SYSTEM_ATTRIBUTE_NAMES);
            String apiCallTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_TIMEOUT);
            String apiCallAttemptTimeout = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    Constants.API_CALL_ATTEMPT_TIMEOUT);
            ReceiveMessageRequest.Builder receiveMessageBuilder = ReceiveMessageRequest.builder().
                    queueUrl(queueUrl);
            if (StringUtils.isNotBlank(maxNumberOfMessages)) {
                receiveMessageBuilder.maxNumberOfMessages(Integer.getInteger(maxNumberOfMessages));
            }
            if (StringUtils.isNotBlank(waitTimeSeconds)) {
                receiveMessageBuilder.waitTimeSeconds(Integer.getInteger(waitTimeSeconds));
            }
            if (StringUtils.isNotBlank(messageAttributeNames)) {
                List<String> names = new ArrayList<String>();
                for (String key : messageAttributeNames.split(",")) {
                    names.add(key.trim());
                }
                receiveMessageBuilder.messageAttributeNames(names);
            }
            if (StringUtils.isNotBlank(visibilityTimeout)) {
                receiveMessageBuilder.visibilityTimeout(Integer.getInteger(visibilityTimeout));
            }
            if (StringUtils.isNotBlank(messageSystemAttributes)) {
                List<String> names = new ArrayList<String>();
                for (String key : messageSystemAttributes.split(",")) {
                    names.add(key.trim());
                }
                receiveMessageBuilder.messageSystemAttributeNamesWithStrings(names);
            }
            if (StringUtils.isNotBlank(apiCallTimeout) || StringUtils.isNotBlank(apiCallAttemptTimeout)) {
                receiveMessageBuilder.overrideConfiguration(
                        Utils.getOverrideConfiguration(apiCallTimeout, apiCallAttemptTimeout).build());
            }
            ReceiveMessageResponse receiveMessageResponse = sqsConnection.getSqsClient().
                    receiveMessage(receiveMessageBuilder.build());
            addResponse(receiveMessageResponse, messageContext);
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

    private void addResponse(ReceiveMessageResponse receiveMessageResponse, MessageContext messageContext) {
        OMElement resultElement = Utils.createOMElement("ReceiveMessageResponse", null);
        OMElement result = Utils.createOMElement("ReceiveMessageResult", null);

        List<Message> messages = receiveMessageResponse.messages();
        for (Message msg : messages) {
            OMElement message = Utils.createOMElement(Constants.MESSAGE, null);
            message.addChild(Utils.createOMElement(Constants.MESSAGE_ID, msg.messageId()));
            message.addChild(Utils.createOMElement(Constants.MD5_OF_BODY, msg.md5OfBody()));
            message.addChild(Utils.createOMElement(Constants.BODY, msg.body()));
            message.addChild(Utils.createOMElement(Constants.RECEIPT_HANDLE, msg.receiptHandle()));
            String md5OfMessageAttributes = msg.md5OfMessageAttributes();
            if (StringUtils.isNotBlank(md5OfMessageAttributes)) {
                message.addChild(Utils.createOMElement(Constants.MD5_OF_MESSAGE_ATTRIBUTES,
                        msg.md5OfMessageAttributes()));
            }
            for (Map.Entry<String, MessageAttributeValue> entry : msg.messageAttributes().entrySet()) {
                OMElement omMessageAttribute = Utils.createOMElement(Constants.MESSAGE_ATTRIBUTE,
                        null);
                omMessageAttribute.addChild(Utils.createOMElement(Constants.NAME, entry.getKey()));
                OMElement value = Utils.createOMElement(Constants.VALUE, null);
                MessageAttributeValue values = entry.getValue();
                if (values.binaryListValues().size() > 0) {
                    OMElement binaryListValues = Utils.createOMElement(Constants.BINARY_LIST_VALUES,
                            null);
                    for (SdkBytes sdkBytes : values.binaryListValues()) {
                        binaryListValues.addChild(Utils.createOMElement(Constants.BINARY, sdkBytes));
                    }
                    value.addChild(binaryListValues);
                }
                SdkBytes binaryValue = values.binaryValue();
                if (binaryValue != null) {
                    value.addChild(Utils.createOMElement(Constants.BINARY_VALUE, binaryValue));
                }
                String dataType = values.dataType();
                if (StringUtils.isNotBlank(dataType)) {
                    value.addChild(Utils.createOMElement(Constants.DATA_TYPE, dataType));
                }
                String stringValue = values.stringValue();
                if (StringUtils.isNotBlank(stringValue)) {
                    value.addChild(Utils.createOMElement(Constants.STRING_VALUE, stringValue));
                }
                List<String> stringListValues = values.stringListValues();
                if (stringListValues.size() > 0) {
                    OMElement stringListValuesElement = Utils.createOMElement(Constants.STRING_LIST_VALUES, null);
                    for (String stringListValue : stringListValues) {
                        stringListValuesElement.addChild(Utils.createOMElement(Constants.STRING, stringListValue));
                    }
                    value.addChild(stringListValuesElement);
                }
                omMessageAttribute.addChild(value);
                message.addChild(omMessageAttribute);
            }
            for (Map.Entry<MessageSystemAttributeName, String> entry : msg.attributes().entrySet()) {
                OMElement messageSystemAttribute = Utils.createOMElement(Constants.ATTRIBUTE, null);
                messageSystemAttribute.addChild(Utils.createOMElement(Constants.NAME, entry.getKey().name()));
                messageSystemAttribute.addChild(Utils.createOMElement(Constants.VALUE, entry.getValue()));
                message.addChild(messageSystemAttribute);
            }
            result.addChild(message);
        }
        resultElement.addChild(result);
        Utils.createResponseMetaDataElement(receiveMessageResponse.responseMetadata(), messageContext, resultElement);
    }
}
