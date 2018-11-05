/**
 * WSClustalW.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSClustalW;

public interface WSClustalW extends java.rmi.Remote {
    public java.lang.String runClustalW(uk.ac.ebi.webservices.WSClustalW.InputParams params, uk.ac.ebi.webservices.WSClustalW.Data[] content) throws java.rmi.RemoteException;
    public java.lang.String checkStatus(java.lang.String jobid) throws java.rmi.RemoteException;
    public byte[] poll(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException;
    public uk.ac.ebi.webservices.WSClustalW.WSFile[] getResults(java.lang.String jobid) throws java.rmi.RemoteException;
    public byte[] test(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException;
}
