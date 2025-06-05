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
import org.wso2.integration.connector.core.connection.Connection;
import org.wso2.integration.connector.core.connection.ConnectionConfig;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.time.Duration;

/**
 * This class manages connections to Amazon SQS using the AWS SDK.
 */
public class SqsConnection implements Connection {

    private ConnectionConfiguration connectionConfig;
    private SqsClient sqsClient;

    public SqsConnection(ConnectionConfiguration connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public ConnectionConfiguration getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfiguration connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public void setSqsClient(ConnectionConfiguration connectionConfig) {
        this.sqsClient = createNewClientInstance(connectionConfig);
    }

    public SqsClient getSqsClient() {
        if (sqsClient == null) {
            this.sqsClient = createNewClientInstance(this.connectionConfig);
        }
        return this.sqsClient;
    }

    private SqsClient createNewClientInstance(ConnectionConfiguration connectionConfig) {
        String awsAccessKeyId = connectionConfig.getAwsAccessKeyId();
        String awsSecretAccessKey = connectionConfig.getAwsSecretAccessKey();
        Integer connectionTimeout = connectionConfig.getConnectionTimeout();
        Integer connectionAcquisitionTimeout = connectionConfig.getConnectionAcquisitionTimeout();
        Integer socketTimeout = connectionConfig.getSocketTimeout();
        Integer connectionTimeToLive = connectionConfig.getConnectionTimeToLive();
        Integer connectionMaxIdleTime = connectionConfig.getConnectionMaxIdleTime();
        Integer apiCallTimeout = connectionConfig.getApiCallTimeout();
        Integer apiCallAttemptTimeout = connectionConfig.getApiCallAttemptTimeout();
        ApacheHttpClient.Builder apacheHttpClientBuilder = ApacheHttpClient.builder();
        if (connectionTimeout != -1) {
            apacheHttpClientBuilder.connectionTimeout(Duration.ofMillis(connectionTimeout));
        }
        if (connectionAcquisitionTimeout != -1) {
            apacheHttpClientBuilder.connectionAcquisitionTimeout(Duration.ofMillis(connectionAcquisitionTimeout));
        }
        if (socketTimeout != -1) {
            apacheHttpClientBuilder.socketTimeout(Duration.ofMillis(socketTimeout));
        }
        if (connectionTimeToLive != -1) {
            apacheHttpClientBuilder.connectionTimeToLive(Duration.ofMillis(connectionTimeToLive));
        }
        if (connectionMaxIdleTime != -1) {
            apacheHttpClientBuilder.connectionMaxIdleTime(Duration.ofMillis(connectionMaxIdleTime));
        }
        SqsClientBuilder sqsClientBuilder = SqsClient.builder().region(Region.of(connectionConfig.getRegion())).
                httpClient(apacheHttpClientBuilder.build());
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        if (StringUtils.isNotBlank(awsAccessKeyId) && StringUtils.isNotBlank(awsSecretAccessKey)) {
            credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.
                    create(awsAccessKeyId, awsSecretAccessKey));
        }
        if (apiCallTimeout != -1) {
            sqsClientBuilder.overrideConfiguration(b -> b.apiCallTimeout(Duration.ofMillis(apiCallTimeout)));
        }
        if (apiCallAttemptTimeout != -1) {
            sqsClientBuilder.overrideConfiguration(b -> b.apiCallAttemptTimeout(
                    Duration.ofMillis(apiCallAttemptTimeout)));
        }
        return sqsClientBuilder.credentialsProvider(credentialsProvider).build();
    }

    @Override
    public void connect(ConnectionConfig config) {

        throw new UnsupportedOperationException("Nothing to do when connecting.");
    }

    @Override
    public void close() {
        if (sqsClient != null) {
            this.sqsClient.close();
        }
    }
}
