/**
 * WSClustalWSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSClustalW;

public class WSClustalWSoapBindingStub extends org.apache.axis.client.Stub implements uk.ac.ebi.webservices.WSClustalW.WSClustalW {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    public WSClustalWSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public WSClustalWSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public WSClustalWSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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
            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "data");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSClustalW.Data.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "WSFile");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSClustalW.WSFile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "WSArrayofData");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSClustalW.Data[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(arraysf);
            cachedDeserFactories.add(arraydf);

            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "inputParams");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSClustalW.InputParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "WSArrayofFile");
            cachedSerQNames.add(qName);
            cls = uk.ac.ebi.webservices.WSClustalW.WSFile[].class;
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
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSClustalW#poll");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "poll"));

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

    public java.lang.String runClustalW(uk.ac.ebi.webservices.WSClustalW.InputParams params, uk.ac.ebi.webservices.WSClustalW.Data[] content) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "params"), new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "inputParams"), uk.ac.ebi.webservices.WSClustalW.InputParams.class, javax.xml.rpc.ParameterMode.IN);
        _call.addParameter(new javax.xml.namespace.QName("", "content"), new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "WSArrayofData"), uk.ac.ebi.webservices.WSClustalW.Data[].class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSClustalW#runClustalW");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "runClustalW"));

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
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSClustalW#test");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "test"));

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
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSClustalW#checkStatus");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "checkStatus"));

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

    public uk.ac.ebi.webservices.WSClustalW.WSFile[] getResults(java.lang.String jobid) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("", "jobid"), new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "WSArrayofFile"), uk.ac.ebi.webservices.WSClustalW.WSFile[].class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://www.ebi.ac.uk/WSClustalW#getResults");
        _call.setOperationStyle("rpc");
        _call.setOperationName(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSClustalW", "getResults"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {jobid});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (uk.ac.ebi.webservices.WSClustalW.WSFile[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (uk.ac.ebi.webservices.WSClustalW.WSFile[]) org.apache.axis.utils.JavaUtils.convert(_resp, uk.ac.ebi.webservices.WSClustalW.WSFile[].class);
            }
        }
    }

}
