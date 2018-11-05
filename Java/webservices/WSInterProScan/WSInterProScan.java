/**
 * WSInterProScan.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSInterProScan;

public interface WSInterProScan extends java.rmi.Remote {
    public java.lang.String runInterProScan(uk.ac.ebi.webservices.WSInterProScan.InputParams params, uk.ac.ebi.webservices.WSInterProScan.Data[] content) throws java.rmi.RemoteException;
    public java.lang.String checkStatus(java.lang.String jobid) throws java.rmi.RemoteException;
    public byte[] poll(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException;
    public uk.ac.ebi.webservices.WSInterProScan.WSFile[] getResults(java.lang.String jobid) throws java.rmi.RemoteException;
    public byte[] test(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException;
    public java.lang.String polljob(java.lang.String jobid, java.lang.String outformat) throws java.rmi.RemoteException;
    public byte[] doIprscan(uk.ac.ebi.webservices.WSInterProScan.InputParams params, byte[] content) throws java.rmi.RemoteException;
}
