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
    private Integer connectionAcquisitionTimeout = -1;
    private Integer socketTimeout = -1;
    private Integer connectionTimeToLive = -1;
    private Integer connectionTimeout = -1;
    private Integer connectionMaxIdleTime = -1;

    private Integer apiCallAttemptTimeout = -1;
    private Integer apiCallTimeout = -1;

    public String getRegion() {
        return region;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) throws SqsInvalidConfigurationException {
        if (StringUtils.isNotBlank(connectionName)) {
            this.connectionName = connectionName;
        } else {
            throw new SqsInvalidConfigurationException("Mandatory parameter 'connectionName' is not set.");
        }
    }

    public void setRegion(String region) throws SqsInvalidConfigurationException {
        if (StringUtils.isNotBlank(region)) {
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
        return this.awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public Integer getConnectionAcquisitionTimeout() {
        return this.connectionAcquisitionTimeout;
    }

    public void setConnectionAcquisitionTimeout(Integer connectionAcquisitionTimeout) {
        this.connectionAcquisitionTimeout = connectionAcquisitionTimeout;
    }

    public Integer getSocketTimeout() {
        return this.socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Integer getConnectionTimeToLive() {
        return this.connectionTimeToLive;
    }

    public void setConnectionTimeToLive(Integer connectionTimeToLive) {
        this.connectionTimeToLive = connectionTimeToLive;
    }

    public Integer getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getConnectionMaxIdleTime() {
        return this.connectionMaxIdleTime;
    }

    public void setConnectionMaxIdleTime(Integer connectionMaxIdleTime) {
        this.connectionMaxIdleTime = connectionMaxIdleTime;
    }

    public Integer getApiCallTimeout() {
        return this.apiCallTimeout;
    }

    public void setApiCallTimeout(Integer apiCallTimeout) {
        this.apiCallTimeout = apiCallTimeout;
    }

    public Integer getApiCallAttemptTimeout() {
        return this.apiCallAttemptTimeout;
    }

    public void setApiCallAttemptTimeout(Integer apiCallAttemptTimeout) {
        this.apiCallAttemptTimeout = apiCallAttemptTimeout;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ConnectionConfiguration) {
            ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration) obj;
            return StringUtils.equals(this.connectionName, connectionConfiguration.getConnectionName()) &&
                    StringUtils.equals(this.region, connectionConfiguration.getRegion()) &&
                    StringUtils.equals(this.awsAccessKeyId, connectionConfiguration.getAwsAccessKeyId()) &&
                    StringUtils.equals(this.awsSecretAccessKey, connectionConfiguration.getAwsSecretAccessKey()) &&
                    StringUtils.equals(this.connectionTimeout.toString(), connectionConfiguration.
                            getConnectionTimeout().toString()) &&
                    StringUtils.equals(this.socketTimeout.toString(), connectionConfiguration.getSocketTimeout().
                            toString()) &&
                    StringUtils.equals(this.connectionAcquisitionTimeout.toString(), connectionConfiguration.
                            getConnectionAcquisitionTimeout().toString()) &&
                    StringUtils.equals(this.connectionTimeToLive.toString(),
                            connectionConfiguration.getConnectionTimeToLive().toString()) &&
                    StringUtils.equals(this.apiCallTimeout.toString(), connectionConfiguration.
                            getApiCallTimeout().toString()) &&
                    StringUtils.equals(this.apiCallAttemptTimeout.toString(),
                            connectionConfiguration.getApiCallAttemptTimeout().toString()) &&
                    StringUtils.equals(this.connectionMaxIdleTime.toString(),
                            connectionConfiguration.getConnectionMaxIdleTime().toString());
        }
        return false;
    }
}
