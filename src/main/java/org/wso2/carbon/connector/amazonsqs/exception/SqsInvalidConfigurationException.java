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
package org.wso2.carbon.connector.amazonsqs.exception;

import org.wso2.integration.connector.core.ConnectException;

/**
 *  This class is a custom exception used to indicate issues related to the operations of Amazon SQS.
 */
public class SqsInvalidConfigurationException extends ConnectException {

    public SqsInvalidConfigurationException(String message, Throwable cause) {
        super(cause, message);
    }

    public SqsInvalidConfigurationException(String message) {
        super(message);
    }
}
