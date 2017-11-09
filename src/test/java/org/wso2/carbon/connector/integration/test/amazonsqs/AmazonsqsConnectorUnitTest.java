/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.integration.test.amazonsqs;

import mockit.Mock;
import junit.framework.Assert;
import mockit.MockUp;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.connector.amazonsqs.auth.AmazonSQSContext;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmazonsqsConnectorUnitTest {
    private org.wso2.carbon.connector.amazonsqs.auth.AmazonSQSAuthConnector amazonSQSAuthConnector;
    private AmazonSQSContext amazonSQSContext;
    private org.wso2.carbon.connector.amazonsqs.constants.AmazonSQSConstants amazonSQSConstants;
    private AmazonSQSAuthConnector amazonSQSAuthConnectorTest;
    private Map<String, String> apiRequestHeadersMap = new HashMap<String, String>();
    private Properties connectorProperties;
    private MessageContext ctx;
    ConfigurationContext configContext;
    SynapseConfiguration synapseConfig;

    @BeforeMethod
    public void setUp() throws Exception {
        amazonSQSAuthConnectorTest = new AmazonSQSAuthConnector();
        amazonSQSAuthConnector = new org.wso2.carbon.connector.amazonsqs.auth.AmazonSQSAuthConnector();
        amazonSQSContext = new org.wso2.carbon.connector.amazonsqs.auth.AmazonSQSContext();
        ctx = createMessageContext();
        //Mock Date object to return fixed date.
        new MockUp<System>() {
            @Mock
            public long currentTimeMillis() {
                Date fake = new Date(1220227200L * 1000);
                return fake.getTime();
            }
        };
    }

    /**
     * Test case to check generated authentication signature.
     *
     * @throws Exception
     */
    @Test
    public void testAmazonSQSAuthConnector() throws Exception {
        connectorProperties = getConnectorConfigProperties();
        generateSignatureRequestObject();
        amazonSQSAuthConnector.connect(ctx);
        Assert.assertEquals(apiRequestHeadersMap.get("Authorization"), ctx.getProperty(amazonSQSConstants.AUTHORIZATION_HEADER));
    }

    /**
     * Test case to remove unneeded properties from  messageContext.
     */
    @Test
    public void testAmazonSQSContext() {
        ctx.setProperty("uri.var.name", "Test");
        amazonSQSContext.connect(ctx);
        Assert.assertNull(ctx.getProperty("uri.var.name"));
    }


    /**
     * Test case to remove that is not exist in messageContext.
     */
    @Test(expectedExceptions = Exception.class)
    public void testAmazonSQSContextNegative() {
        ctx.setProperty(null, "Test");
        amazonSQSContext.connect(ctx);
    }

    /**
     * Create Axis2 Message Context.
     *
     * @return msgCtx created message context.
     * @throws AxisFault
     */
    private MessageContext createMessageContext() throws AxisFault {
        MessageContext msgCtx = createSynapseMessageContext();
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());

        return msgCtx;
    }

    /**
     * Create Synapse Context.
     *
     * @return mc created message context.
     * @throws AxisFault
     */
    private MessageContext createSynapseMessageContext() throws AxisFault {
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        axis2MC.setConfigurationContext(this.configContext);
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MC.setServiceContext(svcCtx);
        axis2MC.setOperationContext(opCtx);
        Axis2MessageContext mc = new Axis2MessageContext(axis2MC, this.synapseConfig, null);
        mc.setMessageID(UIDGenerator.generateURNString());
        mc.setEnvelope(OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope());
        mc.getEnvelope().addChild(OMAbstractFactory.getSOAP12Factory().createSOAPBody());

        return mc;
    }

    /**
     * Return connector configuration properties.
     *
     * @return property list
     * @throws IOException
     */
    private Properties getConnectorConfigProperties() throws IOException {
        String connectorConfigFile;
        connectorConfigFile = Paths.get(System.getProperty("framework.resource.location"), "artifacts", "ESB",
                "connector", "config", "amazonsqs.properties").toString();
        File ignored = new File(connectorConfigFile);
        FileInputStream inputStream = null;
        if (ignored.exists()) {
            inputStream = new FileInputStream(ignored);
        }
        if (inputStream != null) {
            Properties prop = new Properties();
            prop.load(inputStream);
            inputStream.close();
            return prop;
        }

        return null;
    }

    /**
     * Generate signature request object.
     *
     * @throws Exception
     */
    private void generateSignatureRequestObject() throws Exception {
        String requestData;
        Map<String, String> responseMap;
        String signatureRequestFilePath = Paths.get(System.getProperty("framework.resource.location"),
                "artifacts", "ESB", "config", "restRequests", "amazonsqs", "unitTestParameters.json").toString();
        requestData = loadRequestFromFile(signatureRequestFilePath);
        JSONObject signatureRequestObject = new JSONObject(requestData);
        responseMap = amazonSQSAuthConnectorTest.getRequestPayload(signatureRequestObject);
        apiRequestHeadersMap.put("Authorization", responseMap.get(AmazonSQSConstants.AUTHORIZATION_HEADER));
        apiRequestHeadersMap.put("x-amz-date", responseMap.get(AmazonSQSConstants.AMZ_DATE));
        connectorProperties.put("xFormUrl", responseMap.get(AmazonSQSConstants.REQUEST_PAYLOAD));
    }

    /**
     * Load request from file and set those properties into message context.
     *
     * @param requestFileName file name to be process.
     * @return request data.
     * @throws Exception
     */
    private String loadRequestFromFile(String requestFileName) throws Exception {
        String requestFilePath;
        String requestData;
        requestFilePath = requestFileName;
        requestData = getFileContent(requestFilePath);
        Properties prop = (Properties) connectorProperties.clone();

        Matcher matcher = Pattern.compile("%s\\(([A-Za-z0-9]*)\\)", Pattern.DOTALL).matcher(requestData);
        while (matcher.find()) {
            String key = matcher.group(1);
            requestData = requestData.replaceAll("%s\\(" + key + "\\)", prop.getProperty(key));
            ctx.setProperty("uri.var." + key, prop.getProperty(key));
        }
        ctx.setProperty(amazonSQSConstants.SERVICE, "sqs");
        ctx.setProperty(amazonSQSConstants.SIGNATURE_METHOD, amazonSQSConstants.HAMC_SHA_256);
        ctx.setProperty(amazonSQSConstants.SIGNATURE_VERSION, "4");
        ctx.setProperty(amazonSQSConstants.HTTP_METHOD, amazonSQSConstants.POST);
        ctx.setProperty(amazonSQSConstants.TERMINATION_STRING, "aws4_request");
        ctx.setProperty(amazonSQSConstants.CONTENT_TYPE, "application/x-www-form-urlencoded");
        ctx.setProperty(amazonSQSConstants.HOST, "sqs.us-east-1.amazonaws.com");

        return requestData;
    }

    /**
     * Get file content.
     *
     * @param path file path.
     * @return fileContent.
     * @throws IOException
     */
    private String getFileContent(String path) throws IOException {
        String fileContent;
        BufferedInputStream bfist = new BufferedInputStream(new FileInputStream(path));

        byte[] buf = new byte[bfist.available()];
        bfist.read(buf);
        fileContent = new String(buf);

        bfist.close();
        return fileContent;
    }
}