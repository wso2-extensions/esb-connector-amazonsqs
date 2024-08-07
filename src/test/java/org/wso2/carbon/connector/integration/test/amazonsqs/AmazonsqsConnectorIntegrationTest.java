/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.integration.test.amazonsqs;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AmazonsqsConnectorIntegrationTest extends ConnectorIntegrationTestBase {

    private Map< String, String > esbRequestHeadersMap = new HashMap< String, String >();

    private Map< String, String > apiRequestHeadersMap = new HashMap< String, String >();
    private String receiptHandle = "";

    /**
     * Set up the environment.
     */
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        addCertificatesToEIKeyStore("client-truststore.jks", "wso2carbon");
        String connectorName = System.getProperty("connector_name") + "-connector-" +
                System.getProperty("connector_version") + ".zip";
        init(connectorName);
        getApiConfigProperties();

        esbRequestHeadersMap.put("Accept-Charset", "UTF-8");
        esbRequestHeadersMap.put("Content-Type", "");

        apiRequestHeadersMap.putAll(esbRequestHeadersMap);
        esbRequestHeadersMap.put("Accept-Charset", "UTF-8");
        esbRequestHeadersMap.put("Content-Type", "application/xml");

        apiRequestHeadersMap.put("Accept-Charset", "UTF-8");
        apiRequestHeadersMap.put("Content-Type", "application/x-www-form-urlencoded");
    }

    /**
     * Positive test case for createQueue method with mandatory parameters.
     */
    @Test(priority = 1, description = "AmazonSQS {createQueue} integration test with mandatory parameters.")
    public void testCreateQueuesMandatoryParameters() throws IOException, XMLStreamException {
        String methodName = "createQueue";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "createQueue.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        String queueUrl = body.getFirstElement().getFirstElement().getText();
        String[] values = queueUrl.split("/");
        connectorProperties.setProperty("queueId", values[values.length -2]);
        connectorProperties.setProperty("accountIds", values[values.length -2] + ", 111111111111");
        connectorProperties.setProperty("queueUrl", queueUrl);
        Assert.assertTrue(body.toString().startsWith("<CreateQueueResponse><CreateQueueResult><QueueUrl>"));
    }

    /**
     * Positive test case for createQueue method with mandatory parameters.
     */
    @Test(priority = 2, description = "AmazonSQS {createQueue} integration test with mandatory parameters.")
    public void testCreateDeadLetterQueuesMandatoryParameters() throws IOException, XMLStreamException {
        String methodName = "createDeadLetterQueue";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "createDeadLetterQueue.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        connectorProperties.setProperty("deadLetterQueueUrl", body.getFirstElement().getFirstElement().getText());
        Assert.assertTrue(body.toString().startsWith("<CreateQueueResponse><CreateQueueResult><QueueUrl>"));
    }

    /**
     * Positive test case for setQueueAttributes method with mandatory parameters.
     */
    @Test(priority = 3, description = "AmazonSQS {setQueueAttributes} integration test with mandatory parameters.")
    public void testSetQueueAttributesToDeadLetterQueue() throws IOException, XMLStreamException {
        String methodName = "setQueueAttribute";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "setQueueAttribute.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<SetQueueAttributesResponse><ResponseMetadata><RequestId>"));
    }

    /**
     * Positive test case for listDeadLetterSourceQueues method with mandatory parameters.
     */
    @Test(priority = 4, description = "AmazonSQS {listDeadLetterSourceQueues} integration test with mandatory parameters.")
    public void testListDeadLetterSourceQueues() throws IOException, XMLStreamException {
        String methodName = "listDeadLetterSourceQueues";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "listDeadLetterSourceQueues.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<ListDeadLetterSourceQueuesResponse><ListDeadLetterSourceQueuesResult><QueueUrl>"));
    }

    /**
     * Positive test case for deleteQueue method with mandatory parameters.
     */
    @Test(priority = 5, description = "AmazonSQS {deleteQueue} integration test with mandatory parameters.")
    public void testDeleteDeadLetterQueues() throws IOException, XMLStreamException {
        String methodName = "deleteQueue";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "deleteQueue.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<DeleteQueueResponse><ResponseMetadata><RequestId>"));
    }

    /**
     * Positive test case for getQueueUrl method with mandatory parameters.
     */
    @Test(priority = 6, description = "AmazonSQS {getQueueUrl} integration test with mandatory parameters.")
    public void testGetQueueUrl() throws IOException, XMLStreamException {
        String methodName = "getQueueUrl";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "getQueueUrl.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.getFirstElement().getFirstElement().getText().endsWith("TestQueue"));
        Assert.assertTrue(body.toString().startsWith("<GetQueueUrlResponse><GetQueueUrlResult><QueueUrl>"));
    }

    /**
     * Positive test case for getQueueAttribute method with mandatory parameters.
     */
    @Test(priority = 6, description = "AmazonSQS {getQueueAttribute} integration test with mandatory parameters.")
    public void testGetQueueAttribute() throws IOException, XMLStreamException {
        String methodName = "getQueueAttributes";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "getQueueAttributes.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<GetQueueAttributesResponse><GetQueueAttributesResult>" +
                "<Attribute><Name>"));
    }

    /**
     * Positive test case for listQueues method with mandatory parameters.
     */
    @Test(priority = 6, description = "AmazonSQS {listQueues} integration test with mandatory parameters.")
    public void testListQueues() throws IOException, XMLStreamException {
        String methodName = "listQueues";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, null);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<ListQueuesResponse><ListQueuesResult><QueueUrl>"));
    }


    /**
     * Positive test case for addPermission method with mandatory parameters.
     */
    @Test(priority = 7, description = "AmazonSQS {addPermission} integration test with mandatory parameters.")
    public void testAddPermission() throws IOException, XMLStreamException {
        String methodName = "addPermission";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "addPermission.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<AddPermissionResponse><ResponseMetadata><RequestId>"));
    }

    /**
     * Positive test case for removePermission method with mandatory parameters.
     */
    @Test(priority = 8, description = "AmazonSQS {removePermission} integration test with mandatory parameters.")
    public void testRemovePermission() throws IOException, XMLStreamException {
        String methodName = "removePermission";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "removePermission.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<RemovePermissionResponse><ResponseMetadata><RequestId>"));
    }

    /**
     * Positive test case for sendMessage method with mandatory parameters.
     */
    @Test(priority = 9, description = "AmazonSQS {sendMessage} integration test with mandatory parameters.")
    public void testSendMessage() throws IOException, XMLStreamException {
        String methodName = "sendMessage";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "sendMessage.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<SendMessageResponse><SendMessageResult><MessageId>"));
    }

    /**
     * Positive test case for receiveMessage method with mandatory parameters.
     */
    @Test(priority = 10, description = "AmazonSQS {receiveMessage} integration test with mandatory parameters.")
    public void testReceiveMessage() throws IOException, XMLStreamException {
        String methodName = "receiveMessage";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "receiveMessage.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Iterator values = body.getFirstElement().getFirstElement().getChildElements();
        while (values.hasNext()) {
            OMElement omElement = (OMElement) values.next();
            if (omElement.getQName().toString().equals("ReceiptHandle")) {
                receiptHandle = omElement.getText();
                connectorProperties.setProperty("receiptHandle", omElement.getText());
            }
        }
        Assert.assertTrue(body.toString().startsWith("<ReceiveMessageResponse><ReceiveMessageResult>" +
                "<Message><MessageId>"));
    }

    /**
     * Positive test case for changeMessageVisibility method with mandatory parameters.
     */
    @Test(priority = 11, description = "AmazonSQS {changeMessageVisibility} integration test with mandatory parameters.")
    public void testChangeMessageVisibility() throws IOException, XMLStreamException {
        String methodName = "changeMessageVisibility";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "changeMessageVisibility.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<ChangeMessageVisibilityResponse><ResponseMetadata><RequestId>"));
    }

    /**
     * Positive test case for deleteQueue method with mandatory parameters.
     */
    @Test(priority = 12, description = "AmazonSQS {deleteMessage} integration test with mandatory parameters.")
    public void testDeleteMessage() throws IOException, XMLStreamException {
        String methodName = "deleteMessage";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "deleteMessage.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<DeleteMessageResponse><ResponseMetadata><RequestId>"));
    }

    /**
     * Positive test case for sendMessageBatch method with mandatory parameters.
     */
    @Test(priority = 13, description = "AmazonSQS {sendMessageBatch} integration test with mandatory parameters.")
    public void testSendMessageBatch() throws IOException, XMLStreamException {
        String methodName = "sendMessageBatch";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "sendMessageBatch.xml");
        OMElement body = esbRestResponse.getBody();
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertTrue(body.toString().startsWith("<SendMessageBatchResponse><SendMessageBatchResult>" +
                "<SendMessageBatchResultEntry><Id>"));
    }

    /**
     * Positive test case for receiveMessage method with mandatory parameters.
     */
    @Test(priority = 14, description = "This test used to get the receipt handle of batch message.")
    public void testReceiveMessage2() throws IOException, XMLStreamException {
        String methodName = "receiveMessage";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "receiveMessage.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Iterator values = body.getFirstElement().getFirstElement().getChildElements();
        while (values.hasNext()) {
            OMElement omElement = (OMElement) values.next();
            if (omElement.getQName().toString().equals("ReceiptHandle")) {
                receiptHandle = omElement.getText();
                connectorProperties.setProperty("receiptHandle", omElement.getText());
            }
        }
        Assert.assertTrue(body.toString().startsWith("<ReceiveMessageResponse><ReceiveMessageResult>" +
                "<Message><MessageId>"));
    }

    /**
     * Positive test case for changeMessageVisibilityBatch method with mandatory parameters.
     */
    @Test(priority = 15, description = "AmazonSQS {changeMessageVisibilityBatch} integration test with mandatory parameters.")
    public void testChangeMessageVisibilityBatch() throws IOException, XMLStreamException {
        String methodName = "changeMessageVisibilityBatch";
        connectorProperties.setProperty("changeMessageVisibilityRequestEntries", "[{\"receiptHandle\": \"" +
                receiptHandle + "\", " + "\"id\": \"id1\", \"VisibilityTimeout\": 5 }]");
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "changeMessageVisibilityBatch.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
    }

    /**
     * Positive test case for deleteMessageBatch method with mandatory parameters.
     */
    @Test(priority = 16, description = "AmazonSQS {deleteMessageBatch} integration test with mandatory parameters.")
    public void testDeleteMessageBatch() throws IOException, XMLStreamException {
        String methodName = "deleteMessageBatch";
        connectorProperties.setProperty("deleteMessageRequestEntries", "[{\"receiptHandle\": \"" + receiptHandle +
                "\", " + "\"id\": \"id1\" }]");
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "deleteMessageBatch.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
    }

    /**
     * Positive test case for purgeQueue method with mandatory parameters.
     */
    @Test(priority = 17, description = "AmazonSQS {purgeQueues} integration test with mandatory parameters.")
    public void testPurgeQueues() throws IOException, XMLStreamException {
        String methodName = "purgeQueue";
        RestResponse<OMElement> esbRestResponse = sendXmlRestRequest(getProxyServiceURLHttp(methodName),
                "POST", esbRequestHeadersMap, "purgeQueue.xml");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        OMElement body = esbRestResponse.getBody();
        Assert.assertTrue(body.toString().startsWith("<PurgeQueueResponse><ResponseMetadata><RequestId>"));
    }
}
