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
package org.wso2.carbon.connector.amazonsqs.config;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.connector.amazonsqs.connection.ConnectionConfiguration;
import org.wso2.carbon.connector.amazonsqs.connection.SqsConnection;
import org.wso2.carbon.connector.amazonsqs.constants.Constants;
import org.wso2.carbon.connector.amazonsqs.exception.SqsInvalidConfigurationException;
import org.wso2.carbon.connector.amazonsqs.utils.Error;
import org.wso2.carbon.connector.amazonsqs.utils.Utils;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.connection.ConnectionHandler;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

/**
 * Initializes the AWS connection based on provided configs.
 */
public class Init extends AbstractConnector implements ManagedLifecycle {

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {}

    @Override
    public void destroy() {
        ConnectionHandler.getConnectionHandler().shutdownConnections(Constants.CONNECTOR_NAME);
    }

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {
            ConnectionConfiguration configuration = getConnectionConfigFromContext(messageContext);
            String connectionName = configuration.getConnectionName();
            ConnectionHandler handler = ConnectionHandler.getConnectionHandler();
            if (!handler.checkIfConnectionExists(Constants.CONNECTOR_NAME, connectionName)) {
                SqsConnection sqsConnection = new SqsConnection(configuration);
                handler.createConnection(Constants.CONNECTOR_NAME, connectionName, sqsConnection);
            } else {
                SqsConnection sqsConnection = (SqsConnection) handler
                        .getConnection(Constants.CONNECTOR_NAME, connectionName);
                if (!sqsConnection.getConnectionConfig().equals(configuration)) {
                    sqsConnection.setConnectionConfig(configuration);
                    sqsConnection.setSqsInstance();
                }
            }
        } catch (SqsInvalidConfigurationException e) {
            Utils.setErrorPropertiesToMessage(messageContext, Error.INVALID_CONFIGURATION, e.getMessage());
            handleException("Failed to initiate sqs connector configuration.", e, messageContext);
        }
    }

    private ConnectionConfiguration getConnectionConfigFromContext(MessageContext msgContext)
            throws SqsInvalidConfigurationException {
        String connectionName = (String) ConnectorUtils.
                lookupTemplateParamater(msgContext, Constants.CONNECTION_NAME);
        String region = (String) ConnectorUtils.
                lookupTemplateParamater(msgContext, Constants.REGION);
        String awsAccessKeyId = (String) ConnectorUtils.
                lookupTemplateParamater(msgContext, Constants.AWS_ACCESS_KEY_ID);
        String awsSecretAccessKey = (String) ConnectorUtils.
                lookupTemplateParamater(msgContext, Constants.AWS_SECRET_ACCESS_KEY);

        ConnectionConfiguration connectionConfig = new ConnectionConfiguration();
        connectionConfig.setConnectionName(connectionName);
        connectionConfig.setRegion(region);
        connectionConfig.setAwsAccessKeyId(awsAccessKeyId);
        connectionConfig.setAwsSecretAccessKey(awsSecretAccessKey);
        return connectionConfig;
    }
}
