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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.connector.amazonsqs.exception.SqsInvalidConfigurationException;

/**
 * The class encapsulates the configuration details required to establish a connection to Amazon SQS.
 */
public class ConnectionConfiguration {
    public String region;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;

    private String connectionName;

    public String getRegion() {
        return region;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) throws SqsInvalidConfigurationException {
        if (StringUtils.isNotEmpty(connectionName)) {
            this.connectionName = connectionName;
        } else {
            throw new SqsInvalidConfigurationException("Mandatory parameter 'connectionName' is not set.");
        }
    }

    public void setRegion(String region) throws SqsInvalidConfigurationException {
        if (StringUtils.isNotEmpty(region)) {
            this.region = region;
        } else {
            throw new SqsInvalidConfigurationException("Mandatory parameter 'region' is not set.");
        }
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ConnectionConfiguration) {
            ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration) obj;
            return StringUtils.equals(this.connectionName, connectionConfiguration.getConnectionName()) &&
                    StringUtils.equals(this.region, connectionConfiguration.getRegion()) &&
                    StringUtils.equals(this.awsAccessKeyId, connectionConfiguration.awsAccessKeyId) &&
                    StringUtils.equals(this.awsSecretAccessKey, connectionConfiguration.getAwsSecretAccessKey());
        }
        return false;
    }
}
