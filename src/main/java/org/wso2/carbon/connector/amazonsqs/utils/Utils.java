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
package org.wso2.carbon.connector.amazonsqs.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.connector.amazonsqs.connection.SqsConnection;
import org.wso2.carbon.connector.amazonsqs.constants.Constants;
import org.wso2.carbon.connector.amazonsqs.exception.SqsInvalidConfigurationException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeNameForSends;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsResponseMetadata;

import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contain required util methods for to Amazon sqs connector.
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static OMElement createOMElement(String elementName, Object value) {
        OMElement resultElement = null;
        try {
            if (value != null) {
                resultElement = AXIOMUtil.
                        stringToOM("<" + elementName + ">" + value
                                + "</" + elementName + ">");
            } else {
                resultElement = AXIOMUtil.
                        stringToOM("<" + elementName
                                + "></" + elementName + ">");
            }
        } catch (XMLStreamException | OMException e) {
            log.error("Error while generating OMElement from element name" + elementName, e);
        }
        return resultElement;
    }

    public static String createUrl(MessageContext messageContext, SqsConnection sqsConnection)
            throws MalformedURLException, SqsInvalidConfigurationException {
        try {
            String queueName = (String) ConnectorUtils.lookupTemplateParamater(messageContext, Constants.QUEUE_NAME);
            String queueId = (String) ConnectorUtils.lookupTemplateParamater(messageContext, Constants.QUEUE_ID);
            String queueUrl = (String) ConnectorUtils.lookupTemplateParamater(messageContext, Constants.QUEUE_URL);
            if (StringUtils.isNotBlank(queueUrl)) {
                return queueUrl;
            }
            if (StringUtils.isNotBlank(queueName) && (StringUtils.isNotBlank(queueId) ||
                    StringUtils.isNotBlank(queueUrl))) {
                throw new SqsInvalidConfigurationException("Missing queue info: Should provide value of queue url or " +
                        "queue name and queue id.");
            }
            return new URL(Constants.HTTPS, "sqs".concat(sqsConnection.getConnectionConfig().getRegion()).
                    concat(".amazonaws.com"), Constants.SLASH.concat(queueId).
                    concat(Constants.SLASH).concat(queueName)).toString();
        } catch (MalformedURLException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    public static void add200ResponseWithOutBody(SqsResponseMetadata responseMetadata,
                                                 MessageContext messageContext, String parentElementName) {
        OMElement resultElement = Utils.createOMElement(parentElementName, null);
        createResponseMetaDataElement(responseMetadata, messageContext, resultElement);
        setStatusCode(messageContext, "200");
    }

    public static void createResponseMetaDataElement(SqsResponseMetadata responseMetadata,
                                                     MessageContext messageContext, OMElement resultElement) {
        resultElement.addChild(Utils.createOMElement("ResponseMetadata", responseMetadata.requestId()));
        SOAPBody soapBody = messageContext.getEnvelope().getBody();
        JsonUtil.removeJsonPayload(((Axis2MessageContext) messageContext).getAxis2MessageContext());
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                removeProperty(PassThroughConstants.NO_ENTITY_BODY);
        soapBody.addChild(resultElement);
    }

    public static void addErrorResponse(MessageContext messageContext, AwsServiceException e) {
        OMElement resultElement = Utils.createOMElement("ErrorResponse", null);
        OMElement error = Utils.createOMElement("Error", null);
        error.addChild(Utils.createOMElement("Type", e.awsErrorDetails().serviceName()));
        error.addChild(Utils.createOMElement("Code", e.awsErrorDetails().errorCode()));
        error.addChild(Utils.createOMElement("Message", e.awsErrorDetails().errorMessage()));
        error.addChild(Utils.createOMElement("Detail", null));
        resultElement.addChild(error);
        resultElement.addChild(Utils.createOMElement("RequestId", e.requestId()));
        SOAPBody soapBody = messageContext.getEnvelope().getBody();
        JsonUtil.removeJsonPayload(((Axis2MessageContext) messageContext).getAxis2MessageContext());
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                removeProperty(PassThroughConstants.NO_ENTITY_BODY);
        soapBody.addChild(resultElement);
        setStatusCode(messageContext, String.valueOf(e.statusCode()));
    }

    public static void setStatusCode(MessageContext messageContext, String statusCode) {
        org.apache.axis2.context.MessageContext axis2MessageCtx = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        axis2MessageCtx.setProperty(Constants.STATUS_CODE, statusCode);
    }

    public static void addMessageAttributes(String messageAttributes, SendMessageRequest.Builder sendMessageBuilder) {
        JSONObject messageAttributesInJson = new JSONObject(messageAttributes);
        Iterator<?> keys = messageAttributesInJson.keys();
        Map<String, MessageAttributeValue> messageAttributeValueMap = new HashMap<>();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject messageAttributeInJson = (JSONObject) messageAttributesInJson.get(key);
            Set keySet = messageAttributeInJson.keySet();
            MessageAttributeValue.Builder messageAttributeValueBuilder = MessageAttributeValue.builder();

            if (keySet.contains(Constants.DATA_TYPE)) {
                messageAttributeValueBuilder.dataType((String) messageAttributeInJson.get(Constants.DATA_TYPE));
            }

            if (keySet.contains(Constants.BINARY_VALUE)) {
                messageAttributeValueBuilder.binaryValue(SdkBytes.fromUtf8String(
                        messageAttributeInJson.get(Constants.BINARY_VALUE).toString()));
            }

            if (keySet.contains(Constants.STRING_VALUE)) {
                messageAttributeValueBuilder.stringValue(messageAttributeInJson.get(Constants.STRING_VALUE).
                        toString());
            }

            if (keySet.contains(Constants.STRING_LIST_VALUE)) {
                JSONArray arr = new JSONArray(messageAttributeInJson.get(Constants.STRING_LIST_VALUE).toString());
                List<String> stringList = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    stringList.add(arr.get(i).toString());
                }
                messageAttributeValueBuilder.stringListValues(stringList);
            }
            if (keySet.contains(Constants.BINARY_LIST_VALUE)) {
                JSONArray arr = new JSONArray(messageAttributeInJson.get(Constants.BINARY_LIST_VALUE).toString());
                List<SdkBytes> binaryList = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    binaryList.add(SdkBytes.fromUtf8String(arr.get(i).toString()));
                }
                messageAttributeValueBuilder.binaryListValues(binaryList);
            }
            messageAttributeValueMap.put(key, messageAttributeValueBuilder.build());
        }
        sendMessageBuilder.messageAttributes(messageAttributeValueMap);
    }

    public static void addSystemMessageAttributes(String messageSystemAttributes,
                                                  SendMessageRequest.Builder sendMessageBuilder) {
        JSONObject messageAttributesInJson = new JSONObject(messageSystemAttributes);
        Iterator<?> keys = messageAttributesInJson.keys();
        Map<MessageSystemAttributeNameForSends, MessageSystemAttributeValue> messageSystemAttributeMap =
                new HashMap<>();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject messageAttributeInJson = (JSONObject) messageAttributesInJson.get(key);
            Set keySet = messageAttributeInJson.keySet();
            MessageSystemAttributeValue.Builder messageSystemAttributeValue = MessageSystemAttributeValue.builder();
            if (keySet.contains(Constants.DATA_TYPE)) {
                messageSystemAttributeValue.dataType(messageAttributeInJson.get(Constants.DATA_TYPE).toString());
            }

            if (keySet.contains(Constants.BINARY_VALUE)) {
                messageSystemAttributeValue.binaryValue(SdkBytes.fromUtf8String(
                        messageAttributeInJson.get(Constants.BINARY_VALUE).toString()));
            }

            if (keySet.contains(Constants.STRING_VALUE)) {
                messageSystemAttributeValue.stringValue(messageAttributeInJson.get(
                        Constants.STRING_VALUE).toString());
            }

            if (keySet.contains(Constants.STRING_LIST_VALUE)) {
                JSONArray arr = new JSONArray(messageAttributeInJson.get(Constants.STRING_LIST_VALUE).toString());
                List<String> stringList = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    stringList.add(arr.get(i).toString());
                }
                messageSystemAttributeValue.stringListValues(stringList);
            }

            if (keySet.contains(Constants.BINARY_LIST_VALUE)) {
                JSONArray arr = new JSONArray(messageAttributeInJson.get(Constants.BINARY_LIST_VALUE).toString());
                List<SdkBytes> binaryList = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    binaryList.add(SdkBytes.fromUtf8String(arr.get(i).toString()));
                }
                messageSystemAttributeValue.binaryListValues(binaryList);
            }
            messageSystemAttributeMap.put(MessageSystemAttributeNameForSends.fromValue(key),
                    messageSystemAttributeValue.build());
        }
        sendMessageBuilder.messageSystemAttributes(messageSystemAttributeMap);
    }

    /**
     * Retrieves connection name from message context if configured as configKey attribute
     * or from the template parameter
     *
     * @param messageContext Message Context from which the parameters should be extracted from
     * @return connection name
     */
    public static String getConnectionName(MessageContext messageContext) throws SqsInvalidConfigurationException {
        String connectionName = (String) messageContext.getProperty(Constants.CONNECTION_NAME);
        if (connectionName == null) {
            throw new SqsInvalidConfigurationException("Mandatory parameter 'connectionName' is not set.");
        }
        return connectionName;
    }

    public static void setResultAsPayload(MessageContext msgContext, String operationName, Error error,
                                          String errorMsg) {
        OMElement resultElement = createOMElement(operationName + "Result", null);

        OMElement statusCodeElement = createOMElement("success", "false");
        resultElement.addChild(statusCodeElement);

        setErrorPropertiesToMessage(msgContext, error, errorMsg);

        //set error code and detail to the message
        OMElement errorEle = createOMElement("error", null);
        OMElement errorTyoe = createOMElement("Type", error.toString());
        OMElement errorCodeEle = createOMElement("code", error.getErrorCode());
        OMElement errorMessageEle = createOMElement("message", errorMsg);
        errorEle.addChild(errorTyoe);
        errorEle.addChild(errorCodeEle);
        errorEle.addChild(errorMessageEle);
        resultElement.addChild(errorEle);

        SOAPBody soapBody = msgContext.getEnvelope().getBody();
        JsonUtil.removeJsonPayload(((Axis2MessageContext)msgContext).getAxis2MessageContext());
        ((Axis2MessageContext)msgContext).getAxis2MessageContext().
                removeProperty(PassThroughConstants.NO_ENTITY_BODY);
        soapBody.addChild(resultElement);
    }

    /**
     * Sets the error code and error detail in message
     *
     * @param messageContext Message Context
     * @param error          Error to be set
     */
    public static void setErrorPropertiesToMessage(MessageContext messageContext, Error error, String errorDetail) {
        messageContext.setProperty(Constants.PROPERTY_ERROR_CODE, error.getErrorCode());
        messageContext.setProperty(Constants.PROPERTY_ERROR_MESSAGE, error.getErrorMessage());
        messageContext.setProperty(Constants.PROPERTY_ERROR_MESSAGE, errorDetail);
        setStatusCode(messageContext, "400");
    }
}
