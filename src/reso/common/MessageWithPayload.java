package reso.common;

public interface MessageWithPayload extends Message {

    Message getPayload();

    int getProtocol();

}
