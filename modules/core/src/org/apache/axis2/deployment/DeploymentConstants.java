/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*/


package org.apache.axis2.deployment;

/**
 * Constants used during service/module deployment
 */
public interface DeploymentConstants {
    public static String META_INF = "META-INF";
    public static String SERVICES_XML = "META-INF/services.xml";
    public static String MODULE_XML = "META-INF/module.xml";
    public static String SERVICE_PATH = "/services/";
    public static String MODULE_PATH = "/modules/";

    int TYPE_SERVICE = 0;                // is it a service
    int TYPE_MODULE = 1;                // is it a module
    
    String TAG_PHASE_ORDER = "phaseOrder";
    String TAG_PHASE = "phase";
    String TAG_PARAMETER = "parameter";     
    String TAG_MODULE = "module";
    String TAG_MODULE_CONFIG = "moduleConfig";
    String TAG_MESSAGE = "message";
    String TAG_LISTENER = "listener";     
    String TAG_LABEL = "label";
    String TAG_HOST_CONFIG = "hostConfiguration";
    String TAG_HANDLER = "handler";
    String TAG_TYPE = "type";
    String TAG_TRANSPORT_SENDER = "transportSender";
    String TAG_TRANSPORT_RECEIVER = "transportReceiver";
    String TAG_SERVICE_GROUP = "serviceGroup";
    String TAG_SERVICE = "service";
    String TAG_REFERENCE = "ref";
    String TAG_PHASE_LAST = "phaseLast";
    String TAG_PHASE_FIRST = "phaseFirst";
    String TAG_ORDER = "order";           // to resolve the order tag
    String TAG_OPERATION = "operation";       // operation start tag
    String TAG_MESSAGE_RECEIVER = "messageReceiver";
    String TAG_MEP = "mep";

    String TAG_FLOW_OUT_FAULT = "Outfaultflow";    // faultflow start tag
    String TAG_FLOW_OUT = "outflow";         // outflow start tag
    String TAG_FLOW_IN_FAULT = "INfaultflow";    // faultflow start tag
    String TAG_FLOW_IN = "inflow";         // inflow start tag
    String TAG_HOT_UPDATE = "hotupdate";
    String TAG_HOT_DEPLOYMENT = "hotdeployment";
    String TAG_EXTRACT_SERVICE_ARCHIVE = "extractServiceArchive";
    String TAG_DISPATCH_ORDER = "dispatchOrder";
    String TAG_DISPATCHER = "dispatcher";
    String TAG_DESCRIPTION = "description";
    String TAG_CLASS_NAME = "class";
    String TAG_AFTER = "after";
    String TAG_BEFORE = "before";

    // for parameters
    String ATTRIBUTE_NAME = "name";
    String ATTRIBUTE_LOCKED = "locked";
}
