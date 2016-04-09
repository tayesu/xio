package com.xjeffrose.xio.client.loadbalancer;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.xjeffrose.xio.client.retry.ExponentialBackoffRetry;
import com.xjeffrose.xio.client.retry.RetryLoop;
import com.xjeffrose.xio.client.retry.TracerDriver;
import io.netty.channel.Channel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The base type of nodes over which load is balanced. Nodes define the load metric that is used;
 * distributors like P2C will use these to decide where to balance the next connection request.
 */
public class Node {

  private final UUID token = UUID.randomUUID();
  private final ConcurrentLinkedDeque<Channel> pending = new ConcurrentLinkedDeque<>();
  private final SocketAddress address;
  private double load;
  private final ImmutableList<String> filters;
  private final int weight;

  public Node(HostAndPort hostAndPort) {
    this(toInetAddress(hostAndPort));
  }

  public Node(SocketAddress address) {
    this(address, ImmutableList.of(), 0);
  }

  public Node(SocketAddress address, int weight) {
    this(address, ImmutableList.of(), weight);
  }

  public Node(SocketAddress address, ImmutableList<String> filters, int weight) {
    this.address = address;
    this.load = 0;
    this.filters = ImmutableList.copyOf(filters);
    this.weight = weight;
  }

  /**
   * The current host and port returned as a InetSocketAddress
   */
  public static InetSocketAddress toInetAddress(HostAndPort hostAndPort) {
    return (hostAndPort == null) ? null : new InetSocketAddress(hostAndPort.getHostText(), hostAndPort.getPort());
  }

  /**
   * The current load, in units of the active metric.
   */
  public double load() {
    return load;
  }

  /**
   * The number of pending requests to this node.
   */
  public int pending() {
    return pending.size();
  }

  /**
   * A token is a random integer identifying the node. It persists through node updates.
   */
  public UUID token() {
    return token;
  }

  public InetSocketAddress address() {
    return (InetSocketAddress)address;
  }

  public void pending(Channel channel) {
    pending.add(channel);
  }

  public boolean isAvailable() {

    //TODO(JR): Make this value configurable
    RetryLoop retryLoop = new RetryLoop(new ExponentialBackoffRetry(20, 3, 50), new AtomicReference<TracerDriver>());

    while (retryLoop.shouldContinue()) {
    try {
      SocketChannel channel = SocketChannel.open();
      channel.connect(address);
//      channel.configureBlocking(false);
      channel.close();
      return true;
    } catch (IOException e) {
      try {
        retryLoop.takeException(e);
      } catch (Exception e1) {
        return false;
      }
    }
    }
    return false;
  }

  public ImmutableList<String> getFilters() {
    return filters;
  }

  public int getWeight() {
    return weight;
  }
}
