package org.apache.axis2.engine.util;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.integration.UtilServer;

import javax.xml.namespace.QName;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

/**
 * This will contain the information shared across the integration test cases.
 */
public interface TestConstants {
    public static final EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
                    + "/axis/services/EchoXMLService/echoOMElement");

    public static final QName serviceName = new QName("EchoXMLService");

    public static final QName operationName = new QName("echoOMElement");
}
