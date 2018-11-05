/**
 * WSClustalWServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSClustalW;

public class WSClustalWServiceLocator extends org.apache.axis.client.Service implements uk.ac.ebi.webservices.WSClustalW.WSClustalWService {

    // Use to get a proxy class for WSClustalW
    private final java.lang.String WSClustalW_address = "http://www.ebi.ac.uk/cgi-bin/webservices/WSClustalW";

    public java.lang.String getWSClustalWAddress() {
        return WSClustalW_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSClustalWWSDDServiceName = "WSClustalW";

    public java.lang.String getWSClustalWWSDDServiceName() {
        return WSClustalWWSDDServiceName;
    }

    public void setWSClustalWWSDDServiceName(java.lang.String name) {
        WSClustalWWSDDServiceName = name;
    }

    public uk.ac.ebi.webservices.WSClustalW.WSClustalW getWSClustalW() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSClustalW_address);
        }
        catch (java.net.MalformedURLException e) {
            return null; // unlikely as URL was validated in WSDL2Java
        }
        return getWSClustalW(endpoint);
    }

    public uk.ac.ebi.webservices.WSClustalW.WSClustalW getWSClustalW(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            uk.ac.ebi.webservices.WSClustalW.WSClustalWSoapBindingStub _stub = new uk.ac.ebi.webservices.WSClustalW.WSClustalWSoapBindingStub(portAddress, this);
            _stub.setPortName(getWSClustalWWSDDServiceName());
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
            if (uk.ac.ebi.webservices.WSClustalW.WSClustalW.class.isAssignableFrom(serviceEndpointInterface)) {
                uk.ac.ebi.webservices.WSClustalW.WSClustalWSoapBindingStub _stub = new uk.ac.ebi.webservices.WSClustalW.WSClustalWSoapBindingStub(new java.net.URL(WSClustalW_address), this);
                _stub.setPortName(getWSClustalWWSDDServiceName());
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
        return new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "WSClustalWService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("WSClustalW"));
        }
        return ports.iterator();
    }

}
