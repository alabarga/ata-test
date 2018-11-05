/**
 * WSWhatizitServerService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.WSWhatizit;

public interface WSWhatizitServerService extends javax.xml.rpc.Service {
    public java.lang.String getWSwhatizitAddress();

    public uk.ac.ebi.webservices.WSWhatizit.WSWhatizitServer getWSwhatizit() throws javax.xml.rpc.ServiceException;

    public uk.ac.ebi.webservices.WSWhatizit.WSWhatizitServer getWSwhatizit(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
