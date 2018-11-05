/**
 * WSInterProScanSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSInterProScan;

public class WSInterProScanSoapBindingStub extends org.apache.axis.client.Stub implements uk.ac.ebi.webservices.WSInterProScan.WSInterProScan {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    public WSInterProScanSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public WSInterProScanSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public WSInterProScanSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "WSFile");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSInterProScan.WSFile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "inputParams");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSInterProScan.InputParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "data");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSInterProScan.Data.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "WSArrayofFile");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSInterProScan.WSFile[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(arraysf);
            cachedDeserFactories.add(arraydf);

            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "WSArrayofData");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSInterProScan.Data[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(arraysf);
            cachedDeserFactories.add(arraydf);

    }

    private org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call =
                    (org.apache.axis.client.Call) super.service.createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                if(_call.isPropertySupported(key))
                    _call.setProperty(key, super.cachedProperties.get(key));
                else
                    _call.setScopedProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                        java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                        _call.registerTypeMapping(cls, qName, sf, df, false);
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", t);
        }
    }

    public byte[] poll(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "jobid"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.addParameter(new javax.xml.namespace.QName("", "type"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSInterProScan#poll");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "poll"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {jobid, type});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (byte[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (byte[]) org.apache.axis.utils.JavaUtils.convert(_resp, byte[].class);
            }
        }
    }

    public java.lang.String runInterProScan(uk.ac.ebi.webservices.WSInterProScan.InputParams params, uk.ac.ebi.webservices.WSInterProScan.Data[] content) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "params"), new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "inputParams"), uk.ac.ebi.webservices.WSInterProScan.InputParams.class, javax.xml.rpc.ParameterMode.IN);
        _call.addParameter(new javax.xml.namespace.QName("", "content"), new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "WSArrayofData"), uk.ac.ebi.webservices.WSInterProScan.Data[].class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSInterProScan#runInterProScan");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "runInterProScan"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {params, content});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
    }

    public byte[] test(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "jobid"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.addParameter(new javax.xml.namespace.QName("", "type"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSInterProScan#test");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "test"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {jobid, type});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (byte[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (byte[]) org.apache.axis.utils.JavaUtils.convert(_resp, byte[].class);
            }
        }
    }

    public java.lang.String checkStatus(java.lang.String jobid) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "jobid"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSInterProScan#checkStatus");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "checkStatus"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {jobid});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
    }

    public uk.ac.ebi.webservices.WSInterProScan.WSFile[] getResults(java.lang.String jobid) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "jobid"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "WSArrayofFile"), uk.ac.ebi.webservices.WSInterProScan.WSFile[].class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSInterProScan#getResults");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "getResults"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {jobid});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (uk.ac.ebi.webservices.WSInterProScan.WSFile[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (uk.ac.ebi.webservices.WSInterProScan.WSFile[]) org.apache.axis.utils.JavaUtils.convert(_resp, uk.ac.ebi.webservices.WSInterProScan.WSFile[].class);
            }
        }
    }

    public java.lang.String polljob(java.lang.String jobid, java.lang.String outformat) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "jobid"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.addParameter(new javax.xml.namespace.QName("", "outformat"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSIprscan#polljob");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "polljob"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {jobid, outformat});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
    }

    public byte[] doIprscan(uk.ac.ebi.webservices.WSInterProScan.InputParams params, byte[] content) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "params"), new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "inputParams"), uk.ac.ebi.webservices.WSInterProScan.InputParams.class, javax.xml.rpc.ParameterMode.IN);
        _call.addParameter(new javax.xml.namespace.QName("", "content"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSIprscan#doIprscan");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "doIprscan"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {params, content});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (byte[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (byte[]) org.apache.axis.utils.JavaUtils.convert(_resp, byte[].class);
            }
        }
    }

}
