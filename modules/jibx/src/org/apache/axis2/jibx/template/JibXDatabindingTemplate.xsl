
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>
<!-- #################################################################################  -->
    <!-- ############################   JiBX template   ##############################  -->
  <xsl:template match="databinders[@dbtype='jibx']">
    <xsl:param name="context">unknown</xsl:param>
    
    <xsl:apply-templates select="initialize-binding"/>

    <!-- wrapped='true' uses original code, wrapped='false' unwraps method calls -->
    <xsl:variable name="wrapped"><xsl:value-of select="@wrapped"/></xsl:variable>
    <xsl:if test="$wrapped='true'">
      
      <!-- MTOM not yet supported by JiBX, but array may be needed -->
      <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
      <xsl:if test="$base64">
        private static javax.xml.namespace.QName[] qNameArray = {
        <xsl:for-each select="base64Elements/name">
          <xsl:if test="position()">1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
        </xsl:for-each>
        };
      </xsl:if>
  
      <xsl:variable name="firstType"><xsl:value-of select="param[1]/@type"/></xsl:variable>
  
      <xsl:for-each select="param[not(@type = preceding-sibling::param/@type)]">
        <xsl:if test="@type!=''">
  
            private org.apache.axiom.om.OMElement toOM(<xsl:value-of select="@type"/> param, org.apache.axiom.soap.SOAPFactory factory, boolean optimizeContent) {
                if (param instanceof org.jibx.runtime.IMarshallable){
                    if (bindingFactory == null) {
                        throw new RuntimeException("Could not find JiBX binding information for <xsl:value-of select="$firstType"/>, JiBX binding unusable");
                    }
                    org.jibx.runtime.IMarshallable marshallable =
                        (org.jibx.runtime.IMarshallable)param;
                    int index = marshallable.JiBX_getIndex();
                    org.apache.axis2.jibx.JiBXDataSource source =
                        new org.apache.axis2.jibx.JiBXDataSource(marshallable, bindingFactory);
                    org.apache.axiom.om.OMNamespace namespace = factory.createOMNamespace(bindingFactory.getElementNamespaces()[index], null);
                    return factory.createOMElement(source, bindingFactory.getElementNames()[index], namespace);
                } else {
                    throw new RuntimeException("No JiBX &lt;mapping> defined for class <xsl:value-of select="@type"/>");
                }
            }

            private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param, boolean optimizeContent) {
                org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                if (param != null){
                    envelope.getBody().addChild(toOM(param, factory, optimizeContent));
                }
                return envelope;
            }

        </xsl:if>
      </xsl:for-each>

        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory) {
            return factory.getDefaultEnvelope();
        }

        private java.lang.Object fromOM(
            org.apache.axiom.om.OMElement param,
            java.lang.Class type,
            java.util.Map extraNamespaces) {
            try {
                if (bindingFactory == null) {
                    throw new RuntimeException("Could not find JiBX binding information for com.sosnoski.seismic.jibxsoap.Query, JiBX binding unusable");
                }
                org.jibx.runtime.impl.UnmarshallingContext ctx =
                    (org.jibx.runtime.impl.UnmarshallingContext)bindingFactory.createUnmarshallingContext();
                org.jibx.runtime.IXMLReader reader = new org.jibx.runtime.impl.StAXReaderWrapper(param.getXMLStreamReaderWithoutCaching(), "SOAP-message", true);
                ctx.setDocument(reader);
                return ctx.unmarshalElement(type);
            } catch (Exception e) {
                 throw new RuntimeException(e);
            }
        }
      
    </xsl:if>
    
    <xsl:if test="$wrapped='false'">
      <xsl:choose>
        <xsl:when test="$context='message-receiver'">
          <xsl:apply-templates mode="message-receiver" select="dbmethod"/>
        </xsl:when>
        <xsl:when test="$context='interface-implementation'">
          <xsl:apply-templates mode="interface-implementation" select="dbmethod"/>
        </xsl:when>
      </xsl:choose>
    </xsl:if>

  </xsl:template>
  
  
  <!--
  MESSAGE RECEIVER METHOD GENERATION
  -->
  
  <!-- Invoked by main template to handle unwrapped method generation for message receiver -->
  <xsl:template match="dbmethod" mode="message-receiver">
      public org.apache.axiom.soap.SOAPEnvelope <xsl:value-of select="@receiver-name"/>(org.apache.axiom.om.OMElement element, <xsl:value-of select="/*/@skeletonname"/> skel, org.apache.axiom.soap.SOAPFactory factory) throws org.apache.axis2.AxisFault {
          org.apache.axiom.soap.SOAPEnvelope envelope = null;
          try {
              org.jibx.runtime.impl.UnmarshallingContext uctx = getNewUnmarshalContext(element);
              uctx.next();
              int index;
    <xsl:apply-templates select="in-wrapper/parameter-element" mode="message-receiver"/>
    
    <!-- actual call handling depends on type of returned result -->
    <xsl:choose>
    
      <!-- returning an array of values -->
      <xsl:when test="out-wrapper/@empty='false' and out-wrapper/return-element/@array='true'">
              envelope = factory.getDefaultEnvelope();
              org.apache.axiom.om.OMElement wrapper = factory.createOMElement("<xsl:value-of select='out-wrapper/@name'/>", "<xsl:value-of select='out-wrapper/@ns'/>", "");
              envelope.getBody().addChild(wrapper);
              <xsl:value-of select="out-wrapper/return-element/@java-type"/>[] results = skel.<xsl:call-template name="call-arg-list"/>;
              if (results == null || results.length == 0) {
        <xsl:choose>
          <xsl:when test="out-wrapper/return-element/@optional='true'"/>
          <xsl:otherwise>
                  throw new org.apache.axis2.AxisFault("Missing required result");
          </xsl:otherwise>
        </xsl:choose>
              } else {
                  org.apache.axiom.om.OMNamespace appns = factory.createOMNamespace("<xsl:value-of select='out-wrapper/return-element/@ns'/>", "app");
                  wrapper.declareNamespace(appns);
        <xsl:choose>
          <xsl:when test="out-wrapper/return-element/@form='complex'">
                  for (int i = 0; i &lt; results.length; i++) {
                      <xsl:value-of select="out-wrapper/return-element/@java-type"/> result = results[i];
                      if (result == null) {
            <xsl:choose>
              <xsl:when test="out-wrapper/return-element/@nillable='true'">
                          org.apache.axiom.om.OMElement child = factory.createOMElement("<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
                          org.apache.axiom.om.OMNamespace xsins = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
                          child.declareNamespace(xsins);
                          child.addAttribute("nil", "true", xsins);
                          wrapper.addChild(child);
              </xsl:when>
              <xsl:otherwise>
                          throw new org.apache.axis2.AxisFault("Null value in result array not allowed unless element has nillable='true'");
              </xsl:otherwise>
            </xsl:choose>
                      } else {
                          org.apache.axiom.om.OMDataSource src = new org.apache.axis2.jibx.JiBXDataSource(result, _type_index<xsl:value-of select="out-wrapper/return-element/@type-index"/>, bindingFactory);
                          org.apache.axiom.om.OMElement child = factory.createOMElement(src, "<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
                          wrapper.addChild(child);
                      }
                  }
          </xsl:when>
          <xsl:otherwise>
                  for (int i = 0; i &lt; results.length; i++) {
                      <xsl:value-of select="out-wrapper/return-element/@java-type"/> result = results[i];
                      org.apache.axiom.om.OMElement child = factory.createOMElement("<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
            <xsl:choose>
              <xsl:when test="out-wrapper/return-element/@serializer=''">
                      child.setText(result.toString());
              </xsl:when>
              <xsl:otherwise>
                      child.setText(<xsl:value-of select="out-wrapper/return-element/@serializer"/>(result);
              </xsl:otherwise>
            </xsl:choose>
                      wrapper.addChild(child);
                  }
          </xsl:otherwise>
        </xsl:choose>
              }
      </xsl:when>
    
      <!-- returning a single value -->
      <xsl:when test="out-wrapper/@empty='false'">
              envelope = factory.getDefaultEnvelope();
              org.apache.axiom.om.OMElement wrapper = factory.createOMElement("<xsl:value-of select='out-wrapper/@name'/>", "<xsl:value-of select='out-wrapper/@ns'/>", "");
              envelope.getBody().addChild(wrapper);
              <xsl:value-of select="out-wrapper/return-element/@java-type"/> result = skel.<xsl:call-template name="call-arg-list"/>;
              org.apache.axiom.om.OMNamespace appns = factory.createOMNamespace("<xsl:value-of select='out-wrapper/return-element/@ns'/>", "app");
              wrapper.declareNamespace(appns);
        <xsl:choose>
          <xsl:when test="out-wrapper/return-element/@form='complex'">
              if (result == null) {
            <xsl:choose>
              <xsl:when test="out-wrapper/return-element/@optional='true'"/>
              <xsl:when test="out-wrapper/return-element/@nillable='true'">
                  org.apache.axiom.om.OMElement child = factory.createOMElement("<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
                  org.apache.axiom.om.OMNamespace xsins = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
                  child.declareNamespace(xsins);
                  child.addAttribute("nil", "true", xsins);
                  wrapper.addChild(child);
              </xsl:when>
              <xsl:otherwise>
                  throw new org.apache.axis2.AxisFault("Missing required result");
              </xsl:otherwise>
            </xsl:choose>
              } else {
                  org.apache.axiom.om.OMDataSource src = new org.apache.axis2.jibx.JiBXDataSource(result, _type_index<xsl:value-of select="out-wrapper/return-element/@type-index"/>, bindingFactory);
                  org.apache.axiom.om.OMElement child = factory.createOMElement(src, "<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
                  wrapper.addChild(child);
              }
          </xsl:when>
          <xsl:otherwise>
              org.apache.axiom.om.OMElement child = factory.createOMElement("<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
            <xsl:choose>
              <xsl:when test="out-wrapper/return-element/@serializer=''">
              child.setText(result.toString());
              </xsl:when>
              <xsl:otherwise>
              child.setText(<xsl:value-of select="out-wrapper/return-element/@serializer"/>(result));
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    
      <!-- not returning anything -->
      <xsl:otherwise>
              skel.<xsl:call-template name="call-arg-list"/>;
        <xsl:if test="count(out-wrapper)&gt;0">
              envelope = factory.getDefaultEnvelope();
              envelope.getBody().addChild(factory.createOMElement("<xsl:value-of select='out-wrapper/@name'/>", "<xsl:value-of select='out-wrapper/@ns'/>", ""););
        </xsl:if>
      </xsl:otherwise>
      
    </xsl:choose>
          } catch (org.jibx.runtime.JiBXException e) {
              throw new org.apache.axis2.AxisFault(e);
          }
          return envelope;
      }
  </xsl:template>
  
  <!-- Generate argument list for message receiver call to actual implementation method. -->
  <xsl:template name="call-arg-list">
    <xsl:value-of select="@method-name"/>(
    <xsl:for-each select="in-wrapper/parameter-element">
      <xsl:if test="position()&gt;1">, </xsl:if><xsl:value-of select="@java-name"/>
    </xsl:for-each>
    )
  </xsl:template>
  
  <!-- Generate code for a particular parameter element in a message receiver method -->
  <xsl:template match="parameter-element" mode="message-receiver">
    <xsl:choose>
      <xsl:when test="@array='true'">
        <xsl:call-template name="unmarshal-array"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="unmarshal-value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <!-- 
  CLIENT STUB UNWRAPPED METHOD GENERATION
  -->
  
  <!-- Invoked by main template to handle unwrapped method generation for client stub -->
  <xsl:template match="dbmethod" mode="interface-implementation">
    <xsl:variable name="return-base-type"><xsl:value-of select="out-wrapper/return-element/@java-type"/></xsl:variable>
    <xsl:variable name="return-full-type"><xsl:value-of select="$return-base-type"/><xsl:if test="out-wrapper/return-element/@array='true'">[]</xsl:if></xsl:variable>
    <xsl:variable name="method-name"><xsl:value-of select="@method-name"/></xsl:variable>
    
        /**
         * Auto generated method signature
         * 
         * @see com.sosnoski.ws.library.adb.LibraryAdb#getBook
         * @param param18
         * 
         */
        public <xsl:value-of select="$return-full-type"/><xsl:text> </xsl:text><xsl:value-of select="@method-name"/>(
    <xsl:for-each select="in-wrapper/parameter-element">
      <xsl:if test="position()&gt;1">, </xsl:if><xsl:value-of select="@java-type"/><xsl:if test="@array='true'">[]</xsl:if><xsl:text> </xsl:text><xsl:value-of select="@java-name"/>
    </xsl:for-each>
            ) throws java.rmi.RemoteException {
    <!-- Simple parameter values (those with serializers) can be handled by
      direct conversion to elements. Complex parameter values need to use data
      sources. This code handles both types. -->
            try {
                javax.xml.namespace.QName opname = _operations[<xsl:apply-templates mode="get-index" select="/class/method[@name=$method-name]"></xsl:apply-templates>].getName();
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(opname);
                _operationClient.getOptions().setAction("<xsl:apply-templates mode="get-action" select="/class/method[@name=$method-name]"></xsl:apply-templates>");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
    
                // create SOAP envelope with the payload
                org.apache.axiom.soap.SOAPEnvelope env = createEnvelope(_operationClient.getOptions());
                org.apache.axiom.soap.SOAPFactory factory = getFactory(_operationClient.getOptions().getSoapVersionURI());
                org.apache.axiom.om.OMElement wrapper = factory.createOMElement("<xsl:value-of select='in-wrapper/@name'/>", "<xsl:value-of select='in-wrapper/@ns'/>", "");
                env.getBody().addChild(wrapper);
                org.apache.axiom.om.OMElement child;
    <xsl:apply-templates select="in-wrapper/parameter-element" mode="interface-implementation"/>
    
                // adding SOAP headers
                _serviceClient.addHeadersToEnvelope(env);
                // create message context with that soap envelope
                org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
                _messageContext.setEnvelope(env);
    
                // add the message contxt to the operation client
                _operationClient.addMessageContext(_messageContext);
    
                // execute the operation client
                _operationClient.execute(true);
                
    <xsl:if test="out-wrapper/@empty='false'">
                org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
                    .getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.om.OMElement result = _returnMessageContext.getEnvelope().getFirstElement();
                if (result != null &amp;&amp; "<xsl:value-of select='out-wrapper/@name'/>".equals(result.getLocalName()) &amp;&amp;
                    "<xsl:value-of select='out-wrapper/@ns'/>".equals(result.getNamespace().getNamespaceURI())) {
                    org.jibx.runtime.impl.UnmarshallingContext uctx = getNewUnmarshalContext(result);
                    uctx.next();
                    int index;
      <xsl:apply-templates select="out-wrapper/return-element" mode="interface-implementation"/>
                    return <xsl:value-of select="out-wrapper/return-element/@java-name"/>;
                } else {
                    throw new org.apache.axis2.AxisFault("Missing expected result wrapper element {<xsl:value-of select='out-wrapper/@ns'/>}<xsl:value-of select='out-wrapper/@name'/>");
                }
    </xsl:if>
            } catch (org.jibx.runtime.JiBXException e) {
                throw new org.apache.axis2.AxisFault(e);
            } catch (org.apache.axis2.AxisFault f) {
                org.apache.axiom.om.OMElement faultElt = f.getDetail();
                if (faultElt != null) {
                    if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
                        // make the fault by reflection
                        try {
                            java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap
                            .get(faultElt.getQName());
                            java.lang.Class exceptionClass = java.lang.Class
                            .forName(exceptionClassName);
                            java.rmi.RemoteException ex = (java.rmi.RemoteException)exceptionClass
                            .newInstance();
                            // message class
                            java.lang.String messageClassName = (java.lang.String)faultMessageMap
                            .get(faultElt.getQName());
                            java.lang.Class messageClass = java.lang.Class
                            .forName(messageClassName);
                            java.lang.Object messageObject = null;
                            java.lang.reflect.Method m = exceptionClass.getMethod(
                                "setFaultMessage",
                                new java.lang.Class[] { messageClass });
                            m.invoke(ex, new java.lang.Object[] { messageObject });
    
                            throw ex;
                        } catch (java.lang.ClassCastException e) {
                            // we cannot intantiate the class - throw the original
                            // Axis fault
                            throw f;
                        } catch (java.lang.ClassNotFoundException e) {
                            // we cannot intantiate the class - throw the original
                            // Axis fault
                            throw f;
                        } catch (java.lang.NoSuchMethodException e) {
                            // we cannot intantiate the class - throw the original
                            // Axis fault
                            throw f;
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            // we cannot intantiate the class - throw the original
                            // Axis fault
                            throw f;
                        } catch (java.lang.IllegalAccessException e) {
                            // we cannot intantiate the class - throw the original
                            // Axis fault
                            throw f;
                        } catch (java.lang.InstantiationException e) {
                            // we cannot intantiate the class - throw the original
                            // Axis fault
                            throw f;
                        }
                    } else {
                        throw f;
                    }
                } else {
                    throw f;
                }
            }
        }
  </xsl:template>
  
  <!-- Invoked to get the operation index number for a method. -->
  <xsl:template match="method" mode="get-index"><xsl:value-of select="position()-1"/></xsl:template>
  
  <!-- Invoked to get the operation action for a method. -->
  <xsl:template match="method" mode="get-action"><xsl:value-of select="@action"/></xsl:template>
  
  <!-- Generate code for a particular parameter element in a client stub method -->
  <xsl:template match="parameter-element" mode="interface-implementation">
    <xsl:choose>
      <xsl:when test="@array='true'">
        <xsl:call-template name="marshal-array"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="marshal-value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Marshal an array to a repeated element -->
  <xsl:template name="marshal-array">
    if (<xsl:value-of select="@java-name"/> == null || <xsl:value-of select="@java-name"/>.length == 0) {
    <xsl:choose>
      <xsl:when test="@optional='true'"></xsl:when>
      <xsl:when test="@nillable='true'">
        child = factory.createOMElement("<xsl:value-of select='@ns'/>", "<xsl:value-of select='@name'/>", "");
        org.apache.axiom.om.OMNamespace xsins = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        child.declareNamespace(xsins);
        child.addAttribute("nil", "true", xsins);
        wrapper.addChild(child);
      </xsl:when>
      <xsl:otherwise>
        throw new org.apache.axis2.AxisFault("Missing required value <xsl:value-of select='@java-name'/>");
      </xsl:otherwise>
    </xsl:choose>
    } else {
        for (int i = 0; i &lt; <xsl:value-of select="@java-name"/>.length; i++) {
            <xsl:value-of select="@java-type"/> _item = <xsl:value-of select="@java-name"/>[i];
    <xsl:choose>
      <xsl:when test="@object='true' and @nillable='true'">
            if (_item == null) {
                child = factory.createOMElement("<xsl:value-of select='@ns'/>", "<xsl:value-of select='@name'/>", "");
                org.apache.axiom.om.OMNamespace xsins = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
                child.declareNamespace(xsins);
                child.addAttribute("nil", "true", xsins);
                wrapper.addChild(child);
            } else {
        <xsl:call-template name="serialize-value-to-child"/>
            }
      </xsl:when>
      <xsl:when test="@object='true'">
            if (_item == null) {
                throw new org.apache.axis2.AxisFault("Null value in array <xsl:value-of select='@java-name'/>");
            } else {
        <xsl:call-template name="serialize-value-to-child"/>
            }
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="serialize-value-to-child"/>
      </xsl:otherwise>
    </xsl:choose>
        }
    }
  </xsl:template>
  
  <!-- Marshal a simple value to a non-repeated element -->
  <xsl:template name="marshal-value">
    <xsl:choose>
      <xsl:when test="@object='true' and @nillable='true'">
        if (<xsl:value-of select="@java-name"/> == null) {
            child = factory.createOMElement("<xsl:value-of select='@ns'/>", "<xsl:value-of select='@name'/>", "");
            org.apache.axiom.om.OMNamespace xsins = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            child.declareNamespace(xsins);
            child.addAttribute("nil", "true", xsins);
            wrapper.addChild(child);
        } else {
        <xsl:call-template name="serialize-value-to-child"/>
        }
      </xsl:when>
      <xsl:when test="@object='true'">
        if (<xsl:value-of select="@java-name"/> == null) {
            throw new org.apache.axis2.AxisFault("Null value for <xsl:value-of select='@java-name'/>");
        } else {
        <xsl:call-template name="serialize-value-to-child"/>
        }
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="serialize-value-to-child"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Convert the current value to an element. -->
  <xsl:template name="serialize-value-to-child">
    <xsl:choose>
      <xsl:when test="@java-type='java.lang.String' and @serializer=''">
        child = factory.createOMElement("<xsl:value-of select='@ns'/>", "<xsl:value-of select='@name'/>", "");
        child.setText(<xsl:call-template name="parameter-or-array-item"/>);
      </xsl:when>
      <xsl:when test="@form='simple' and @serializer=''">
        child = factory.createOMElement("<xsl:value-of select='@ns'/>", "<xsl:value-of select='@name'/>", "");
        child.setText(<xsl:call-template name="parameter-or-array-item"/>.toString());
      </xsl:when>
      <xsl:when test="@form='simple'">
        child = factory.createOMElement("<xsl:value-of select='@ns'/>", "<xsl:value-of select='@name'/>", "");
        child.setText(<xsl:value-of select="@serializer"/>(<xsl:call-template name="parameter-or-array-item"/>));
      </xsl:when>
      <xsl:when test="@form='complex'">
        org.apache.axiom.om.OMDataSource src = new org.apache.axis2.jibx.JiBXDataSource(<xsl:call-template name="parameter-or-array-item"/>, _type_index<xsl:value-of select="@type-index"/>, bindingFactory);
        org.apache.axiom.om.OMNamespace appns = factory.createOMNamespace("<xsl:value-of select='@ns'/>", "");
        child.declareNamespace(appns);
        child = factory.createOMElement(src, "<xsl:value-of select='@name'/>", appns);
      </xsl:when>
    </xsl:choose>
        wrapper.addChild(child);
  </xsl:template>
  
  <!-- Reference to parameter or array item value, as appropriate -->
  <xsl:template name="parameter-or-array-item"><xsl:choose><xsl:when test="@array='true'">_item</xsl:when><xsl:otherwise><xsl:value-of select='@java-name'/></xsl:otherwise></xsl:choose></xsl:template>
  
  <!-- Generate code for the result in a client stub method -->
  <xsl:template match="return-element" mode="interface-implementation">
    <xsl:choose>
      <xsl:when test="@array='true'">
        <xsl:call-template name="unmarshal-array"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="unmarshal-value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <!--
  STATIC CODE GENERATION
  -->
  
  <!-- Called by main template to handle static data structures. -->
  <xsl:template match="initialize-binding">
      private static final org.jibx.runtime.IBindingFactory bindingFactory;
      private static final String bindingErrorMessage;
    <xsl:apply-templates mode="generate-index-fields" select="abstract-type"/>
      static {
          org.jibx.runtime.IBindingFactory factory = null;
          String message = null;
          try {
              factory = org.jibx.runtime.BindingDirectory.getFactory(<xsl:value-of select="@bound-class"/>.class);
              message = null;
          } catch (Exception e) { message = e.getMessage(); }
          bindingFactory = factory;
          bindingErrorMessage = message;
    <xsl:apply-templates mode="set-index-fields" select="abstract-type"/>
      };
      
      private static org.jibx.runtime.impl.UnmarshallingContext getNewUnmarshalContext(org.apache.axiom.om.OMElement param)
          throws org.jibx.runtime.JiBXException {
          if (bindingFactory == null) {
              throw new RuntimeException(bindingErrorMessage);
          }
          org.jibx.runtime.impl.UnmarshallingContext ctx =
              (org.jibx.runtime.impl.UnmarshallingContext)bindingFactory.createUnmarshallingContext();
          org.jibx.runtime.IXMLReader reader = new org.jibx.runtime.impl.StAXReaderWrapper(param.getXMLStreamReaderWithoutCaching(), "SOAP-message", true);
          ctx.setDocument(reader);
          return ctx;
      }
  </xsl:template>
  
  <!-- Called by "initialize-binding" template to generate mapped class index fields. -->
  <xsl:template match="abstract-type" mode="generate-index-fields">
          private static final int _type_index<xsl:value-of select="@type-index"/>;
  </xsl:template>
    
  <!-- Called by "initialize-binding" template to initialize mapped class index fields. -->
  <xsl:template match="abstract-type" mode="set-index-fields">
         _type_index<xsl:value-of select="@type-index"/> = (bindingFactory == null) ?
            -1 : bindingFactory.getTypeIndex("{<xsl:value-of select="@ns"/>}<xsl:value-of select="@name"/>");
  </xsl:template>
  
  
  <!--
  SHARED TEMPLATES
  -->
  
  <!-- Unmarshal a repeated element into an array -->
  <xsl:template name="unmarshal-array">
    <xsl:value-of select="@java-type"/>[] <xsl:value-of select="@java-name"/> = new <xsl:value-of select="@java-type"/>[4];
      index = 0;
      while (uctx.isAt("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>")) {
          if (index >= <xsl:value-of select="@java-name"/>.length) {
              <xsl:value-of select="@java-name"/> = (<xsl:value-of select="@java-type"/>[])org.jibx.runtime.Utility.growArray(<xsl:value-of select="@java-name"/>);
          }
    <xsl:if test="@nillable='true'">
          if (uctx.attributeBoolean("http://www.w3.org/2001/XMLSchema-instance", "nil", false)) {
              uctx.skipElement();
          } else {
    </xsl:if>
    <xsl:value-of select="@java-name"/>[index++] = (<xsl:value-of select="@java-type"/>)<xsl:call-template name="deserialize-element-value"/>;
    <xsl:if test="@nillable='true'">
          }
    </xsl:if>
      }
      <xsl:value-of select="@java-name"/> = (<xsl:value-of select="@java-type"/>[])org.jibx.runtime.Utility.resizeArray(index, <xsl:value-of select="@java-name"/>);
    <xsl:if test="@optional!='true'">
      if (index == 0) {
          throw new org.apache.axis2.AxisFault("Missing required element {<xsl:value-of select='@ns'/>}<xsl:value-of select='@name'/>");
      }
    </xsl:if>
  </xsl:template>
  
  <!-- Unmarshal a non-repeated element into an simple value -->
  <xsl:template name="unmarshal-value">
    <xsl:value-of select="@java-type"/><xsl:text> </xsl:text><xsl:value-of select="@java-name"/> = <xsl:choose><xsl:when test="boolean(@default)"><xsl:value-of select="@default"/></xsl:when><xsl:otherwise>null</xsl:otherwise></xsl:choose>;
          if (uctx.isAt("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>")) {
    <xsl:if test="@nillable='true'">
              if (uctx.attributeBoolean("http://www.w3.org/2001/XMLSchema-instance", "nil", false)) {
                  uctx.skipElement();
              } else {
    </xsl:if>
    <xsl:value-of select="@java-name"/> = (<xsl:value-of select="@java-type"/>)<xsl:call-template name="deserialize-element-value"/>;
    <xsl:if test="@nillable='true'">
              }
    </xsl:if>
    <xsl:choose>
      <xsl:when test="optional">
          }
      </xsl:when>
      <xsl:otherwise>
          } else {
              throw new org.apache.axis2.AxisFault("Missing required element {<xsl:value-of select='@ns'/>}<xsl:value-of select='@name'/>");
          }
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Convert the current element into a value. -->
  <xsl:template name="deserialize-element-value">
    <xsl:choose>
      <xsl:when test="@java-type='java.lang.String' and @deserializer=''">
        uctx.parseElementText("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>")
      </xsl:when>
      <xsl:when test="@form='simple' and @deserializer=''">
        new <xsl:value-of select="@java-type"/>(uctx.parseElementText("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>"))
      </xsl:when>
      <xsl:when test="@form='simple'">
        <xsl:value-of select="@deserializer"/>(uctx.parseElementText("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>"))
      </xsl:when>
      <xsl:when test="@form='complex'">
        uctx.getUnmarshaller(_type_index<xsl:value-of select="@type-index"/>).unmarshal(null, uctx)
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>