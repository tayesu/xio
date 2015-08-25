package com.xjeffrose.xio.client;

import org.jboss.netty.buffer.ChannelBuffer;

public interface RequestChannel {

  void sendAsynchronousRequest(final ChannelBuffer request,
                               final boolean oneway,
                               final Listener listener)
      throws XioException;

  void close();

  boolean hasError();

  XioException getError();

  XioProtocolFactory getProtocolFactory();
}