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
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.net.MalformedURLException;

/**
 * Implements delete message operation.
 */
public class DeleteMessage extends AbstractConnector {

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String operationName = "deleteMessage";
        try {
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            SqsConnection sqsConnection = (SqsConnection) handler
                    .getConnection(Constants.CONNECTOR_NAME, Utils.getConnectionName(messageContext));
            String queueUrl = Utils.createUrl(messageContext, sqsConnection);
            String receiptHandle = (String) ConnectorUtils.lookupTemplateParamater(messageContext,
                    "receiptHandle");
            DeleteMessageRequest deleteQueueRequest = DeleteMessageRequest.builder().queueUrl(queueUrl).
                    receiptHandle(receiptHandle).build();
            DeleteMessageResponse deleteMessageResponse = sqsConnection.getSqsClient().
                    deleteMessage(deleteQueueRequest);
            Utils.add200ResponseWithOutBody(deleteMessageResponse.responseMetadata(), messageContext,
                    "DeleteMessageResponse");
        } catch (SqsException e) {
            Utils.addErrorResponse(messageContext, e);
        } catch (SdkClientException e) {
            Utils.setResultAsPayload(messageContext, operationName , Error.CONNECTION_ERROR, e.getMessage());
            handleException(Constants.CLIENT_EXCEPTION_MSG, e, messageContext);
        } catch (MalformedURLException e) {
            Utils.setResultAsPayload(messageContext, operationName , Error.INVALID_URL, e.getMessage());
            handleException(Constants.RUN_TIME_EXCEPTION_MSG, e, messageContext);
        } catch (SqsInvalidConfigurationException|NumberFormatException e) {
            Utils.setResultAsPayload(messageContext, operationName , Error.INVALID_CONFIGURATION, e.getMessage());
            handleException(Constants.GENERAL_ERROR_MSG, e, messageContext);
        } catch (Exception e) {
            Utils.setResultAsPayload(messageContext, operationName , Error.GENERAL_ERROR, e.getMessage());
            handleException(Constants.GENERAL_ERROR_MSG + e.getMessage(), messageContext);
        }
    }
}
