//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-b52-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.03.21 at 10:56:51 AM CDT 
//


package org.apache.axis2.jaxws.description.xml.handler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The resource-refType contains a declaration of a Deployment Component's reference to an external
 * resource. It consists of an optional description, the resource manager connection factory
 * reference name, an optional indication of the resource manager connection factory type expected
 * by the Deployment Component code, an optional type of authentication (Application or Container),
 * and an optional specification of the shareability of connections obtained from the resource
 * (Shareable or Unshareable).
 * <p/>
 * It also includes optional elements to define injection of the named resource into fields or
 * JavaBeans properties.
 * <p/>
 * The connection factory type must be supplied unless an injection target is specified, in which
 * case the type of the target is used.  If both are specified, the type must be assignment
 * compatible with the type of the injection target.
 * <p/>
 * Example:
 * <p/>
 * <resource-ref> <res-ref-name>jdbc/EmployeeAppDB</res-ref-name> <res-type>javax.sql.DataSource</res-type>
 * <res-auth>Container</res-auth> <res-sharing-scope>Shareable</res-sharing-scope> </resource-ref>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p>Java class for resource-refType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="resource-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="res-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/>
 *         &lt;element name="res-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"
 * minOccurs="0"/>
 *         &lt;element name="res-auth" type="{http://java.sun.com/xml/ns/javaee}res-authType"
 * minOccurs="0"/>
 *         &lt;element name="res-sharing-scope" type="{http://java.sun.com/xml/ns/javaee}res-sharing-scopeType"
 * minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-refType", propOrder = {
        "description",
        "resRefName",
        "resType",
        "resAuth",
        "resSharingScope",
        "mappedName",
        "injectionTarget"
        })
public class ResourceRefType {

    @XmlElement(namespace = "http://java.sun.com/xml/ns/javaee", required = true)
    protected List<DescriptionType> description;
    @XmlElement(name = "res-ref-name", namespace = "http://java.sun.com/xml/ns/javaee",
                required = true)
    protected JndiNameType resRefName;
    @XmlElement(name = "res-type", namespace = "http://java.sun.com/xml/ns/javaee")
    protected FullyQualifiedClassType resType;
    @XmlElement(name = "res-auth", namespace = "http://java.sun.com/xml/ns/javaee")
    protected ResAuthType resAuth;
    @XmlElement(name = "res-sharing-scope", namespace = "http://java.sun.com/xml/ns/javaee")
    protected ResSharingScopeType resSharingScope;
    @XmlElement(name = "mapped-name", namespace = "http://java.sun.com/xml/ns/javaee")
    protected XsdStringType mappedName;
    @XmlElement(name = "injection-target", namespace = "http://java.sun.com/xml/ns/javaee",
                required = true)
    protected List<InjectionTargetType> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected java.lang.String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link DescriptionType }
     */
    public List<DescriptionType> getDescription() {
        if (description == null) {
            description = new ArrayList<DescriptionType>();
        }
        return this.description;
    }

    /**
     * Gets the value of the resRefName property.
     *
     * @return possible object is {@link JndiNameType }
     */
    public JndiNameType getResRefName() {
        return resRefName;
    }

    /**
     * Sets the value of the resRefName property.
     *
     * @param value allowed object is {@link JndiNameType }
     */
    public void setResRefName(JndiNameType value) {
        this.resRefName = value;
    }

    /**
     * Gets the value of the resType property.
     *
     * @return possible object is {@link FullyQualifiedClassType }
     */
    public FullyQualifiedClassType getResType() {
        return resType;
    }

    /**
     * Sets the value of the resType property.
     *
     * @param value allowed object is {@link FullyQualifiedClassType }
     */
    public void setResType(FullyQualifiedClassType value) {
        this.resType = value;
    }

    /**
     * Gets the value of the resAuth property.
     *
     * @return possible object is {@link ResAuthType }
     */
    public ResAuthType getResAuth() {
        return resAuth;
    }

    /**
     * Sets the value of the resAuth property.
     *
     * @param value allowed object is {@link ResAuthType }
     */
    public void setResAuth(ResAuthType value) {
        this.resAuth = value;
    }

    /**
     * Gets the value of the resSharingScope property.
     *
     * @return possible object is {@link ResSharingScopeType }
     */
    public ResSharingScopeType getResSharingScope() {
        return resSharingScope;
    }

    /**
     * Sets the value of the resSharingScope property.
     *
     * @param value allowed object is {@link ResSharingScopeType }
     */
    public void setResSharingScope(ResSharingScopeType value) {
        this.resSharingScope = value;
    }

    /**
     * Gets the value of the mappedName property.
     *
     * @return possible object is {@link XsdStringType }
     */
    public XsdStringType getMappedName() {
        return mappedName;
    }

    /**
     * Sets the value of the mappedName property.
     *
     * @param value allowed object is {@link XsdStringType }
     */
    public void setMappedName(XsdStringType value) {
        this.mappedName = value;
    }

    /**
     * Gets the value of the injectionTarget property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the injectionTarget property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInjectionTarget().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link InjectionTargetType }
     */
    public List<InjectionTargetType> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTargetType>();
        }
        return this.injectionTarget;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link java.lang.String }
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link java.lang.String }
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

}