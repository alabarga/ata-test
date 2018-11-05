/**
 * WSInterProScanServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSInterProScan;

public class WSInterProScanServiceLocator extends org.apache.axis.client.Service implements uk.ac.ebi.webservices.WSInterProScan.WSInterProScanService {

    // Use to get a proxy class for WSInterProScan
    private final java.lang.String WSInterProScan_address = "http://www.ebi.ac.uk/cgi-bin/webservices/WSInterProScan";

    public java.lang.String getWSInterProScanAddress() {
        return WSInterProScan_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSInterProScanWSDDServiceName = "WSInterProScan";

    public java.lang.String getWSInterProScanWSDDServiceName() {
        return WSInterProScanWSDDServiceName;
    }

    public void setWSInterProScanWSDDServiceName(java.lang.String name) {
        WSInterProScanWSDDServiceName = name;
    }

    public uk.ac.ebi.webservices.WSInterProScan.WSInterProScan getWSInterProScan() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSInterProScan_address);
        }
        catch (java.net.MalformedURLException e) {
            return null; // unlikely as URL was validated in WSDL2Java
        }
        return getWSInterProScan(endpoint);
    }

    public uk.ac.ebi.webservices.WSInterProScan.WSInterProScan getWSInterProScan(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            uk.ac.ebi.webservices.WSInterProScan.WSInterProScanSoapBindingStub _stub = new uk.ac.ebi.webservices.WSInterProScan.WSInterProScanSoapBindingStub(portAddress, this);
            _stub.setPortName(getWSInterProScanWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (uk.ac.ebi.webservices.WSInterProScan.WSInterProScan.class.isAssignableFrom(serviceEndpointInterface)) {
                uk.ac.ebi.webservices.WSInterProScan.WSInterProScanSoapBindingStub _stub = new uk.ac.ebi.webservices.WSInterProScan.WSInterProScanSoapBindingStub(new java.net.URL(WSInterProScan_address), this);
                _stub.setPortName(getWSInterProScanWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        java.rmi.Remote _stub = getPort(serviceEndpointInterface);
        ((org.apache.axis.client.Stub) _stub).setPortName(portName);
        return _stub;
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "WSInterProScanService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("WSInterProScan"));
        }
        return ports.iterator();
    }

}
