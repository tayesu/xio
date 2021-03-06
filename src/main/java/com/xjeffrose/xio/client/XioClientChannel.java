package com.xjeffrose.xio.client;


import io.airlift.units.Duration;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import javax.annotation.Nullable;

public interface XioClientChannel extends RequestChannel {

  Duration getSendTimeout();

  void setSendTimeout(@Nullable Duration sendTimeout);

  Duration getReceiveTimeout();

  void setReceiveTimeout(@Nullable Duration receiveTimeout);

  Duration getReadTimeout();

  void setReadTimeout(@Nullable Duration readTimeout);

  void executeInIoThread(Runnable runnable);

  Channel getNettyChannel();

  ChannelHandlerContext getCtx();
}