/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class AmazonSQSAuthConnector which helps to generate authentication signature for Amazon SQS WSO2
 * ESB Connector.
 */

public class AmazonSQSAuthConnector {

    /**
     * Connect method which is generating authentication of the connector for each request.
     *
     * @param signatureRequestObject ESB messageContext.
     * @throws java.io.UnsupportedEncodingException
     * @throws IllegalStateException 
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.InvalidKeyException
     * @throws JSONException 
     */
    public final Map<String, String> getRequestPayload(final JSONObject signatureRequestObject) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException, JSONException {

        final StringBuilder canonicalRequest = new StringBuilder();
        final StringBuilder stringToSign = new StringBuilder();
        final StringBuilder payloadBuilder = new StringBuilder();
        final StringBuilder payloadStrBuilder = new StringBuilder();
        final StringBuilder authHeader = new StringBuilder();
        init(signatureRequestObject);

        
        // Generate time-stamp which will be sent to API and to be used in Signature
        final Date date = new Date();
        final TimeZone timeZone = TimeZone.getTimeZone(AmazonSQSConstants.GMT);
        final DateFormat dateFormat = new SimpleDateFormat(AmazonSQSConstants.ISO8601_BASIC_DATE_FORMAT);
        dateFormat.setTimeZone(timeZone);
        final String amzDate = dateFormat.format(date);

        final DateFormat shortDateFormat = new SimpleDateFormat(AmazonSQSConstants.SHORT_DATE_FORMAT);
        shortDateFormat.setTimeZone(timeZone);
        final String shortDate = shortDateFormat.format(date);

        signatureRequestObject.put(AmazonSQSConstants.AMZ_DATE, amzDate);
        final Map<String, String> parameterNamesMap = getParameterNamesMap();
        final Map<String, String> parametersMap = getSortedParametersMap(signatureRequestObject, parameterNamesMap);
            canonicalRequest.append(signatureRequestObject.get(AmazonSQSConstants.HTTP_METHOD));
            canonicalRequest.append(AmazonSQSConstants.NEW_LINE);
            final String charSet = Charset.defaultCharset().toString();
            if (signatureRequestObject.has(AmazonSQSConstants.URL_QUEUE_NAME)
                    && !("").equals(signatureRequestObject.get(AmazonSQSConstants.URL_QUEUE_NAME))
                    && signatureRequestObject.has(AmazonSQSConstants.QUEUE_ID)
                    && !("").equals(signatureRequestObject.get(AmazonSQSConstants.QUEUE_ID))) {
                // queue ID and queue name should be encoded twise to match the Signature being generated by API,
                // Note that API it looks encodes the incoming URL once before creating the signature, SInce
                // we send url encoded URLs, API signatures are twise encoded
                final String encodedQueueID =
                        URLEncoder.encode(signatureRequestObject.get(AmazonSQSConstants.QUEUE_ID).toString(), charSet);
                final String encodedQueueName =
                        URLEncoder.encode(signatureRequestObject.get(AmazonSQSConstants.URL_QUEUE_NAME).toString(),
                                charSet);
                canonicalRequest
                        .append((AmazonSQSConstants.FORWARD_SLASH + URLEncoder.encode(encodedQueueID, charSet)
                                + AmazonSQSConstants.FORWARD_SLASH + URLEncoder.encode(encodedQueueName, charSet) + AmazonSQSConstants.FORWARD_SLASH)
                                .replaceAll(AmazonSQSConstants.REGEX_ASTERISK, AmazonSQSConstants.URL_ENCODED_ASTERISK));
                // Sets the http request Uri to message context 
                signatureRequestObject.put(AmazonSQSConstants.HTTP_REQUEST_URI, AmazonSQSConstants.FORWARD_SLASH
                        + encodedQueueID + AmazonSQSConstants.FORWARD_SLASH + encodedQueueName
                        + AmazonSQSConstants.FORWARD_SLASH);
            } else {
                canonicalRequest.append(AmazonSQSConstants.FORWARD_SLASH);
            }

            canonicalRequest.append(AmazonSQSConstants.NEW_LINE);

            final Set<String> keySet = parametersMap.keySet();
            for (String key : keySet) {
                payloadBuilder.append(URLEncoder.encode(key, charSet));
                payloadBuilder.append(AmazonSQSConstants.EQUAL);
                payloadBuilder.append(URLEncoder.encode(parametersMap.get(key), charSet));
                payloadBuilder.append(AmazonSQSConstants.AMPERSAND);
                payloadStrBuilder.append(AmazonSQSConstants.QUOTE);
                payloadStrBuilder.append(key);
                payloadStrBuilder.append(AmazonSQSConstants.QUOTE);
                payloadStrBuilder.append(AmazonSQSConstants.COLON);
                payloadStrBuilder.append(AmazonSQSConstants.QUOTE);
                payloadStrBuilder.append(parametersMap.get(key));
                payloadStrBuilder.append(AmazonSQSConstants.QUOTE);
                payloadStrBuilder.append(AmazonSQSConstants.COMMA);

            }
            // Adds authorization header to message context, removes additionally appended comma at the end
            if (payloadStrBuilder.length() > 0) {
                signatureRequestObject.put(AmazonSQSConstants.REQUEST_PAYLOAD,
                        payloadStrBuilder.substring(0, payloadStrBuilder.length() - 1));
            }
            // Appends empty string since no URL parameters are used in POST API requests
            canonicalRequest.append("");
            canonicalRequest.append(AmazonSQSConstants.NEW_LINE);
            final Map<String, String> headersMap = getSortedHeadersMap(signatureRequestObject, parameterNamesMap);
            final StringBuilder canonicalHeaders = new StringBuilder();
            final StringBuilder signedHeader = new StringBuilder();
            final Set<String> keysSet = headersMap.keySet();
            for (String key : keysSet) {
                canonicalHeaders.append(key);
                canonicalHeaders.append(AmazonSQSConstants.COLON);
                canonicalHeaders.append(headersMap.get(key));
                canonicalHeaders.append(AmazonSQSConstants.NEW_LINE);
                signedHeader.append(key);
                signedHeader.append(AmazonSQSConstants.SEMI_COLON);
            }
            canonicalRequest.append(canonicalHeaders.toString());
            canonicalRequest.append(AmazonSQSConstants.NEW_LINE);
            // Remove unwanted semi-colon at the end of the signedHeader string
            String signedHeaders = "";
            if (signedHeader.length() > 0) {
                signedHeaders = signedHeader.substring(0, signedHeader.length() - 1);
            }
            canonicalRequest.append(signedHeaders);
            canonicalRequest.append(AmazonSQSConstants.NEW_LINE);
            // HashedPayload = HexEncode(Hash(requestPayload))
            String requestPayload = "";
            if (payloadBuilder.length() > 0) {
            /*
             * First removes the additional ampersand appended to the end of the payloadBuilder, then o
             * further modifications to preserve unreserved characters as per the API guide
             * (http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html)
             */
                requestPayload =
                        payloadBuilder.substring(0, payloadBuilder.length() - 1).toString()
                                .replace(AmazonSQSConstants.PLUS, AmazonSQSConstants.URL_ENCODED_PLUS)
                                .replace(AmazonSQSConstants.URL_ENCODED_TILT, AmazonSQSConstants.TILT)
                                .replace(AmazonSQSConstants.ASTERISK, AmazonSQSConstants.URL_ENCODED_ASTERISK);
            }
            canonicalRequest.append(bytesToHex(hash(requestPayload)).toLowerCase());
            stringToSign.append(AmazonSQSConstants.AWS4_HMAC_SHA_256);
            stringToSign.append(AmazonSQSConstants.NEW_LINE);
            stringToSign.append(amzDate);
            stringToSign.append(AmazonSQSConstants.NEW_LINE);
            stringToSign.append(shortDate);
            stringToSign.append(AmazonSQSConstants.FORWARD_SLASH);
            stringToSign.append(signatureRequestObject.get(AmazonSQSConstants.REGION));
            stringToSign.append(AmazonSQSConstants.FORWARD_SLASH);
            stringToSign.append(signatureRequestObject.get(AmazonSQSConstants.SERVICE));
            stringToSign.append(AmazonSQSConstants.FORWARD_SLASH);
            stringToSign.append(signatureRequestObject.get(AmazonSQSConstants.TERMINATION_STRING));
            stringToSign.append(AmazonSQSConstants.NEW_LINE);
            stringToSign.append(bytesToHex(hash(canonicalRequest.toString())).toLowerCase());
            final byte[] signingKey =
                    getSignatureKey(signatureRequestObject, signatureRequestObject.get(AmazonSQSConstants.SECRET_ACCESS_KEY)
                            .toString(), shortDate, signatureRequestObject.get(AmazonSQSConstants.REGION).toString(),
                            signatureRequestObject.get(AmazonSQSConstants.SERVICE).toString());

            // Construction of authorization header value to be in cluded in API request
            authHeader.append(AmazonSQSConstants.AWS4_HMAC_SHA_256);
            authHeader.append(AmazonSQSConstants.COMMA);
            authHeader.append(AmazonSQSConstants.CREDENTIAL);
            authHeader.append(AmazonSQSConstants.EQUAL);
            authHeader.append(signatureRequestObject.get(AmazonSQSConstants.ACCESS_KEY_ID));
            authHeader.append(AmazonSQSConstants.FORWARD_SLASH);
            authHeader.append(shortDate);
            authHeader.append(AmazonSQSConstants.FORWARD_SLASH);
            authHeader.append(signatureRequestObject.get(AmazonSQSConstants.REGION));
            authHeader.append(AmazonSQSConstants.FORWARD_SLASH);
            authHeader.append(signatureRequestObject.get(AmazonSQSConstants.SERVICE));
            authHeader.append(AmazonSQSConstants.FORWARD_SLASH);
            authHeader.append(signatureRequestObject.get(AmazonSQSConstants.TERMINATION_STRING));
            authHeader.append(AmazonSQSConstants.COMMA);
            authHeader.append(AmazonSQSConstants.SIGNED_HEADERS);
            authHeader.append(AmazonSQSConstants.EQUAL);
            authHeader.append(signedHeaders);
            authHeader.append(AmazonSQSConstants.COMMA);
            authHeader.append(AmazonSQSConstants.API_SIGNATURE);
            authHeader.append(AmazonSQSConstants.EQUAL);
            authHeader.append(bytesToHex(hmacSHA256(signingKey, stringToSign.toString())).toLowerCase());
            // Adds authorization header to message context
            signatureRequestObject.put(AmazonSQSConstants.AUTHORIZATION_HEADER, authHeader.toString());
            
            Map <String , String> responseMap = new HashMap<String, String>();
            responseMap.put(AmazonSQSConstants.AUTHORIZATION_HEADER, authHeader.toString());
            responseMap.put(AmazonSQSConstants.AMZ_DATE, amzDate);
            responseMap.put(AmazonSQSConstants.REQUEST_PAYLOAD, requestPayload);
            return responseMap;
    }
    
    /**
     * @param signatureRequestObject
     * @throws JSONException
     */
    private void init(JSONObject signatureRequestObject) throws JSONException {
        signatureRequestObject.put(AmazonSQSConstants.SERVICE, "sqs");
        signatureRequestObject.put(AmazonSQSConstants.SIGNATURE_METHOD, "HmacSHA256");
        signatureRequestObject.put(AmazonSQSConstants.SIGNATURE_VERSION, "4");
        signatureRequestObject.put(AmazonSQSConstants.CONTENT_TYPE, "application/x-www-form-urlencoded");
        signatureRequestObject.put(AmazonSQSConstants.HTTP_METHOD, "POST");
        signatureRequestObject.put(AmazonSQSConstants.TERMINATION_STRING, "aws4_request");
        signatureRequestObject.put(AmazonSQSConstants.HOST, "sqs." + signatureRequestObject.get(AmazonSQSConstants.REGION) + ".amazonaws.com");
    }
    /**
     * getKeys method returns a list of parameter keys.
     *
     * @return list of parameter key value.
     */
    private String[] getParameterKeys() {
        return new String[] {AmazonSQSConstants.ACTION, AmazonSQSConstants.EXPIRES, AmazonSQSConstants.SECURITY_TOKEN,
                AmazonSQSConstants.SIGNATURE, AmazonSQSConstants.SIGNATURE_METHOD,
                AmazonSQSConstants.SIGNATURE_VERSION, AmazonSQSConstants.TIMESTAMP, AmazonSQSConstants.VERSION,
                AmazonSQSConstants.QUEUE_NAME_PREFIX, AmazonSQSConstants.QUEUE_URLS, AmazonSQSConstants.ACCESS_KEY_ID,
                AmazonSQSConstants.PAYLOAD_QUEUE_NAME, AmazonSQSConstants.LABEL, AmazonSQSConstants.MESSAGE_BODY,
                AmazonSQSConstants.LABEL, AmazonSQSConstants.MESSAGE_BODY, AmazonSQSConstants.RECEIPT_HANDLE,
                AmazonSQSConstants.MAX_NO_OF_MESSAGES, AmazonSQSConstants.VISIBILITY_TIMEOUT, AmazonSQSConstants.WAIT_TIME_SECONDS
                , AmazonSQSConstants.DELAY_SECONDS, AmazonSQSConstants.API_ACCOUNT_ID

        };
    }
    /**
     * getKeys method returns a list of header keys.
     *
     * @return list of header key value.
     */
    private String[] getHeaderKeys() {
        return new String[] {AmazonSQSConstants.HOST, AmazonSQSConstants.CONTENT_TYPE, AmazonSQSConstants.AMZ_DATE, };
    }
    /**
     * getCollectionParameterKeys method returns a list of predefined parameter keys which users will be used.
     * to send collection of values in each parameter.
     *
     * @return list of parameter key value.
     */
    private String[] getMultivaluedParameterKeys() {
        return new String[] {AmazonSQSConstants.AWS_ACCOUNT_NUMBERS, AmazonSQSConstants.ACTION_NAMES,
                AmazonSQSConstants.REQUEST_ENTRIES, AmazonSQSConstants.ATTRIBUTE_ENTRIES, AmazonSQSConstants.ATTRIBUTES,
                AmazonSQSConstants.MESSAGE_ATTRIBUTE_NAMES, AmazonSQSConstants.MESSAGE_ATTRIBUTES, AmazonSQSConstants.MESSAGE_REQUEST_ENTRY };
    }
    /**
     * getParametersMap method used to return list of parameter values sorted by expected API parameter names.
     *
     * @param signatureRequestObject ESB messageContext.
     * @param namesMap contains a map of esb parameter names and matching API parameter names
     * @return assigned parameter values as a HashMap.
     * @throws JSONException 
     */
    private Map<String, String> getSortedParametersMap(final JSONObject signatureRequestObject,
            final Map<String, String> namesMap) throws JSONException {
        final String[] singleValuedKeys = getParameterKeys();
        final Map<String, String> parametersMap = new TreeMap<String, String>();
        // Stores sorted, single valued API parameters
        for (byte index = 0; index < singleValuedKeys.length; index++) {
            final String key = singleValuedKeys[index];
            // builds the parameter map only if provided by the user
            if (signatureRequestObject.has(key) && !("").equals((String) signatureRequestObject.get(key))) {
                parametersMap.put(namesMap.get(key), (String) signatureRequestObject.get(key));
            }
        }
        final String[] multiValuedKeys = getMultivaluedParameterKeys();
        // Stores sorted, multi-valued API parameters
        for (byte index = 0; index < multiValuedKeys.length; index++) {
            final String key = multiValuedKeys[index];
            // builds the parameter map only if provided by the user
            if (signatureRequestObject.has(key) && !("").equals((String) signatureRequestObject.get(key))) {
                final String collectionParam = (String) signatureRequestObject.get(key);
                // Splits the collection parameter to retrieve parameters separately
                final String[] keyValuepairs = collectionParam.split(AmazonSQSConstants.AMPERSAND);
                for (String keyValue : keyValuepairs) {
                    if (keyValue.contains(AmazonSQSConstants.EQUAL)
                            && keyValue.split(AmazonSQSConstants.EQUAL).length == AmazonSQSConstants.TWO) {
                        // Split the key and value of parameters to be sent to API
                        parametersMap.put(keyValue.split(AmazonSQSConstants.EQUAL)[0],
                                keyValue.split(AmazonSQSConstants.EQUAL)[1]);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
        return parametersMap;
    }

    /**
     * getSortedHeadersMap method used to return list of header values sorted by expected API parameter names.
     *
     * @param signatureRequestObject ESB messageContext.
     * @param namesMap contains a map of esb parameter names and matching API parameter names
     * @return assigned header values as a HashMap.
     * @throws JSONException 
     */
    private Map<String, String> getSortedHeadersMap(final JSONObject signatureRequestObject,
            final Map<String, String> namesMap) throws JSONException {

        final String[] headerKeys = getHeaderKeys();
        final Map<String, String> parametersMap = new TreeMap<String, String>();
        // Stores sorted, single valued API parameters
        for (byte index = 0; index < headerKeys.length; index++) {
            final String key = headerKeys[index];
            // builds the parameter map only if provided by the user
            if (signatureRequestObject.has(key) && !("").equals((String) signatureRequestObject.get(key))) {
                parametersMap.put(namesMap.get(key).toLowerCase(), signatureRequestObject.get(key).toString().trim()
                        .replaceAll(AmazonSQSConstants.TRIM_SPACE_REGEX, AmazonSQSConstants.SPACE));
            }
        }
        return parametersMap;
    }

    /**
     * getparameterNamesMap returns a map of esb parameter names and corresponding API parameter names.
     *
     * @return generated map.
     */
    private Map<String, String> getParameterNamesMap() {

        final Map<String, String> map = new HashMap<String, String>();
        map.put(AmazonSQSConstants.ACTION, AmazonSQSConstants.API_ACTION);
        map.put(AmazonSQSConstants.EXPIRES, AmazonSQSConstants.API_EXPIRES);
        map.put(AmazonSQSConstants.SECURITY_TOKEN, AmazonSQSConstants.API_SECURITY_TOKEN);
        map.put(AmazonSQSConstants.SIGNATURE, AmazonSQSConstants.API_SIGNATURE);
        map.put(AmazonSQSConstants.SIGNATURE_METHOD, AmazonSQSConstants.API_SIGNATURE_METHOD);
        map.put(AmazonSQSConstants.SIGNATURE_VERSION, AmazonSQSConstants.API_SIGNATURE_VERSION);
        map.put(AmazonSQSConstants.TIMESTAMP, AmazonSQSConstants.API_TIMESTAMP);
        map.put(AmazonSQSConstants.VERSION, AmazonSQSConstants.API_VERSION);
        map.put(AmazonSQSConstants.ACCESS_KEY_ID, AmazonSQSConstants.AWS_ACCESS_KEY_ID);
        map.put(AmazonSQSConstants.QUEUE_NAME_PREFIX, AmazonSQSConstants.API_QUEUE_NAME_PREFIX);
        map.put(AmazonSQSConstants.QUEUE_URLS, AmazonSQSConstants.API_QUEUE_URLS);
        map.put(AmazonSQSConstants.LABEL, AmazonSQSConstants.API_LABEL);
        map.put(AmazonSQSConstants.PAYLOAD_QUEUE_NAME, AmazonSQSConstants.API_QUEUE_NAME);
        map.put(AmazonSQSConstants.MESSAGE_BODY, AmazonSQSConstants.API_MESSAGE_BODY);
        map.put(AmazonSQSConstants.MESSAGE_GROUP_ID, AmazonSQSConstants.API_MESSAGE_GROUP_ID);
        map.put(AmazonSQSConstants.MESSAGE_DEDUPLICATION_ID, AmazonSQSConstants.API_MESSAGE_DEDUPLICATION_ID);
        map.put(AmazonSQSConstants.RECEIPT_HANDLE, AmazonSQSConstants.API_RECEIPT_HANDLE);
        map.put(AmazonSQSConstants.MAX_NO_OF_MESSAGES, AmazonSQSConstants.API_MAX_NO_OF_MESSAGES);
        map.put(AmazonSQSConstants.VISIBILITY_TIMEOUT, AmazonSQSConstants.API_VISIBILITY_TIMEOUT);
        map.put(AmazonSQSConstants.WAIT_TIME_SECONDS, AmazonSQSConstants.API_WAIT_TIME_SECONDS);
        map.put(AmazonSQSConstants.DELAY_SECONDS, AmazonSQSConstants.API_DELAY_SECONDS);
        map.put(AmazonSQSConstants.ACCOUNT_ID, AmazonSQSConstants.API_ACCOUNT_ID);
        // Header parameters
        map.put(AmazonSQSConstants.HOST, AmazonSQSConstants.API_HOST);
        map.put(AmazonSQSConstants.CONTENT_TYPE, AmazonSQSConstants.API_CONTENT_TYPE);
        map.put(AmazonSQSConstants.AMZ_DATE, AmazonSQSConstants.API_AMZ_DATE);
        
        return map;
    }

    /**
     * Add a Throwable to a message context, the message from the throwable is embedded as the Synapse.
     * Constant ERROR_MESSAGE.
     *
     * @param ctxt message context to which the error tags need to be added
     * @param throwable Throwable that needs to be parsed and added
     * @param errorCode errorCode mapped to the exception
     */
    public final void storeErrorResponseStatus(final MessageContext ctxt, final Throwable throwable, final int errorCode) {
    
        ctxt.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        ctxt.setProperty(SynapseConstants.ERROR_MESSAGE, throwable.getMessage());
        ctxt.setFaultResponse(true);
    }

    /**
     * Add a message to message context, the message from the throwable is embedded as the Synapse Constant
     * ERROR_MESSAGE.
     *
     * @param ctxt message context to which the error tags need to be added
     * @param message message to be returned to the user
     * @param errorCode errorCode mapped to the exception
     */
    public final void storeErrorResponseStatus(final MessageContext ctxt, final String message, final int errorCode) {

        ctxt.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        ctxt.setProperty(SynapseConstants.ERROR_MESSAGE, message);
        ctxt.setFaultResponse(true);
    }

    /**
     * Hashes the string contents (assumed to be UTF-8) using the SHA-256 algorithm.
     *
     * @param text text to be hashed
     *
     * @return SHA-256 hashed text
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException
     */
    public final byte[] hash(final String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest messageDigest = null;
            messageDigest = MessageDigest.getInstance(AmazonSQSConstants.SHA_256);
            messageDigest.update(text.getBytes(AmazonSQSConstants.UTF_8));
        return messageDigest.digest();
    }


    /**
     * bytesToHex method HexEncoded the received byte array.
     *
     * @param bytes bytes to be hex encoded
     * @return hex encoded String of the given byte array
     */
    public static String bytesToHex(final byte[] bytes) {

        final char[] hexArray = AmazonSQSConstants.HEX_ARRAY_STRING.toCharArray();
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            final int byteVal = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[byteVal >>> 4];
            hexChars[j * 2 + 1] = hexArray[byteVal & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Returns the encoded signature key to be used for further encodings as per API doc.
     *
     * @param signatureRequestObject message context of the connector
     * @param key key to be used for signing
     * @param dateStamp current date stamp
     * @param regionName region name given to the connector
     * @param serviceName Name of the service being addressed
     *
     * @return Signature key
     * @throws java.io.UnsupportedEncodingException  Unsupported Encoding Exception
     * @throws IllegalStateException  Illegal Argument Exception
     * @throws java.security.NoSuchAlgorithmException  No Such Algorithm Exception
     * @throws java.security.InvalidKeyException  Invalid Key Exception
     * @throws JSONException 
     */
    private byte[] getSignatureKey(final JSONObject signatureRequestObject, final String key, final String dateStamp, final String regionName,
            final String serviceName) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, JSONException {

        final byte[] kSecret = (AmazonSQSConstants.AWS4 + key).getBytes(AmazonSQSConstants.UTF8);
        final byte[] kDate = hmacSHA256(kSecret, dateStamp);
        final byte[] kRegion = hmacSHA256(kDate, regionName);
        final byte[] kService = hmacSHA256(kRegion, serviceName);
        return hmacSHA256(kService, signatureRequestObject.get(AmazonSQSConstants.TERMINATION_STRING).toString());
    }

    /**
     * Provides the HMAC SHA 256 encoded value(using the provided key) of the given data.
     *
     * @param key to use for encoding
     * @param data to be encoded
     *
     * @return HMAC SHA 256 encoded byte array
     * @throws java.security.NoSuchAlgorithmException No such algorithm Exception
     * @throws java.security.InvalidKeyException Invalid key Exception
     * @throws java.io.UnsupportedEncodingException Unsupported Encoding Exception
     * @throws IllegalStateException Illegal State Exception
     */
    private byte[] hmacSHA256(final byte[] key, final String data) throws NoSuchAlgorithmException, InvalidKeyException,
            IllegalStateException, UnsupportedEncodingException{

        final String algorithm = AmazonSQSConstants.HAMC_SHA_256;
        final Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes(AmazonSQSConstants.UTF8));
    }
}
