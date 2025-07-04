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
import org.wso2.carbon.connector.amazonsqs.connection.SqsConnection;
import org.wso2.carbon.connector.amazonsqs.constants.Constants;
import org.wso2.carbon.connector.amazonsqs.exception.SqsInvalidConfigurationException;
import org.wso2.carbon.connector.amazonsqs.utils.Error;
import org.wso2.carbon.connector.amazonsqs.utils.Utils;
import org.wso2.integration.connector.core.AbstractConnectorOperation;
import org.wso2.integration.connector.core.ConnectException;
import org.wso2.integration.connector.core.connection.ConnectionHandler;
import org.wso2.integration.connector.core.util.ConnectorUtils;
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
public class ReceiveMessage extends AbstractConnectorOperation {

    @Override
    public void execute(MessageContext messageContext, String responseVariable, Boolean overwriteBody)
            throws ConnectException {
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
                receiveMessageBuilder.maxNumberOfMessages(Integer.parseInt(maxNumberOfMessages));
            }
            if (StringUtils.isNotBlank(waitTimeSeconds)) {
                receiveMessageBuilder.waitTimeSeconds(Integer.parseInt(waitTimeSeconds));
            }
            if (StringUtils.isNotBlank(messageAttributeNames)) {
                List<String> names = new ArrayList<String>();
                for (String key : messageAttributeNames.split(",")) {
                    names.add(key.trim());
                }
                receiveMessageBuilder.messageAttributeNames(names);
            }
            if (StringUtils.isNotBlank(visibilityTimeout)) {
                receiveMessageBuilder.visibilityTimeout(Integer.parseInt(visibilityTimeout));
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
            JsonObject resultJSON = createReceiveMessageJsonResponse(receiveMessageResponse);
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

    private JsonObject createReceiveMessageJsonResponse(ReceiveMessageResponse receiveMessageResponse) {
        JsonObject resultJson = Utils.createResponseMetaDataElement(receiveMessageResponse.responseMetadata());
        
        JsonObject receiveMessageResult = new JsonObject();
        JsonArray messagesArray = new JsonArray();
        
        List<Message> messages = receiveMessageResponse.messages();
        for (Message msg : messages) {
            JsonObject messageJson = new JsonObject();
            messageJson.addProperty(Constants.MESSAGE_ID, msg.messageId());
            messageJson.addProperty(Constants.MD5_OF_BODY, msg.md5OfBody());
            messageJson.addProperty(Constants.BODY, msg.body());
            messageJson.addProperty(Constants.RECEIPT_HANDLE, msg.receiptHandle());
            
            String md5OfMessageAttributes = msg.md5OfMessageAttributes();
            if (StringUtils.isNotBlank(md5OfMessageAttributes)) {
                messageJson.addProperty(Constants.MD5_OF_MESSAGE_ATTRIBUTES, md5OfMessageAttributes);
            }
            
            // Add message attributes
            JsonArray messageAttributesArray = new JsonArray();
            for (Map.Entry<String, MessageAttributeValue> entry : msg.messageAttributes().entrySet()) {
                JsonObject messageAttribute = new JsonObject();
                messageAttribute.addProperty(Constants.NAME, entry.getKey());
                
                JsonObject value = new JsonObject();
                MessageAttributeValue values = entry.getValue();
                
                if (values.binaryListValues().size() > 0) {
                    JsonArray binaryListValues = new JsonArray();
                    for (SdkBytes sdkBytes : values.binaryListValues()) {
                        binaryListValues.add(sdkBytes.asUtf8String());
                    }
                    value.add(Constants.BINARY_LIST_VALUES, binaryListValues);
                }
                
                SdkBytes binaryValue = values.binaryValue();
                if (binaryValue != null) {
                    value.addProperty(Constants.BINARY_VALUE, binaryValue.asUtf8String());
                }
                
                String dataType = values.dataType();
                if (StringUtils.isNotBlank(dataType)) {
                    value.addProperty(Constants.DATA_TYPE, dataType);
                }
                
                String stringValue = values.stringValue();
                if (StringUtils.isNotBlank(stringValue)) {
                    value.addProperty(Constants.STRING_VALUE, stringValue);
                }
                
                List<String> stringListValues = values.stringListValues();
                if (stringListValues.size() > 0) {
                    JsonArray stringListValuesArray = new JsonArray();
                    for (String stringListValue : stringListValues) {
                        stringListValuesArray.add(stringListValue);
                    }
                    value.add(Constants.STRING_LIST_VALUES, stringListValuesArray);
                }
                
                messageAttribute.add(Constants.VALUE, value);
                messageAttributesArray.add(messageAttribute);
            }
            if (messageAttributesArray.size() > 0) {
                messageJson.add(Constants.MESSAGE_ATTRIBUTES, messageAttributesArray);
            }
            
            // Add system attributes
            JsonArray attributesArray = new JsonArray();
            for (Map.Entry<MessageSystemAttributeName, String> entry : msg.attributes().entrySet()) {
                JsonObject attribute = new JsonObject();
                attribute.addProperty(Constants.NAME, entry.getKey().name());
                attribute.addProperty(Constants.VALUE, entry.getValue());
                attributesArray.add(attribute);
            }
            if (attributesArray.size() > 0) {
                messageJson.add(Constants.ATTRIBUTES, attributesArray);
            }
            
            messagesArray.add(messageJson);
        }
        
        receiveMessageResult.add(Constants.MESSAGES, messagesArray);
        resultJson.add(Constants.RECEIVE_MESSAGE_RESULT, receiveMessageResult);
        resultJson.addProperty(Constants.SUCCESS, true);

        // Add response metadata
        return resultJson;
    }
}
