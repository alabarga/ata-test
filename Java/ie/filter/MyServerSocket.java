package com.ata.ie.filter;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class MyServerSocket extends ServerSocket {
  
  public static final int SEND_RECV_SOCKET_BUFFER_SIZE = 1024 * 4;
  
  
  public MyServerSocket (int port, int queue, InetAddress ip) throws IOException {
    super(port, queue, ip);
  }
  
  public Socket accept () throws IOException {
    Socket sock = super.accept();
    sock.setSendBufferSize(SEND_RECV_SOCKET_BUFFER_SIZE);
    sock.setReceiveBufferSize(SEND_RECV_SOCKET_BUFFER_SIZE);
    sock.setTcpNoDelay(true);
    return sock;
  }
}
