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
package org.wso2.carbon.connector.amazonsqs.connection;

import org.wso2.carbon.connector.amazonsqs.object.SqsClientManager;
import org.wso2.carbon.connector.core.connection.Connection;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * This class manages connections to Amazon SQS using the AWS SDK.
 */
public class SqsConnection implements Connection {

    private ConnectionConfiguration connectionConfig;
    private SqsClientManager sqsClientManager;

    public SqsConnection(ConnectionConfiguration connectionConfig) {
        this.connectionConfig = connectionConfig;
        this.sqsClientManager = SqsClientManager.getInstance(connectionConfig);
    }

    public ConnectionConfiguration getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfiguration connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public SqsClient getSqsClient() {
        return this.sqsClientManager.getSqsClient();
    }

    public void setSqsInstance() {
        this.sqsClientManager = SqsClientManager.createNewInstance(this.connectionConfig);
    }
}
