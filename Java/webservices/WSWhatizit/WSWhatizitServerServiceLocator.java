/**
 * WSWhatizitServerServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSWhatizit;

public class WSWhatizitServerServiceLocator extends org.apache.axis.client.Service implements uk.ac.ebi.webservices.WSWhatizit.WSWhatizitServerService {

    // Use to get a proxy class for WSwhatizit
    private final java.lang.String WSwhatizit_address = "http://web1.ebi.ac.uk:8100/wsmarkup/services/WSwhatizit";

    public java.lang.String getWSwhatizitAddress() {
        return WSwhatizit_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSwhatizitWSDDServiceName = "WSwhatizit";

    public java.lang.String getWSwhatizitWSDDServiceName() {
        return WSwhatizitWSDDServiceName;
    }

    public void setWSwhatizitWSDDServiceName(java.lang.String name) {
        WSwhatizitWSDDServiceName = name;
    }

    public uk.ac.ebi.webservices.WSWhatizit.WSWhatizitServer getWSwhatizit() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSwhatizit_address);
        }
        catch (java.net.MalformedURLException e) {
            return null; // unlikely as URL was validated in WSDL2Java
        }
        return getWSwhatizit(endpoint);
    }

    public uk.ac.ebi.webservices.WSWhatizit.WSWhatizitServer getWSwhatizit(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            uk.ac.ebi.webservices.WSWhatizit.WSwhatizitSoapBindingStub _stub = new uk.ac.ebi.webservices.WSWhatizit.WSwhatizitSoapBindingStub(portAddress, this);
            _stub.setPortName(getWSwhatizitWSDDServiceName());
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
            if (uk.ac.ebi.webservices.WSWhatizit.WSWhatizitServer.class.isAssignableFrom(serviceEndpointInterface)) {
                uk.ac.ebi.webservices.WSWhatizit.WSwhatizitSoapBindingStub _stub = new uk.ac.ebi.webservices.WSWhatizit.WSwhatizitSoapBindingStub(new java.net.URL(WSwhatizit_address), this);
                _stub.setPortName(getWSwhatizitWSDDServiceName());
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
        return new javax.xml.namespace.QName("http://web1.ebi.ac.uk:8100/wsmarkup/services/WSwhatizit", "WSWhatizitServerService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("WSwhatizit"));
        }
        return ports.iterator();
    }

}
