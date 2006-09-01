package org.apache.axis2.wsdl.codegen.emitter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.XSLTUtils;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.writer.AntBuildWriter;
import org.apache.axis2.wsdl.codegen.writer.CallbackHandlerWriter;
import org.apache.axis2.wsdl.codegen.writer.ClassWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceImplementationWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceWriter;
import org.apache.axis2.wsdl.codegen.writer.MessageReceiverWriter;
import org.apache.axis2.wsdl.codegen.writer.SchemaWriter;
import org.apache.axis2.wsdl.codegen.writer.ServiceXMLWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonInterfaceWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonWriter;
import org.apache.axis2.wsdl.codegen.writer.TestClassWriter;
import org.apache.axis2.wsdl.codegen.writer.WSDL11Writer;
import org.apache.axis2.wsdl.codegen.writer.WSDL20Writer;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.wsdl.util.XSLTIncludeResolver;
import org.apache.axis2.wsdl.util.MessagePartInformationHolder;
import org.apache.axis2.wsdl.util.TypeTesterUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.policy.Policy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
//import com.ibm.wsdl.util.xml.DOM2Writer;

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


public class AxisServiceBasedMultiLanguageEmitter implements Emitter {

    protected static final String CALL_BACK_HANDLER_SUFFIX = "CallbackHandler";
    protected static final String STUB_SUFFIX = "Stub";
    protected static final String TEST_SUFFIX = "Test";
    protected static final String SKELETON_CLASS_SUFFIX = "Skeleton";
    protected static final String SKELETON_INTERFACE_SUFFIX = "SkeletonInterface";
    protected static final String MESSAGE_RECEIVER_SUFFIX = "MessageReceiver";
    protected static final String FAULT_SUFFIX = "Exception";
    protected static final String DATABINDING_SUPPORTER_NAME_SUFFIX = "DatabindingSupporter";

    protected static Map mepToClassMap;
    protected static Map mepToSuffixMap;

    protected int uniqueFaultNameCounter = 0;
    /**
     * Field constructorMap
     */
    protected static HashMap constructorMap = new HashMap(50);

    //~--- static initializers ------------------------------------------------

    static {

        // Type maps to a valid initialization value for that type
        // Type var = new Type(arg)
        // Where "Type" is the key and "new Type(arg)" is the string stored
        // Used in emitting test cases and server skeletons.
        constructorMap.put("int", "0");
        constructorMap.put("float", "0");
        constructorMap.put("boolean", "true");
        constructorMap.put("double", "0");
        constructorMap.put("byte", "(byte)0");
        constructorMap.put("short", "(short)0");
        constructorMap.put("long", "0");
        constructorMap.put("java.lang.Boolean", "new java.lang.Boolean(false)");
        constructorMap.put("java.lang.Byte", "new java.lang.Byte((byte)0)");
        constructorMap.put("java.lang.Double", "new java.lang.Double(0)");
        constructorMap.put("java.lang.Float", "new java.lang.Float(0)");
        constructorMap.put("java.lang.Integer", "new java.lang.Integer(0)");
        constructorMap.put("java.lang.Long", "new java.lang.Long(0)");
        constructorMap.put("java.lang.Short", "new java.lang.Short((short)0)");
        constructorMap.put("java.math.BigDecimal", "new java.math.BigDecimal(0)");
        constructorMap.put("java.math.BigInteger", "new java.math.BigInteger(\"0\")");
        constructorMap.put("java.lang.Object", "new java.lang.String()");
        constructorMap.put("byte[]", "new byte[0]");
        constructorMap.put("java.util.Calendar", "java.util.Calendar.getInstance()");
        constructorMap.put("javax.xml.namespace.QName",
                "new javax.xml.namespace.QName(\"http://foo\", \"bar\")");

        //populate the MEP -> class map
        mepToClassMap = new HashMap();
        mepToClassMap.put(WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY, "org.apache.axis2.receivers.AbstractInMessageReceiver");
        mepToClassMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY, "org.apache.axis2.receivers.AbstractInMessageReceiver");
        mepToClassMap.put(WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT, "org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver");
        mepToClassMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT, "org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver");

        //populate the MEP -> suffix map
        mepToSuffixMap = new HashMap();
        mepToSuffixMap.put(WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY, MESSAGE_RECEIVER_SUFFIX + "InOnly");
        mepToSuffixMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY, MESSAGE_RECEIVER_SUFFIX + "InOnly");
        mepToSuffixMap.put(WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT, MESSAGE_RECEIVER_SUFFIX + "InOut");
        mepToSuffixMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT, MESSAGE_RECEIVER_SUFFIX + "InOut");
        //register the other types as necessary
    }

    //~--- fields -------------------------------------------------------------
    protected static final Log log = LogFactory.getLog(AxisServiceBasedMultiLanguageEmitter.class);
    protected URIResolver resolver;

    protected Map infoHolder;

    protected CodeGenConfiguration codeGenConfiguration;

    protected TypeMapper mapper;

    protected AxisService axisService;

    //a map to keep the fault classNames
    protected Map fullyQualifiedFaultClassNameMap = new HashMap();
    protected Map InstantiatableFaultClassNameMap = new HashMap();
    protected Map faultClassNameMap = new HashMap();

    protected Map instantiatableMessageClassNames = new HashMap();

    protected static final String SRC_DIR_NAME = "src";
    protected static final String TEST_SRC_DIR_NAME = "test";
    protected static final String RESOURCE_SRC_DIR_NAME = "resources";


    /**
     * default constructor - builds
     */
    public AxisServiceBasedMultiLanguageEmitter() {
        infoHolder = new HashMap();
    }

    /**
     * Sets the relevant codegen configuration
     * @see Emitter#setCodeGenConfiguration(org.apache.axis2.wsdl.codegen.CodeGenConfiguration)
     * @param configuration
     */
    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.codeGenConfiguration = configuration;
        this.axisService = codeGenConfiguration.getAxisService();
        resolver = new XSLTIncludeResolver(codeGenConfiguration);
    }

    /**
     * Sets the type mapper
     * @see Emitter#setMapper(org.apache.axis2.wsdl.databinding.TypeMapper)
     * @param mapper
     */
    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }



    /**
     * Update mapper for the stub
     */
    protected void updateMapperForStub() {
        updateMapperClassnames(getFullyQualifiedStubName());
    }

    /**
     * Returns the fully qualified Stub name
     * reused in many methods
     * @return
     */
    protected String getFullyQualifiedStubName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + STUB_SUFFIX;
    }

    /**
     * rests the fault name maps
     */
    protected void resetFaultNames() {
        fullyQualifiedFaultClassNameMap.clear();
        faultClassNameMap.clear();
    }

    /**
     * Populate a map of fault class names
     */
    protected void generateAndPopulateFaultNames() {
        //loop through and find the faults
        Iterator operations = axisService.getOperations();
        AxisOperation operation;
        AxisMessage faultMessage;
        while (operations.hasNext()) {
            operation = (AxisOperation) operations.next();
            ArrayList faultMessages = operation.getFaultMessages();
            for (int i = 0; i < faultMessages.size(); i++) {
                faultMessage = (AxisMessage) faultMessages.get(i);
                //make a unique name and put that in the hashmap
                if (!fullyQualifiedFaultClassNameMap.
                        containsKey(faultMessage.getElementQName())) {
                    //make a name
                    String className = makeJavaClassName(faultMessage.getName()
                            + FAULT_SUFFIX);
                    while (fullyQualifiedFaultClassNameMap.containsValue(className)) {
                        className = makeJavaClassName(className + (uniqueFaultNameCounter++));
                    }

                    fullyQualifiedFaultClassNameMap.put(
                            faultMessage.getElementQName(),
                            className);
                    //this needs to be kept seperate and updated later
                    InstantiatableFaultClassNameMap.put(
                            faultMessage.getElementQName(),
                            className);
                    //we've to keep track of the fault base names seperately
                    faultClassNameMap.put(faultMessage.getElementQName(),
                            className);

                }
            }

        }
    }

    /**
     * Emits the stubcode with bindings.
     * @see org.apache.axis2.wsdl.codegen.emitter.Emitter#emitStub()
     * @throws Exception
     */
    public void emitStub() throws CodeGenerationException {
        try{
            // see the comment at updateMapperClassnames for details and reasons for
            // calling this method
            if (mapper.isObjectMappingPresent()) {
                updateMapperForStub();
            } else {
                copyToFaultMap();
            }

            //generate and populate the fault names before hand. We need that for
            //the smooth opration of the thing
            //first reset the fault names and recreate it
            resetFaultNames();
            generateAndPopulateFaultNames();
            updateFaultPackageForStub();

            // write the inteface
            // feed the binding information also
            // note that we do not create this interface if the user switched on the wrap classes mode
            if (!codeGenConfiguration.isPackClasses()) {
                writeInterface(false);
            }

            // write the call back handlers
            writeCallBackHandlers();

            // write interface implementations
            writeInterfaceImplementation();

            // write the test classes
            writeTestClasses();

            // write an ant build file
            // Note that ant build is generated only once
            // and that has to happen here only if the
            // client side code is required
            if (!codeGenConfiguration.isGenerateAll()) {
                //our logic for the build xml is that it will
                //only be written when not flattened
                if (!codeGenConfiguration.isFlattenFiles()) {
                    writeAntBuild();
                }
            }
        }catch(CodeGenerationException ce){
            throw ce;
        }catch(Exception e){
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Writes the Ant build.
     *
     * @throws Exception
     */
    protected void writeAntBuild() throws Exception {

        // Write the service xml in a folder with the
        Document skeletonModel = createDOMDocumentForAntBuild();
        debugLogDocument("Document for ant build:", skeletonModel);
        AntBuildWriter antBuildWriter = new AntBuildWriter(codeGenConfiguration.getOutputLocation(),
                codeGenConfiguration.getOutputLanguage());

        antBuildWriter.setDatabindingFramework(codeGenConfiguration.getDatabindingType());
        writeClass(skeletonModel, antBuildWriter);
    }

    /**
     * Creates the DOM tree for the Ant build. Uses the interface.
     */
    protected Document createDOMDocumentForAntBuild() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("ant");
        String serviceName = makeJavaClassName(axisService.getName());
        String packageName = codeGenConfiguration.getPackageName();
        String[] dotSeparatedValues = packageName.split("\\.");

        addAttribute(doc, "package", dotSeparatedValues[0], rootElement);
        addAttribute(doc, "name", serviceName, rootElement);
        addAttribute(doc, "servicename", serviceName, rootElement);
        if (codeGenConfiguration.isServerSide()) {
            addAttribute(doc,
                    "isserverside",
                    "yes",
                    rootElement);
        }

        doc.appendChild(rootElement);

        return doc;
    }

    /**
     * Write the test classes
     */
    protected void writeTestClasses() throws Exception {
        if (codeGenConfiguration.isWriteTestCase()) {
            Document classModel = createDOMDocumentForTestCase();
            debugLogDocument("Document for test case:", classModel);
            TestClassWriter callbackWriter =
                    new TestClassWriter(
                            codeGenConfiguration.isFlattenFiles() ?
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(), TEST_SRC_DIR_NAME),
                            codeGenConfiguration.getOutputLanguage());

            writeClass(classModel, callbackWriter);
        }
    }

    /**
     * Creates the XML Model for the test case
     * @return
     */
    protected Document createDOMDocumentForTestCase() {
        String coreClassName = makeJavaClassName(axisService.getName());
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", coreClassName + TEST_SUFFIX, rootElement);
        //todo is this right ???
        addAttribute(doc, "namespace", axisService.getTargetNamespace(), rootElement);
        addAttribute(doc, "interfaceName", coreClassName, rootElement);
        addAttribute(doc, "callbackname", coreClassName + CALL_BACK_HANDLER_SUFFIX, rootElement);
        addAttribute(doc, "stubname", coreClassName + STUB_SUFFIX, rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        // add the databind supporters. Now the databind supporters are completly contained inside
        // the stubs implementation and not visible outside
        rootElement.appendChild(createDOMElementforDatabinders(doc,false));
        doc.appendChild(rootElement);

        return doc;
    }

    /**
     * Writes the implementations.
     *
     * @throws Exception
     */
    protected void writeInterfaceImplementation() throws Exception {

        // first check for the policies in this service and write them
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation();
        debugLogDocument("Document for interface implementation:", interfaceImplModel);
        InterfaceImplementationWriter writer =
                new InterfaceImplementationWriter(
                        codeGenConfiguration.isFlattenFiles() ?
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), SRC_DIR_NAME),
                        codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceImplModel, writer);
    }

    /**
     * Creates the DOM tree for implementations.
     */
    protected Document createDOMDocumentForInterfaceImplementation() throws Exception {

        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        String stubName = localPart + STUB_SUFFIX;
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");

        addAttribute(doc, "package", packageName, rootElement);
        addAttribute(doc, "name", stubName, rootElement);
        addAttribute(doc, "servicename", localPart, rootElement);
        //The target nemespace is added as the namespace for this service
        addAttribute(doc, "namespace", axisService.getTargetNamespace(), rootElement);
        addAttribute(doc, "interfaceName", localPart, rootElement);
        addAttribute(doc, "callbackname", localPart + CALL_BACK_HANDLER_SUFFIX, rootElement);

        // add the wrap classes flag
        if (codeGenConfiguration.isPackClasses()) {
            addAttribute(doc, "wrapped", "yes", rootElement);
        }

        // add SOAP version
        addSoapVersion(doc, rootElement);

        // add the end point
        addEndpoint(doc, rootElement);

        // set the sync/async attributes
        fillSyncAttributes(doc, rootElement);

        // ###########################################################################################
        // this block of code specifically applies to the integration of databinding code into the
        // generated classes tightly (probably as inner classes)
        // ###########################################################################################
        // check for the special models in the mapper and if they are present process them
        if (mapper.isObjectMappingPresent()) {

            // add an attribute to the root element showing that the writing has been skipped
            addAttribute(doc, "skip-write", "yes", rootElement);

            // process the mapper objects
            processModelObjects(mapper.getAllMappedObjects(), rootElement, doc);
        }

        // #############################################################################################

        // load the operations
        loadOperations(doc, rootElement, null);

        // add the databind supporters. Now the databind supporters are completly contained inside
        // the stubs implementation and not visible outside
        rootElement.appendChild(createDOMElementforDatabinders(doc,false));

        Object moduleCodegenPolicyExtensionElement;

        //if some extension has added the stub methods property, add them to the
        //main document
        if ((moduleCodegenPolicyExtensionElement = codeGenConfiguration.getProperty("module-codegen-policy-extensions")) != null) {
            rootElement.appendChild(doc.importNode((Element) moduleCodegenPolicyExtensionElement, true));
        }

        //add another element to have the unique list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));


        doc.appendChild(rootElement);

        //////////////////////////////////////////////////////////
        //System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;
    }

    /**
     * A util method that returns a unique list of faults
     *
     * @param doc
     * @return
     */
    protected Element getUniqueListofFaults(Document doc) {
        Element rootElement = doc.createElement("fault-list");
        Element faultElement;
        QName key;
        Iterator iterator = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (iterator.hasNext()) {
            faultElement = doc.createElement("fault");
            key = (QName) iterator.next();

            //as for the name of a fault, we generate an exception
            addAttribute(doc, "name",
                    (String) fullyQualifiedFaultClassNameMap.get(key),
                    faultElement);
            addAttribute(doc, "intantiatiableName",
                    (String) InstantiatableFaultClassNameMap.get(key),
                    faultElement);
            addAttribute(doc, "shortName",
                    (String) faultClassNameMap.get(key),
                    faultElement);

            //the type represents the type that will be wrapped by this
            //name
            String typeMapping =
                    this.mapper.getTypeMappingName(key);
            addAttribute(doc, "type", (typeMapping == null)
                    ? ""
                    : typeMapping, faultElement);
            String attribValue = (String) instantiatableMessageClassNames.
                    get(key);

            addAttribute(doc, "instantiatableType",
                    attribValue == null ? "" : attribValue,
                    faultElement);

            // add an extra attribute to say whether the type mapping is
            // the default
            if (mapper.getDefaultMappingName().equals(typeMapping)) {
                addAttribute(doc, "default", "yes", faultElement);
            }
            addAttribute(doc, "value", getParamInitializer(typeMapping),
                    faultElement);


            rootElement.appendChild(faultElement);
        }
        return rootElement;
    }

    /**
     * Adds the endpoint to the document.
     *
     * @param doc
     * @param rootElement
     */
    protected void addEndpoint(Document doc, Element rootElement) throws Exception {

        PolicyInclude policyInclude = axisService.getPolicyInclude();
        Policy servicePolicy = policyInclude.getPolicy();

        if (servicePolicy != null) {
            String policyString = PolicyUtil.getPolicyAsString(servicePolicy);
            addAttribute(doc, "policy", policyString, rootElement);
        }

        Element endpointElement = doc.createElement("endpoint");

        String endpoint = axisService.getEndpoint();
        Text text = doc.createTextNode((endpoint != null)
                ? endpoint
                : "");

        endpointElement.appendChild(text);
        rootElement.appendChild(endpointElement);
    }

    /**
     * Looks for the SOAPVersion and adds it.
     *
     * @param doc
     * @param rootElement
     */
    protected void addSoapVersion(Document doc, Element rootElement) {

        // loop through the extensibility elements to get to the bindings element

        String soapNsUri = axisService.getSoapNsUri();
        if (org.apache.axis2.namespace.Constants.URI_WSDL11_SOAP.equals(soapNsUri)) {
            addAttribute(doc, "soap-version", "1.1", rootElement);
        } else if (org.apache.axis2.namespace.Constants.URI_WSDL12_SOAP.equals(soapNsUri)) {
            addAttribute(doc, "soap-version", "1.2", rootElement);
        }

    }


    /**
     * Writes the callback handlers.
     */
    protected void writeCallBackHandlers() throws Exception {
        if (codeGenConfiguration.isAsyncOn()) {
            Document interfaceModel = createDOMDocumentForCallbackHandler();
            debugLogDocument("Document for callback handler:", interfaceModel);
            CallbackHandlerWriter callbackWriter =
                    new CallbackHandlerWriter(
                            codeGenConfiguration.isFlattenFiles() ?
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(), SRC_DIR_NAME),
                            codeGenConfiguration.getOutputLanguage());

            writeClass(interfaceModel, callbackWriter);
        }
    }

    /**
     * Generates the model for the callbacks.
     */
    protected Document createDOMDocumentForCallbackHandler() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("callback");

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", makeJavaClassName(axisService.getName()) + CALL_BACK_HANDLER_SUFFIX, rootElement);

        // TODO JAXRPC mapping support should be considered here ??
        this.loadOperations(doc, rootElement, null);

        doc.appendChild(rootElement);
        return doc;
    }

    /**
     * Writes the interfaces.
     *
     * @throws Exception
     */
    protected void writeInterface(boolean writeDatabinders) throws Exception {
        Document interfaceModel = createDOMDocumentForInterface(writeDatabinders);
        debugLogDocument("Document for interface:", interfaceModel);
        InterfaceWriter interfaceWriter =
                new InterfaceWriter(
                        codeGenConfiguration.isFlattenFiles() ?
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), SRC_DIR_NAME),
                        this.codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceModel, interfaceWriter);
    }

    /**
     * Creates the DOM tree for the interface creation. Uses the interface.
     */
    protected Document createDOMDocumentForInterface(boolean writeDatabinders) {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        String localPart = makeJavaClassName(axisService.getName());

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", localPart, rootElement);
        addAttribute(doc, "callbackname", localPart + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        // ###########################################################################################
        // this block of code specifically applies to the integration of databinding code into the
        // generated classes tightly (probably as inner classes)
        // ###########################################################################################
        // check for the special models in the mapper and if they are present process them
        if (writeDatabinders) {
            if (mapper.isObjectMappingPresent()) {

                // add an attribute to the root element showing that the writing has been skipped
                addAttribute(doc, "skip-write", "yes", rootElement);

                // process the mapper objects
                processModelObjects(mapper.getAllMappedObjects(), rootElement, doc);
            }
        }

        // #############################################################################################
        doc.appendChild(rootElement);

        return doc;
    }



    /**
     * Update mapper for message receiver
     */
    protected void updateMapperForMessageReceiver() {
        updateMapperClassnames(getFullyQualifiedMessageReceiverName());
    }

    /**
     * @return fully qualified MR name
     */
    protected String getFullyQualifiedMessageReceiverName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + MESSAGE_RECEIVER_SUFFIX;
    }

    /**
     * @return fully qualified skeleton name
     */
    protected String getFullyQualifiedSkeletonName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + SKELETON_CLASS_SUFFIX;
    }

    /**
     * @return fully qualified skeleton interface name
     */
    protected String getFullyQualifiedSkeletonInterfaceName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + SKELETON_INTERFACE_SUFFIX;
    }
    /**
     * Emits the skeleton
     * @throws Exception
     */
    public void emitSkeleton() throws CodeGenerationException {

        try{
            // see the comment at updateMapperClassnames for details and reasons for
            // calling this method
            if (mapper.isObjectMappingPresent()) {
                updateMapperForMessageReceiver();
            } else {
                copyToFaultMap();
            }

            //handle faults
            generateAndPopulateFaultNames();
            updateFaultPackageForSkeleton(codeGenConfiguration.isServerSideInterface());

            //
            if (codeGenConfiguration.isServerSideInterface()) {
                //write skeletonInterface
                writeSkeletonInterface();
            }

            // write skeleton only if the used has
            // asked for the deployment descriptor in the interface mode
            // else write it anyway :)
            if (codeGenConfiguration.isServerSideInterface()){
                 if (codeGenConfiguration.isGenerateDeployementDescriptor()){
                     writeSkeleton();
                 }
            }else{
               writeSkeleton();
            }

            // write a MessageReceiver for this particular service.
            writeMessageReceiver();

            // write service xml
            // if asked
            if (codeGenConfiguration.isGenerateDeployementDescriptor()) {
                writeServiceXml();
            }

            //write the ant build
            //we skip this for the flattened case
            if (!codeGenConfiguration.isFlattenFiles()) {
                writeAntBuild();
            }

            //for the server side codegen
            //we need to serialize the WSDL's
            writeWSDLFiles();

        }catch(CodeGenerationException cgExp){
            throw cgExp;
        }catch(Exception e){
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Write out the WSDL files (and the schemas)
     * writing the WSDL (and schemas) is somewhat special so we cannot follow
     * the usual pattern of using the class writer
     */
    protected void writeWSDLFiles() {

        //first modify the schema names (and locations) so that
        //they have unique (flattened) names and the schema locations
        //are adjusted to suit it
        axisService.setCustomSchemaNamePrefix("");//prefix with nothing
        axisService.setCustomSchemaNameSuffix(".xsd");//suffix with .xsd - the file name extension
        //force the mappings to be reconstructed
        axisService.setSchemaLocationsAdjusted(false);
        axisService.populateSchemaMappings();

        //now get the schema list and write it out
        SchemaWriter schemaWriter = new SchemaWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), RESOURCE_SRC_DIR_NAME));


        Map schemaMappings = axisService.getSchemaMappingTable();
        Iterator keys = schemaMappings.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            schemaWriter.writeSchema(
                    (XmlSchema) schemaMappings.get(key),
                    (String) key
            );
        }

        //switch between the correct writer
        if (CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_2.
                equals(codeGenConfiguration.getWSDLVersion())) {

            WSDL20Writer wsdl20Writer = new WSDL20Writer(
                    codeGenConfiguration.isFlattenFiles() ?
                            getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                            getOutputDirectory(codeGenConfiguration.getOutputLocation(), RESOURCE_SRC_DIR_NAME)
            );
            wsdl20Writer.writeWSDL(axisService);
        } else {

            WSDL11Writer wsdl11Writer = new WSDL11Writer(
                    codeGenConfiguration.isFlattenFiles() ?
                            getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                            getOutputDirectory(codeGenConfiguration.getOutputLocation(), RESOURCE_SRC_DIR_NAME));
            wsdl11Writer.writeWSDL(axisService);

        }


    }

    /**
     * Utility method to copy the faults to the correct map
     */
    protected void copyToFaultMap() {
        Map classNameMap = mapper.getAllMappedNames();
        Iterator keys = classNameMap.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            instantiatableMessageClassNames.put(key,
                    classNameMap.get(key));
        }
    }

    /**
     *  Change the fault classnames to go with the package and stub
     */
    protected void updateFaultPackageForStub() {
        Iterator faultClassNameKeys = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (faultClassNameKeys.hasNext()) {
            Object key = faultClassNameKeys.next();
            String className = (String) fullyQualifiedFaultClassNameMap.get(key);
            //append the skelton name
            String fullyQualifiedStubName = getFullyQualifiedStubName();
            fullyQualifiedFaultClassNameMap.put(key, fullyQualifiedStubName + "."
                    + className);
            InstantiatableFaultClassNameMap.put(key, fullyQualifiedStubName + "$"
                    + className);
        }
    }

    /**
     *  Change the fault classnames to go with the package & class of the
     *  skeleton
     *  the faults are always generated as inner types
     */
    protected void updateFaultPackageForSkeleton(boolean isInterface) {
        Iterator faultClassNameKeys = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (faultClassNameKeys.hasNext()) {
            Object key = faultClassNameKeys.next();
            String className = (String) fullyQualifiedFaultClassNameMap.get(key);
            //append the skelton name
            String fullyQualifiedSkeletonName = null;
            if (isInterface){
                fullyQualifiedSkeletonName = getFullyQualifiedSkeletonInterfaceName();
            }else{
                fullyQualifiedSkeletonName = getFullyQualifiedSkeletonName();
            }
            fullyQualifiedFaultClassNameMap.put(key, fullyQualifiedSkeletonName + "."
                    + className);
            InstantiatableFaultClassNameMap.put(key, fullyQualifiedSkeletonName + "$"
                    + className);
        }
    }

    /**
     * Writes the message receiver
     * @throws Exception
     */
    protected void writeMessageReceiver() throws Exception {

        if (codeGenConfiguration.isWriteMessageReceiver()) {
            //loop through the meps and generate code for each mep
            Iterator it = mepToClassMap.keySet().iterator();
            while (it.hasNext()) {
                String mep = (String) it.next();
                Document classModel = createDocumentForMessageReceiver(
                        mep,
                        codeGenConfiguration.isServerSideInterface());
                debugLogDocument("Document for message receiver:", classModel);
                //write the class only if any methods are found
                if (Boolean.TRUE.equals(infoHolder.get(mep))) {
                    MessageReceiverWriter writer =
                            new MessageReceiverWriter(
                                    codeGenConfiguration.isFlattenFiles() ?
                                            getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                            getOutputDirectory(codeGenConfiguration.getOutputLocation(), SRC_DIR_NAME),
                                    codeGenConfiguration.getOutputLanguage());

                    writeClass(classModel, writer);
                }
            }
        }
    }

    /**
     * Creates the XML model for the message receiver
     * @param mep
     * @param isServerSideInterface
     * @return
     */
    protected Document createDocumentForMessageReceiver(String mep, boolean isServerSideInterface) {

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);

        String localPart = makeJavaClassName(axisService.getName());

        addAttribute(doc, "name", localPart + mepToSuffixMap.get(mep), rootElement);
        addAttribute(doc, "skeletonname", localPart + SKELETON_CLASS_SUFFIX, rootElement);
        if (isServerSideInterface) {
            addAttribute(doc, "skeletonInterfaceName", localPart + SKELETON_INTERFACE_SUFFIX,
                    rootElement);
        } else {
            addAttribute(doc, "skeletonInterfaceName", localPart + SKELETON_CLASS_SUFFIX,
                    rootElement);
        }
        addAttribute(doc, "basereceiver", (String) mepToClassMap.get(mep), rootElement);

        fillSyncAttributes(doc, rootElement);

        // ###########################################################################################
        // this block of code specifically applies to the integration of databinding code into the
        // generated classes tightly (probably as inner classes)
        // ###########################################################################################
        // check for the special models in the mapper and if they are present process them
        if (mapper.isObjectMappingPresent()) {
            // add an attribute to the root element showing that the writing has been skipped
            addAttribute(doc, "skip-write", "yes", rootElement);
            // process the mapper objects
            processModelObjects(mapper.getAllMappedObjects(), rootElement, doc);
        }
        // #############################################################################################

        boolean isOpsFound = loadOperations(doc, rootElement, mep);
        //put the result in the property map
        infoHolder.put(mep, isOpsFound ? Boolean.TRUE : Boolean.FALSE);
        //create the databinder element with serverside as true
        rootElement.appendChild(createDOMElementforDatabinders(doc,true));

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);

        //////////////////////////////////////////////////////////
        //System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////

        return doc;
    }

    /**
     * create a dom element for databinders. This is called by other
     *
     * @param doc
     */
    protected Element createDOMElementforDatabinders(Document doc,boolean isServerside) {

        // First Iterate through the operations and find the relevant fromOM and toOM methods to be generated
        ArrayList parameters = new ArrayList();

        for (Iterator operationsIterator = axisService.getOperations();operationsIterator.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operationsIterator.next();
            // Add the parameters to a map with their type as the key
            // this step is needed to remove repetitions

            // process the input parameters
            String MEP = axisOperation.getMessageExchangePattern();
            if (WSDLUtil.isInputPresentForMEP(MEP)) {
                Element[] inputParamElement = getInputParamElement(doc, axisOperation);
                for (int i = 0; i < inputParamElement.length; i++) {
                    //add an attribute to the parameter saying that this is an
                    //input
                    addAttribute(doc,"direction","in",inputParamElement[i]);
                    //add the short type name
                    parameters.add(
                            inputParamElement[i]);

                }
            }
            // process output parameters
            if (WSDLUtil.isOutputPresentForMEP(MEP)) {
                Element outputParamElement = getOutputParamElement(doc, axisOperation);
                if (outputParamElement != null) {
                    //set the direction as out
                    addAttribute(doc,"direction","out",outputParamElement);
                    parameters.add(outputParamElement);
                }
            }

            //process faults
            Element[] faultParamElements = getFaultParamElements(doc, axisOperation);
            for (int i = 0; i < faultParamElements.length; i++) {
                //set the direction as out - all faults are out messages ?
                addAttribute(doc,"direction","out",faultParamElements[i]);
                parameters.add(faultParamElements[i]);
            }

            // process the header parameters
            Element newChild;
            List headerParameterQNameList = new ArrayList();
            addHeaderOperations(headerParameterQNameList, axisOperation, true);
            List parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameters.add(newChild);
            }

            headerParameterQNameList.clear();
            parameterElementList.clear();
            addHeaderOperations(headerParameterQNameList, axisOperation, false);
            parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameters.add(newChild);
            }
        }

        Element rootElement = doc.createElement("databinders");
        //add the db type attribute  - the name of the databinding type
        //this will be used to select the correct template
        addAttribute(doc, "dbtype", codeGenConfiguration.getDatabindingType(), rootElement);

        //at this point we may need to capture the extra parameters passes to the
        //particular databinding framework
        //these parameters showup in the property map and we can just copy these items over
        //to an extra element.
        Element extraElement = addElement(doc, "extra", null, rootElement);
        Map propertiesMap = codeGenConfiguration.getProperties();
        for (Iterator it = propertiesMap.keySet().iterator();
             it.hasNext();){
            Object key = it.next();
            Object value = propertiesMap.get(key);
            //if the value is null set it to empty string
            if (value==null) value="";
            //add the property to the extra element only if both
            //are strings
            if (key instanceof String && value instanceof String){
                 addAttribute(doc,(String)key,(String)value, extraElement);
            }
        }

        //add the server side attribute. this helps the databinding template
        //to determine the methods to generate
        if (isServerside){
            addAttribute(doc,"isserverside","yes",rootElement);
        }
        // add the names of the elements that have base 64 content
        // if the base64 name list is missing then this whole step is skipped
        rootElement.appendChild(getBase64Elements(doc));

        //add the method names
        rootElement.appendChild(getOpNames(doc));

        for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
            rootElement.appendChild((Element) iterator.next());
        }


        ///////////////////////////////////////////////
        //System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////

        return rootElement;
    }

    /**
     * Adds the short type name to the given parameter element
     * if the type has no '.' characters in its name
     * the type itself is taken as the shorttype
     * @param paramElement
     */
    protected void addShortType(Element paramElement,String type) {
        if (type!=null && type.indexOf('.')!=-1){
            addAttribute(paramElement.getOwnerDocument(),
                    "shorttype",
                    type.substring(type.lastIndexOf('.')+1),
                    paramElement);
        }else{
            addAttribute(paramElement.getOwnerDocument(),
                    "shorttype",
                    type==null?"":type,
                    paramElement);
        }

    }

    /**
     * Gets an element representing the operation names
     * @param doc
     * @return Returns Element.
     */
    protected Element getOpNames(Document doc) {
        Element root = doc.createElement("opnames");
        Element elt;

        for (Iterator operationsIterator = axisService.getOperations();operationsIterator.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operationsIterator.next();
            elt = doc.createElement("name");
            elt.appendChild(doc.createTextNode(axisOperation.getName().getLocalPart()));

            //what needs to be put here as the opertation namespace is actually the
            //traget namespace of the service
            addAttribute(doc,"opnsuri",axisService.getTargetNamespace(),elt);
            root.appendChild(elt);
        }

        return root;
    }

    /**
     * Gets the base64 types. If not available this will be empty!!!
     * @param doc
     * @return Returns Element.
     */
    protected Element getBase64Elements(Document doc) {
        Element root = doc.createElement("base64Elements");
        Element elt;
        QName qname;

        // this is a list of QNames
        List list = (List) codeGenConfiguration.getProperties().get(Constants.BASE_64_PROPERTY_KEY);

        if ((list != null) && !list.isEmpty()) {
            int count = list.size();

            for (int i = 0; i < count; i++) {
                qname = (QName) list.get(i);
                elt = doc.createElement("name");
                addAttribute(doc, "ns-url", qname.getNamespaceURI(), elt);
                addAttribute(doc, "localName", qname.getLocalPart(), elt);
                root.appendChild(elt);
            }
        }

        return root;
    }

    /**
     * @param objectMappings
     * @param root
     * @param doc
     */
    protected void processModelObjects(Map objectMappings, Element root, Document doc) {
        Iterator objectIterator = objectMappings.values().iterator();

        while (objectIterator.hasNext()) {
            Object o = objectIterator.next();

            if (o instanceof Document) {
                //we cannot have an empty document
                root.appendChild(doc.importNode(((Document) o).getDocumentElement(), true));
            } else {

                // oops we have no idea how to do this, if the model provided is not a DOM document
                // we are done. we might as well skip  it here
            }
        }
    }

    /**
     * we need to modify the mapper's class name list. The issue here is that in this case we do not
     * expect the fully qulified class names to be present in the class names list due to the simple
     * reason that they've not been written yet! Hence the mappers class name list needs to be updated
     * to suit the expected package to be written
     * in this case we modify the package name to have the class a inner class of the stub,
     * interface or the message receiver depending on the style
     */
    protected void updateMapperClassnames(String fullyQulifiedIncludingClassNamePrefix) {
        Map classNameMap = mapper.getAllMappedNames();
        Iterator keys = classNameMap.keySet().iterator();

        while (keys.hasNext()) {
            Object key = keys.next();
            String className = (String) classNameMap.get(key);

            //this is a generated class name - update the name
            if (!TypeTesterUtil.hasPackage(className) && !TypeTesterUtil.isPrimitive(className)){
                classNameMap.put(key, fullyQulifiedIncludingClassNamePrefix + "." + className);
                instantiatableMessageClassNames.put(key,
                        fullyQulifiedIncludingClassNamePrefix + "$" + className);
            }else{
                //this is a fully qualified class name - just leave it as it is
                classNameMap.put(key, className);
                instantiatableMessageClassNames.put(key,
                        className);
            }
        }
    }

    /**
     * Write the service XML
     *
     * @throws Exception
     */
    protected void writeServiceXml() throws Exception {

            // Write the service xml in a folder with the
            Document serviceXMLModel = createDOMDocumentForServiceXML();
            debugLogDocument("Document for service XML:", serviceXMLModel);
            ClassWriter serviceXmlWriter =
                    new ServiceXMLWriter(
                            codeGenConfiguration.isFlattenFiles() ?
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(), RESOURCE_SRC_DIR_NAME),
                            this.codeGenConfiguration.getOutputLanguage());

            writeClass(serviceXMLModel, serviceXmlWriter);

    }

    protected Document createDOMDocumentForServiceXML() {
        Document doc = getEmptyDocument();
        String serviceName = axisService.getName();
        String className = makeJavaClassName(serviceName);

        doc.appendChild(getServiceElement(serviceName, className, doc));
        return doc;

    }

    /**
     * A resusable method to return the service element for creating the
     * service xml
     *
     * @param serviceName
     * @param className
     * @param doc
     * @return
     */
    protected Node getServiceElement(String serviceName, String className, Document doc) {
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", "", rootElement);
        addAttribute(doc, "classpackage", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", className + SKELETON_CLASS_SUFFIX, rootElement);
        if (!codeGenConfiguration.isWriteTestCase()) {
            addAttribute(doc, "testOmit", "true", rootElement);
        }
        addAttribute(doc, "servicename", serviceName, rootElement);

        Iterator it = mepToClassMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();

            if (Boolean.TRUE.equals(infoHolder.get(key))) {
                Element elt = addElement(doc, "messagereceiver", className + mepToSuffixMap.get(key), rootElement);
                addAttribute(doc, "mepURI", key.toString(), elt);
            }

        }

        loadOperations(doc, rootElement, null);

        return rootElement;
    }

    protected void writeSkeleton() throws Exception {
        Document skeletonModel = createDOMDocumentForSkeleton(codeGenConfiguration.isServerSideInterface());
        debugLogDocument("Document for skeleton:", skeletonModel);
        ClassWriter skeletonWriter = new SkeletonWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), SRC_DIR_NAME)
                , this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriter);
    }

    /**
     * Write the skeletonInterface
     * @throws Exception
     */
    protected void writeSkeletonInterface() throws Exception {
        Document skeletonModel = createDOMDocumentForSkeletonInterface();
        debugLogDocument("Document for skeleton Interface:", skeletonModel);
        ClassWriter skeletonInterfaceWriter = new SkeletonInterfaceWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), SRC_DIR_NAME)
                , this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonInterfaceWriter);
    }

    /**
     * Creates the XMLModel for the skeleton
     * @param isSkeletonInterface
     * @return
     */
    protected Document createDOMDocumentForSkeleton(boolean isSkeletonInterface) {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String serviceName = makeJavaClassName(axisService.getName());
        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", serviceName + SKELETON_CLASS_SUFFIX, rootElement);
        addAttribute(doc, "callbackname", serviceName + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        if (isSkeletonInterface) {
            addAttribute(doc, "skeletonInterfaceName", serviceName + SKELETON_INTERFACE_SUFFIX,
                    rootElement);
        }
        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);
        return doc;

    }

    /**
     * Creates the XML model for the skeleton interface
     * @return
     */
    protected Document createDOMDocumentForSkeletonInterface() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String serviceName = makeJavaClassName(axisService.getName());
        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", serviceName + SKELETON_INTERFACE_SUFFIX, rootElement);
        addAttribute(doc, "callbackname", serviceName + CALL_BACK_HANDLER_SUFFIX,
                rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);
        return doc;

    }

    /**
     * Loads the operations
     * @param doc
     * @param rootElement
     * @param mep
     * @return
     */
    protected boolean loadOperations(Document doc, Element rootElement, String mep) {
        Element methodElement;
        String portTypeName = makeJavaClassName(axisService.getName());

        Iterator operations = axisService.getOperations();
        boolean opsFound = false;
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();

            // populate info holder with mep information. This will used in determining which
            // message receiver to use, etc.,

            String messageExchangePattern = axisOperation.getMessageExchangePattern();
            if (infoHolder.get(messageExchangePattern) == null) {
                infoHolder.put(messageExchangePattern, Boolean.TRUE);
            }

            if (mep == null) {

                opsFound = true;

                List soapHeaderInputParameterList = new ArrayList();
                List soapHeaderOutputParameterList = new ArrayList();

                methodElement = doc.createElement("method");

                String localPart = axisOperation.getName().getLocalPart();

                addAttribute(doc, "name", localPart, methodElement);
                addAttribute(doc, "namespace", axisOperation.getName().getNamespaceURI(), methodElement);
                String style = axisOperation.getStyle();
                addAttribute(doc, "style", style, methodElement);
                addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                        methodElement);


                addAttribute(doc, "mep", Utils.getAxisSpecifMEPConstant(axisOperation.getMessageExchangePattern()) + "", methodElement);
                addAttribute(doc, "mepURI", axisOperation.getMessageExchangePattern(), methodElement);


                addSOAPAction(doc, methodElement, axisOperation);
                addOutputAndFaultActions(doc, methodElement, axisOperation);
                //add header ops for input
                addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
                //add header ops for output
                addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

                PolicyInclude policyInclude = axisOperation.getPolicyInclude();
                Policy policy = policyInclude.getPolicy();
                if (policy != null) {
                    addAttribute(doc, "policy", PolicyUtil.getPolicyAsString(policy), methodElement);
                }

                methodElement.appendChild(getInputElement(doc, axisOperation, soapHeaderInputParameterList));
                methodElement.appendChild(getOutputElement(doc, axisOperation, soapHeaderOutputParameterList));
                methodElement.appendChild(getFaultElement(doc, axisOperation));

                rootElement.appendChild(methodElement);
            } else {
                //mep is present - we move ahead only if the given mep matches the mep of this operation

                if (mep.equals(axisOperation.getMessageExchangePattern())) {
                    //at this point we know it's true
                    opsFound = true;
                    List soapHeaderInputParameterList = new ArrayList();
                    List soapHeaderOutputParameterList = new ArrayList();
                    methodElement = doc.createElement("method");
                    String localPart = axisOperation.getName().getLocalPart();

                    addAttribute(doc, "name", localPart, methodElement);
                    addAttribute(doc, "namespace", axisOperation.getName().getNamespaceURI(), methodElement);
                    addAttribute(doc, "style", axisOperation.getStyle(), methodElement);
                    addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                            methodElement);

                    addAttribute(doc, "mep", Utils.getAxisSpecifMEPConstant(axisOperation.getMessageExchangePattern()) + "", methodElement);
                    addAttribute(doc, "mepURI", axisOperation.getMessageExchangePattern(), methodElement);


                    addSOAPAction(doc, methodElement, axisOperation);
                    addOutputAndFaultActions(doc, methodElement, axisOperation);
                    addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
                    addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

                    /*
                     * Setting the policy of the operation
                     */

                    Policy policy = axisOperation.getPolicyInclude().getPolicy();
                    if (policy != null) {
                        addAttribute(doc, "policy",
                                PolicyUtil.getPolicyAsString(policy),
                                methodElement);
                    }


                    methodElement.appendChild(getInputElement(doc,
                            axisOperation, soapHeaderInputParameterList));
                    methodElement.appendChild(getOutputElement(doc,
                            axisOperation, soapHeaderOutputParameterList));
                    methodElement.appendChild(getFaultElement(doc,
                            axisOperation));

                    rootElement.appendChild(methodElement);
                    //////////////////////
                }
            }

        }

        return opsFound;
    }

    // ==================================================================
    //                   Util Methods
    // ==================================================================

    protected Document getEmptyDocument() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param word
     * @return Returns character removed string.
     */
    protected String makeJavaClassName(String word) {
        if (JavaUtils.isJavaKeyword(word)) {
            return JavaUtils.makeNonJavaKeyword(word);
        } else {
            return JavaUtils.capitalizeFirstChar(JavaUtils.xmlNameToJava(word));
        }
    }

    /**
     * Utility method to add an attribute to a given element.
     *
     * @param document
     * @param AttribName
     * @param attribValue
     * @param element
     */
    protected void addAttribute(Document document, String AttribName, String attribValue, Element element) {
        XSLTUtils.addAttribute(document, AttribName, attribValue, element);
    }

    /**
     * @param doc
     * @param rootElement
     */
    protected void fillSyncAttributes(Document doc, Element rootElement) {
        addAttribute(doc, "isAsync", this.codeGenConfiguration.isAsyncOn()
                ? "1"
                : "0", rootElement);
        addAttribute(doc, "isSync", this.codeGenConfiguration.isSyncOn()
                ? "1"
                : "0", rootElement);
    }

    /**
     * debugging method - write the output to the debugger
     *
     * @param description
     * @param doc
     */
    protected void debugLogDocument(String description, Document doc) {
        if (log.isDebugEnabled()) {
            try {
                DOMSource source = new DOMSource(doc);
                StringWriter swrite = new StringWriter();
                swrite.write(description);
                swrite.write("\n");
                Transformer transformer =
                        TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty("omit-xml-declaration", "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, new StreamResult(swrite));

                log.debug(swrite.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the output directory for source files.
     *
     * @param outputDir
     * @return Returns File.
     */
    protected File getOutputDirectory(File outputDir, String dir2) {
        if (dir2 != null && !"".equals(dir2)) {
            outputDir = new File(outputDir, dir2);
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }


        return outputDir;
    }

    /**
     * A resusable method for the implementation of interface and implementation writing.
     *
     * @param model
     * @param writer
     * @throws java.io.IOException
     * @throws Exception
     */
    protected void writeClass(Document model, ClassWriter writer) throws IOException, Exception {
        writer.loadTemplate();

        String packageName = model.getDocumentElement().getAttribute("package");
        String className = model.getDocumentElement().getAttribute("name");

        writer.createOutFile(packageName, className);

        // use the global resolver
        writer.parse(model, resolver);
    }

    /**
     * Adds the soap action
     *
     * @param doc
     * @param rootElement
     * @param axisOperation
     */
    protected void addSOAPAction(Document doc, Element rootElement, AxisOperation axisOperation) {
        addAttribute(doc, "soapaction", axisOperation.getSoapAction(), rootElement);
    }

    /**
     * Adds the output and fault actions
     * @param doc
     * @param methodElement
     * @param operation
     */
    private void addOutputAndFaultActions(Document doc, Element methodElement, AxisOperation operation){
        String outputAction = operation.getOutputAction();
        if(outputAction != null){
            Element outputActionElt = doc.createElement(org.apache.axis2.Constants.OUTPUT_ACTION_MAPPING);
            outputActionElt.setAttribute(AddressingConstants.WSA_ACTION, outputAction);
            methodElement.appendChild(outputActionElt);
        }

        String[] faultActionNames = operation.getFaultActionNames();
        if(faultActionNames != null){
            for(int i=0;i<faultActionNames.length; i++){
                Element faultActionElt = doc.createElement(org.apache.axis2.Constants.FAULT_ACTION_MAPPING);
                faultActionElt.setAttribute(org.apache.axis2.Constants.FAULT_ACTION_NAME, faultActionNames[i]);
                faultActionElt.setAttribute(AddressingConstants.WSA_ACTION, operation.getFaultAction(faultActionNames[i]));
                methodElement.appendChild(faultActionElt);
            }
        }
    }

    /**
     * populate the header parameters
     *
     * @param soapHeaderParameterQNameList
     * @param axisOperation
     * @param input
     */
    protected void addHeaderOperations(List soapHeaderParameterQNameList, AxisOperation axisOperation,
                                       boolean input) {
        ArrayList headerparamList = new ArrayList();
        String MEP = axisOperation.getMessageExchangePattern();
        if (input) {
            if (WSDLUtil.isInputPresentForMEP(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    headerparamList = inaxisMessage.getSoapHeaders();

                }
            }
        } else {
            if (WSDLUtil.isOutputPresentForMEP(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    headerparamList = outAxisMessage.getSoapHeaders();
                }
            }
        }

        for (Iterator iterator = headerparamList.iterator(); iterator.hasNext();) {
            SOAPHeaderMessage header = (SOAPHeaderMessage) iterator.next();
            soapHeaderParameterQNameList.add(header.getElement());
        }
    }

    /**
     * Get the input element
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     * @return
     */
    protected Element getInputElement(Document doc, AxisOperation operation, List headerParameterQNameList) {
        Element inputElt = doc.createElement("input");
        String MEP = operation.getMessageExchangePattern();
        if (WSDLUtil.isInputPresentForMEP(MEP)) {
            Element[] param = getInputParamElement(doc, operation);
            for (int i = 0; i < param.length; i++) {
                inputElt.appendChild(param[i]);

            }

            List parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                inputElt.appendChild((Element) parameterElementList.get(i));
            }
        }
        return inputElt;
    }

    /**
     * Get the fault element - No header faults are supported
     * @param doc
     * @param operation
     */
    protected Element getFaultElement(Document doc, AxisOperation operation) {
        Element faultElt = doc.createElement("fault");
        Element[] param = getFaultParamElements(doc, operation);

        for (int i = 0; i < param.length; i++) {
            faultElt.appendChild(param[i]);
        }

        return faultElt;
    }

    /**
     * Finds the output element.
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     */
    protected Element getOutputElement(Document doc, AxisOperation operation, List headerParameterQNameList) {
        Element outputElt = doc.createElement("output");
        String MEP = operation.getMessageExchangePattern();
        if (WSDLUtil.isOutputPresentForMEP(MEP)) {
            Element param = getOutputParamElement(doc, operation);

            if (param != null) {
                outputElt.appendChild(param);
            }

            List outputElementList = getParameterElementList(doc, headerParameterQNameList, "header");
            for (int i = 0; i < outputElementList.size(); i++) {
                outputElt.appendChild((Element) outputElementList.get(i));
            }
        }
        return outputElt;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns the parameter element.
     */
    protected Element[] getFaultParamElements(Document doc, AxisOperation operation) {
        ArrayList params = new ArrayList();
        ArrayList faultMessages = operation.getFaultMessages();

        if (faultMessages != null && !faultMessages.isEmpty()) {
            Element paramElement;
            AxisMessage msg;
            for (int i = 0; i < faultMessages.size(); i++) {
                paramElement = doc.createElement("param");
                msg = (AxisMessage) faultMessages.get(i);

                if(msg.getElementQName() == null) {
                    throw new RuntimeException("Element QName is null for " + msg.getName() + "!");
                }

                //as for the name of a fault, we generate an exception
                addAttribute(doc, "name",
                        (String) fullyQualifiedFaultClassNameMap.get(msg.getElementQName()),
                        paramElement);
                addAttribute(doc, "intantiatiableName",
                        (String) InstantiatableFaultClassNameMap.get(msg.getElementQName()),
                        paramElement);
                addAttribute(doc, "shortName",
                        (String) faultClassNameMap.get(msg.getElementQName()),
                        paramElement);

                // attach the namespace and the localName
                addAttribute(doc, "namespace",
                        msg.getElementQName().getNamespaceURI(),
                        paramElement);
                addAttribute(doc, "localname",
                        msg.getElementQName().getLocalPart(),
                        paramElement);
                //the type represents the type that will be wrapped by this
                //name
                String typeMapping =
                        this.mapper.getTypeMappingName(msg.getElementQName());
                addAttribute(doc, "type", (typeMapping == null)
                        ? ""
                        : typeMapping, paramElement);

                //add the short name
                addShortType(paramElement,typeMapping);

                String attribValue = (String) instantiatableMessageClassNames.
                        get(msg.getElementQName());
                addAttribute(doc, "instantiatableType",
                        attribValue == null ? "" : attribValue,
                        paramElement);

                // add an extra attribute to say whether the type mapping is
                // the default
                if (mapper.getDefaultMappingName().equals(typeMapping)) {
                    addAttribute(doc, "default", "yes", paramElement);
                }
                addAttribute(doc, "value", getParamInitializer(typeMapping),
                        paramElement);

                Iterator iter = msg.getExtensibilityAttributes().iterator();
                while (iter.hasNext()) {
                    // process extensibility attributes
                }
                params.add(paramElement);
            }

            return (Element[]) params.toArray(new Element[params.size()]);
        } else {
            return new Element[]{};//return empty array
        }


    }


    /**
     * @param doc
     * @param operation
     * @return Returns the parameter element.
     */
    protected Element[] getInputParamElement(Document doc, AxisOperation operation) {

        AxisMessage inputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        List paramElementList  = new ArrayList();
        if (inputMessage != null) {

            // This is the  wrapped component - add the type mapping
            Element mainParameter = generateParamComponent(doc,
                    this.mapper.getParameterName(
                            inputMessage.getElementQName()),
                    this.mapper.getTypeMappingName(
                            inputMessage.getElementQName()),
                    operation.getName()
            );

            paramElementList.add(mainParameter);

            // this message has been unwrapped - find the correct references of the
            // the message by looking at the unwrapped details object and attach the
            // needed parameters inside main parameter element
            if (inputMessage.getParameter(Constants.UNWRAPPED_KEY) != null) {
                //we have this unwrapped earlier. get the info holder
                //and then look at the parameters
                Parameter detailsParameter =
                        inputMessage.getParameter(Constants.UNWRAPPED_DETAILS);
                MessagePartInformationHolder infoHolder =
                        (MessagePartInformationHolder)detailsParameter.getValue();
                List partsList = infoHolder.getPartsList();

                //populate the parts list - this list is needed to generate multiple
                //parameters in the signatures
                for (int i = 0; i < partsList.size(); i++) {
                    QName qName = (QName) partsList.get(i);
                    mainParameter.appendChild(generateParamComponent(doc,
                            this.mapper.getParameterName(
                                    qName),
                            this.mapper.getTypeMappingName(
                                    qName),
                            operation.getName(),
                            qName.getLocalPart(),
                            (this.mapper.getTypeMappingStatus(qName)!=null))
                    );
                }

                // apart from the parts list we need to get the wrapping classname
                // as well

            }




        }

        return (Element[])paramElementList.toArray(
                new Element[paramElementList.size()]);
    }

    /**
     * A convenient method for the generating the parameter
     * element
     * @param doc
     * @param paramName
     * @param paramType
     * @param
     * @return
     */
    protected Element generateParamComponent(Document doc,
                                             String paramName,
                                             String paramType,
                                             QName operationName) {
        return generateParamComponent(doc,paramName,paramType,operationName,null,false);

    }

    /**
     * A convenient method for the generating the parameter
     * element
     * @param doc
     * @param paramName
     * @param paramType
     * @return
     */
    protected Element generateParamComponent(Document doc,
                                             String paramName,
                                             String paramType) {
        return generateParamComponent(doc,paramName,paramType,null,null,false);

    }
    /**
     * A convenient method for the generating the parameter
     * element
     *
     * @param doc
     * @param paramElement
     * @param paramName
     * @param paramType
     * @param opName
     * @param paramName
     */
    protected Element generateParamComponent(Document doc,
                                             String paramName,
                                             String paramType,
                                             QName opName,
                                             String partName,
                                             boolean isPrimitive) {
        Element paramElement = doc.createElement("param");
        addAttribute(doc, "name",
                paramName, paramElement);

        addAttribute(doc, "type", (paramType == null)
                ? ""
                : paramType, paramElement);

        //adds the short type
        addShortType(paramElement,paramType);

        // add an extra attribute to say whether the type mapping is the default
        if (mapper.getDefaultMappingName().equals(paramType)) {
            addAttribute(doc, "default", "yes", paramElement);
        }
        addAttribute(doc, "value", getParamInitializer(paramType), paramElement);
        // add this as a body parameter
        addAttribute(doc, "location", "body", paramElement);

        //if the opName and partName are present , add them
        if (opName!=null){
            addAttribute(doc,"opname",opName.getLocalPart(),paramElement);

        }
        if (partName!= null){
            addAttribute(doc,"partname",
                    JavaUtils.capitalizeFirstChar(partName),
                    paramElement);
        }

        if (isPrimitive){
            addAttribute(doc,"primitive","yes",paramElement);
        }

        return paramElement;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns Element.
     */
    protected Element getOutputParamElement(Document doc, AxisOperation operation) {
        Element paramElement = doc.createElement("param");
        AxisMessage outputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        String typeMappingStr;
        String parameterName;

        if (outputMessage != null) {
            parameterName = this.mapper.getParameterName(outputMessage.getElementQName());
            String typeMapping = this.mapper.getTypeMappingName(outputMessage.getElementQName());
            typeMappingStr = (typeMapping == null)
                    ? ""
                    : typeMapping;
        } else {
            parameterName = "";
            typeMappingStr = "";
        }

        addAttribute(doc, "name", parameterName, paramElement);
        addAttribute(doc, "type", typeMappingStr, paramElement);

        //adds the short type
        addShortType(paramElement,typeMappingStr);


        // add an extra attribute to say whether the type mapping is the default
        if (mapper.getDefaultMappingName().equals(typeMappingStr)) {
            addAttribute(doc, "default", "yes", paramElement);
        }

        // add this as a body parameter
        addAttribute(doc, "location", "body", paramElement);
        addAttribute(doc, "opname", operation.getName().getLocalPart(), paramElement);

        return paramElement;
    }

    /**
     * @param paramType
     */
    protected String getParamInitializer(String paramType) {

        // Look up paramType in the table
        String out = (String) constructorMap.get(paramType);

        if (out == null) {
            out = "null";
        }

        return out;
    }

    /**
     * @param doc
     * @param parameters
     * @param location
     */
    protected List getParameterElementList(Document doc, List parameters, String location) {
        List parameterElementList = new ArrayList();

        if ((parameters != null) && !parameters.isEmpty()) {
            int count = parameters.size();

            for (int i = 0; i < count; i++) {
                Element param = doc.createElement("param");
                QName name = (QName) parameters.get(i);

                addAttribute(doc, "name", this.mapper.getParameterName(name), param);

                String typeMapping = this.mapper.getTypeMappingName(name);
                String typeMappingStr = (typeMapping == null)
                        ? ""
                        : typeMapping;

                addAttribute(doc, "type", typeMappingStr, param);
                addAttribute(doc, "location", location, param);
                parameterElementList.add(param);
            }
        }

        return parameterElementList;
    }

    /**
     * Utility method to add an attribute to a given element.
     *
     * @param document
     * @param eltName
     * @param eltValue
     * @param element
     */
    protected Element addElement(Document document, String eltName, String eltValue, Element element) {
        Element elt = XSLTUtils.addChildElement(document, eltName, element);
        if (eltValue!=null){
            elt.appendChild(document.createTextNode(eltValue));
        }
        return elt;
    }


}
