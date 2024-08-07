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

    public static final String GENERAL_ERROR_MSG = "AWS SQS connector encountered an error: ";
    public static final String NUMBER_FORMAT_ERROR_MSG = "Error occurred while converting string to integer : ";

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
    public static final String STRING_LIST_VALUES = "StringListValues";
    public static final String ATTRIBUTE_ENTRIES = "attributeEntries";
    public static final String CONNECTION_MAX_IDLE_TIME = "connectionMaxIdleTime";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String CONNECTION_TIME_TO_LIVE = "connectionTimeToLive";
    public static final String CONNECTION_ACQUISITION_TIMEOUT = "connectionAcquisitionTimeout";
    public static final String SOCKET_TIMEOUT = "socketTimeout";
    public static final String API_CALL_TIMEOUT = "apiCallTimeout";
    public static final String API_CALL_ATTEMPT_TIMEOUT = "apiCallAttemptTimeout";

    /**
     * Constant for client binary list values.
     */
    public static final String BINARY_LIST_VALUES = "BinaryListValues";

    public static final String MESSAGE_ATTRIBUTE = "MessageAttribute";
    public static final String BINARY = "Binary";
    public static final String STRING = "String";
    public static final String NAME = "Name";
    public static final String VALUE = "Value";
    public static final String ATTRIBUTE = "Attribute";
    public static final String MESSAGE = "Message";
    public static final String MESSAGE_ID = "MessageId";
    public static final String MD5_OF_BODY = "MD5OfBody";
    public static final String BODY = "Body";
    public static final String RECEIPT_HANDLE = "ReceiptHandle";
    public static final String RECEIPT_HANDLE_CONFIG = "receiptHandle";
    public static final String MD5_OF_MESSAGE_ATTRIBUTES = "MD5OfMessageAttributes";
    public static final String ATTRIBUTES = "attributes";
    public static final String MESSAGE_REQUEST_ENTRIES = "messageRequestEntries";
    public static final String DELAY_SECONDS = "delaySeconds";
    public static final String ID = "id";
    public static final String ID_KEY = "Id";
    public static final String MESSAGE_BODY = "messageBody";
    public static final String MESSAGE_GROUP_ID = "messageGroupId";
    public static final String MESSAGE_DEDUPLICATION_ID = "messageDeduplicationId";
    public static final String VISIBILITY_TIME_OUT = "visibilityTimeout";
    public static final String WAIT_TIME_SECONDS = "waitTimeSeconds";
    public static final String MAX_NUMBER_OF_MESSAGES = "maxNumberOfMessages";
    public static final String SEQUENCE_NUMBER = "SequenceNumber";

    /**
     * Constant for queue name.
     */
    public static final String QUEUE_NAME = "queueName";
    public static final String ACCOUNT_ID = "accountId";

    /**
     * Constant for queue id.
     */
    public static final String QUEUE_ID = "queueId";

    /**
     * Constant for queue id.
     */
    public static final String QUEUE_URL = "queueUrl";
    public static final String QUEUE_URL_KEY = "QueueUrl";
    public static final String TAGS = "tags";
    public static final String QUEUE_NAME_PREFIX = "queueNamePrefix";
    public static final String NEXT_TOKEN = "nextToken";
    public static final String MAX_RESULT = "maxResults";

    /**
     * Constant for slash.
     */
    public static final String SLASH = "/";

    /**
     * Constant for https.
     */
    public static final String HTTPS = "https";

    public static final String PROPERTY_ERROR_DETAIL = "ERROR_DETAIL";
    public static final String MESSAGE_SYSTEM_ATTRIBUTES = "messageSystemAttributes";
    public static final String MESSAGE_SYSTEM_ATTRIBUTE_NAMES = "messageSystemAttributeNames";
    public static final String MESSAGE_ATTRIBUTE_NAMES = "messageAttributeNames";
    public static final String MESSAGE_ATTRIBUTES = "messageAttributes";
    public static final String LABEL = "label";
    public static final String ACTION_NAMES = "actionNames";
    public static final String AWS_ACCOUNT_NUMBERS = "awsAccountNumbers";
    public static final String MD5_OF_MESSAGE_BODY = "MD5OfMessageBody";
    public static final String MD5_OF_MESSAGE_SYSTEM_ATTRIBUTES = "MD5OfMessageSystemAttributes";

}
