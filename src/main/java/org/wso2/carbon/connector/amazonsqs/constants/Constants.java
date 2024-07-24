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
package org.wso2.carbon.connector.amazonsqs.constants;

/**
 * AmazonSQS contains required constants.
 */
public class Constants {

    /**
     * Constant for connector name.
     */
    public static final String CONNECTOR_NAME = "amazonsqs";

    /**
     * Constant for connection name.
     */
    public static final String CONNECTION_NAME = "name";

    /**
     * Constant for error code.
     */
    public static final String PROPERTY_ERROR_CODE = "ERROR_CODE";

    /**
     * Constant for error  message.
     */
    public static final String PROPERTY_ERROR_MESSAGE = "ERROR_MESSAGE";

    /**
     * Constant for status code.
     */
    public static final String STATUS_CODE = "HTTP_SC";

    /**
     * Constant for Region.
     */
    public static final String REGION = "region";

    /**
     * Constant for AWS_ACCESS_KEY_ID.
     */
    public static final String AWS_ACCESS_KEY_ID = "accessKeyId";

    /**
     * Constant for AWS_SECRET_ACCESS_KEY.
     */
    public static final String AWS_SECRET_ACCESS_KEY = "secretAccessKey";

    /**
     * Constant for client exception msg.
     */
    public static final String CLIENT_EXCEPTION_MSG = "Error occurred while generating the client: ";

    /**
     * Constant for runtime exception msg.
     */
    public static final String RUN_TIME_EXCEPTION_MSG = "Error occurred while performing the operation: ";

    public static final String GENERAL_ERROR_MSG = "Aws SQS connector encountered an error: ";

    /**
     * Constant for data type.
     */
    public static final String DATA_TYPE = "DataType";

    /**
     * Constant for client binary value.
     */
    public static final String BINARY_VALUE = "BinaryValue";

    /**
     * Constant for client string msg.
     */
    public static final String STRING_VALUE = "StringValue";

    /**
     * Constant for client string list values.
     */
    public static final String STRING_LIST_VALUE = "StringListValues";

    /**
     * Constant for client binary list values.
     */
    public static final String BINARY_LIST_VALUE = "BinaryListValues";

    /**
     * Constant for queue name.
     */
    public static final String QUEUE_NAME = "queueName";

    /**
     * Constant for queue id.
     */
    public static final String QUEUE_ID = "queueId";

    /**
     * Constant for queue id.
     */
    public static final String QUEUE_URL = "queueUrl";

    /**
     * Constant for slash.
     */
    public static final String SLASH = "/";

    /**
     * Constant for https.
     */
    public static final String HTTPS = "https";
}
