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
package org.wso2.carbon.connector.amazonsqs.object;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.connector.amazonsqs.connection.ConnectionConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

/**
 * Singleton class to manage the AWS SQS client instance.
 */
public class SqsClientManager {

    private static volatile SqsClientManager instance;
    private final SqsClient sqsClient;

    private SqsClientManager(ConnectionConfiguration connectionConfig) {
        String awsAccessKeyId = connectionConfig.getAwsAccessKeyId();
        String awsSecretAccessKey = connectionConfig.getAwsSecretAccessKey();
        SqsClientBuilder sqsClientBuilder = SqsClient.builder().region(Region.of(connectionConfig.getRegion())).
                httpClient(ApacheHttpClient.builder().build());
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        if (StringUtils.isNotEmpty(awsAccessKeyId) && StringUtils.isNotEmpty(awsSecretAccessKey)) {
            credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.
                    create(awsAccessKeyId, awsSecretAccessKey));
        }
        this.sqsClient = sqsClientBuilder.credentialsProvider(credentialsProvider).build();
    }

    public static synchronized SqsClientManager getInstance(ConnectionConfiguration connectionConfig) {
        if (instance == null) {
            instance = new SqsClientManager(connectionConfig);
        }
        return instance;
    }

    public SqsClient getSqsClient() {
        return sqsClient;
    }

    public static SqsClientManager createNewInstance(ConnectionConfiguration connectionConfig) {
        return new SqsClientManager(connectionConfig);
    }
}
