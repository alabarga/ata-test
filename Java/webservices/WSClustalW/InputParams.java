/**
 * InputParams.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSClustalW;

public class InputParams  implements java.io.Serializable {
    private java.lang.String alignment;
    private java.lang.String output;
    private boolean tossgaps;
    private int ktup;
    private int window;
    private int gapopen;
    private int gapclose;
    private float gapext;
    private int gapdist;
    private int topdiags;
    private int pairgap;
    private java.lang.String outputtree;
    private java.lang.String matrix;
    private java.lang.String cpu;
    private boolean kimura;
    private boolean tree;
    private boolean align;
    private boolean quicktree;
    private java.lang.String scores;
    private java.lang.String outorder;
    private java.lang.String email;
    private boolean async;

    public InputParams() {
    }

    public java.lang.String getAlignment() {
        return alignment;
    }

    public void setAlignment(java.lang.String alignment) {
        this.alignment = alignment;
    }

    public java.lang.String getOutput() {
        return output;
    }

    public void setOutput(java.lang.String output) {
        this.output = output;
    }

    public boolean isTossgaps() {
        return tossgaps;
    }

    public void setTossgaps(boolean tossgaps) {
        this.tossgaps = tossgaps;
    }

    public int getKtup() {
        return ktup;
    }

    public void setKtup(int ktup) {
        this.ktup = ktup;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public int getGapopen() {
        return gapopen;
    }

    public void setGapopen(int gapopen) {
        this.gapopen = gapopen;
    }

    public int getGapclose() {
        return gapclose;
    }

    public void setGapclose(int gapclose) {
        this.gapclose = gapclose;
    }

    public float getGapext() {
        return gapext;
    }

    public void setGapext(float gapext) {
        this.gapext = gapext;
    }

    public int getGapdist() {
        return gapdist;
    }

    public void setGapdist(int gapdist) {
        this.gapdist = gapdist;
    }

    public int getTopdiags() {
        return topdiags;
    }

    public void setTopdiags(int topdiags) {
        this.topdiags = topdiags;
    }

    public int getPairgap() {
        return pairgap;
    }

    public void setPairgap(int pairgap) {
        this.pairgap = pairgap;
    }

    public java.lang.String getOutputtree() {
        return outputtree;
    }

    public void setOutputtree(java.lang.String outputtree) {
        this.outputtree = outputtree;
    }

    public java.lang.String getMatrix() {
        return matrix;
    }

    public void setMatrix(java.lang.String matrix) {
        this.matrix = matrix;
    }

    public java.lang.String getCpu() {
        return cpu;
    }

    public void setCpu(java.lang.String cpu) {
        this.cpu = cpu;
    }

    public boolean isKimura() {
        return kimura;
    }

    public void setKimura(boolean kimura) {
        this.kimura = kimura;
    }

    public boolean isTree() {
        return tree;
    }

    public void setTree(boolean tree) {
        this.tree = tree;
    }

    public boolean isAlign() {
        return align;
    }

    public void setAlign(boolean align) {
        this.align = align;
    }

    public boolean isQuicktree() {
        return quicktree;
    }

    public void setQuicktree(boolean quicktree) {
        this.quicktree = quicktree;
    }

    public java.lang.String getScores() {
        return scores;
    }

    public void setScores(java.lang.String scores) {
        this.scores = scores;
    }

    public java.lang.String getOutorder() {
        return outorder;
    }

    public void setOutorder(java.lang.String outorder) {
        this.outorder = outorder;
    }

    public java.lang.String getEmail() {
        return email;
    }

    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
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
            ((alignment==null && other.getAlignment()==null) || 
             (alignment!=null &&
              alignment.equals(other.getAlignment()))) &&
            ((output==null && other.getOutput()==null) || 
             (output!=null &&
              output.equals(other.getOutput()))) &&
            tossgaps == other.isTossgaps() &&
            ktup == other.getKtup() &&
            window == other.getWindow() &&
            gapopen == other.getGapopen() &&
            gapclose == other.getGapclose() &&
            gapext == other.getGapext() &&
            gapdist == other.getGapdist() &&
            topdiags == other.getTopdiags() &&
            pairgap == other.getPairgap() &&
            ((outputtree==null && other.getOutputtree()==null) || 
             (outputtree!=null &&
              outputtree.equals(other.getOutputtree()))) &&
            ((matrix==null && other.getMatrix()==null) || 
             (matrix!=null &&
              matrix.equals(other.getMatrix()))) &&
            ((cpu==null && other.getCpu()==null) || 
             (cpu!=null &&
              cpu.equals(other.getCpu()))) &&
            kimura == other.isKimura() &&
            tree == other.isTree() &&
            align == other.isAlign() &&
            quicktree == other.isQuicktree() &&
            ((scores==null && other.getScores()==null) || 
             (scores!=null &&
              scores.equals(other.getScores()))) &&
            ((outorder==null && other.getOutorder()==null) || 
             (outorder!=null &&
              outorder.equals(other.getOutorder()))) &&
            ((email==null && other.getEmail()==null) || 
             (email!=null &&
              email.equals(other.getEmail()))) &&
            async == other.isAsync();
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
        if (getAlignment() != null) {
            _hashCode += getAlignment().hashCode();
        }
        if (getOutput() != null) {
            _hashCode += getOutput().hashCode();
        }
        _hashCode += new Boolean(isTossgaps()).hashCode();
        _hashCode += getKtup();
        _hashCode += getWindow();
        _hashCode += getGapopen();
        _hashCode += getGapclose();
        _hashCode += new Float(getGapext()).hashCode();
        _hashCode += getGapdist();
        _hashCode += getTopdiags();
        _hashCode += getPairgap();
        if (getOutputtree() != null) {
            _hashCode += getOutputtree().hashCode();
        }
        if (getMatrix() != null) {
            _hashCode += getMatrix().hashCode();
        }
        if (getCpu() != null) {
            _hashCode += getCpu().hashCode();
        }
        _hashCode += new Boolean(isKimura()).hashCode();
        _hashCode += new Boolean(isTree()).hashCode();
        _hashCode += new Boolean(isAlign()).hashCode();
        _hashCode += new Boolean(isQuicktree()).hashCode();
        if (getScores() != null) {
            _hashCode += getScores().hashCode();
        }
        if (getOutorder() != null) {
            _hashCode += getOutorder().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        _hashCode += new Boolean(isAsync()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(InputParams.class);

    static {
        org.apache.axis.description.FieldDesc field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("alignment");
        field.setXmlName(new javax.xml.namespace.QName("", "alignment"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("output");
        field.setXmlName(new javax.xml.namespace.QName("", "output"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("tossgaps");
        field.setXmlName(new javax.xml.namespace.QName("", "tossgaps"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("ktup");
        field.setXmlName(new javax.xml.namespace.QName("", "ktup"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("window");
        field.setXmlName(new javax.xml.namespace.QName("", "window"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("gapopen");
        field.setXmlName(new javax.xml.namespace.QName("", "gapopen"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("gapclose");
        field.setXmlName(new javax.xml.namespace.QName("", "gapclose"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("gapext");
        field.setXmlName(new javax.xml.namespace.QName("", "gapext"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("gapdist");
        field.setXmlName(new javax.xml.namespace.QName("", "gapdist"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("topdiags");
        field.setXmlName(new javax.xml.namespace.QName("", "topdiags"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("pairgap");
        field.setXmlName(new javax.xml.namespace.QName("", "pairgap"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("outputtree");
        field.setXmlName(new javax.xml.namespace.QName("", "outputtree"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("matrix");
        field.setXmlName(new javax.xml.namespace.QName("", "matrix"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("cpu");
        field.setXmlName(new javax.xml.namespace.QName("", "cpu"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("kimura");
        field.setXmlName(new javax.xml.namespace.QName("", "kimura"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("tree");
        field.setXmlName(new javax.xml.namespace.QName("", "tree"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("align");
        field.setXmlName(new javax.xml.namespace.QName("", "align"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("quicktree");
        field.setXmlName(new javax.xml.namespace.QName("", "quicktree"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("scores");
        field.setXmlName(new javax.xml.namespace.QName("", "scores"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("outorder");
        field.setXmlName(new javax.xml.namespace.QName("", "outorder"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("email");
        field.setXmlName(new javax.xml.namespace.QName("", "email"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("async");
        field.setXmlName(new javax.xml.namespace.QName("", "async"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
