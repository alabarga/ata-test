/**
 * InputParams.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSInterProScan;

public class InputParams  implements java.io.Serializable {
    private java.lang.String app;
    private boolean crc;
    private java.lang.String seqtype;
    private int trlen;
    private int trtable;
    private boolean goterms;
    private boolean async;
    private java.lang.String outformat;
    private java.lang.String email;

    public InputParams() {
    }

    public java.lang.String getApp() {
        return app;
    }

    public void setApp(java.lang.String app) {
        this.app = app;
    }

    public boolean isCrc() {
        return crc;
    }

    public void setCrc(boolean crc) {
        this.crc = crc;
    }

    public java.lang.String getSeqtype() {
        return seqtype;
    }

    public void setSeqtype(java.lang.String seqtype) {
        this.seqtype = seqtype;
    }

    public int getTrlen() {
        return trlen;
    }

    public void setTrlen(int trlen) {
        this.trlen = trlen;
    }

    public int getTrtable() {
        return trtable;
    }

    public void setTrtable(int trtable) {
        this.trtable = trtable;
    }

    public boolean isGoterms() {
        return goterms;
    }

    public void setGoterms(boolean goterms) {
        this.goterms = goterms;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public java.lang.String getOutformat() {
        return outformat;
    }

    public void setOutformat(java.lang.String outformat) {
        this.outformat = outformat;
    }

    public java.lang.String getEmail() {
        return email;
    }

    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InputParams)) return false;
        InputParams other = (InputParams) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((app==null && other.getApp()==null) || 
             (app!=null &&
              app.equals(other.getApp()))) &&
            crc == other.isCrc() &&
            ((seqtype==null && other.getSeqtype()==null) || 
             (seqtype!=null &&
              seqtype.equals(other.getSeqtype()))) &&
            trlen == other.getTrlen() &&
            trtable == other.getTrtable() &&
            goterms == other.isGoterms() &&
            async == other.isAsync() &&
            ((outformat==null && other.getOutformat()==null) || 
             (outformat!=null &&
              outformat.equals(other.getOutformat()))) &&
            ((email==null && other.getEmail()==null) || 
             (email!=null &&
              email.equals(other.getEmail())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getApp() != null) {
            _hashCode += getApp().hashCode();
        }
        _hashCode += new Boolean(isCrc()).hashCode();
        if (getSeqtype() != null) {
            _hashCode += getSeqtype().hashCode();
        }
        _hashCode += getTrlen();
        _hashCode += getTrtable();
        _hashCode += new Boolean(isGoterms()).hashCode();
        _hashCode += new Boolean(isAsync()).hashCode();
        if (getOutformat() != null) {
            _hashCode += getOutformat().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(InputParams.class);

    static {
        org.apache.axis.description.FieldDesc field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("app");
        field.setXmlName(new javax.xml.namespace.QName("", "app"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("crc");
        field.setXmlName(new javax.xml.namespace.QName("", "crc"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("seqtype");
        field.setXmlName(new javax.xml.namespace.QName("", "seqtype"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("trlen");
        field.setXmlName(new javax.xml.namespace.QName("", "trlen"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("trtable");
        field.setXmlName(new javax.xml.namespace.QName("", "trtable"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("goterms");
        field.setXmlName(new javax.xml.namespace.QName("", "goterms"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("async");
        field.setXmlName(new javax.xml.namespace.QName("", "async"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("outformat");
        field.setXmlName(new javax.xml.namespace.QName("", "outformat"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("email");
        field.setXmlName(new javax.xml.namespace.QName("", "email"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
    };

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
