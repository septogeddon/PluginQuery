package septogeddon.pluginquery;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryEventBus;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.api.QueryMetadata;
import septogeddon.pluginquery.api.QueryPipeline;

public class QueryMessengerImpl implements QueryMessenger {

	private QueryMetadata metadata = new QueryMetadataImpl();
	private QueryEventBus eventBus = new QueryEventBusImpl(); 
	private QueryPipeline pipeline = new QueryPipelineImpl();
	private EventLoopGroup eventLoop;
	private Class<? extends Channel> channelClass;
	protected final List<QueryConnection> connections = new ArrayList<>();
	public QueryMessengerImpl() {
		if (Epoll.isAvailable()) {
			eventLoop = new EpollEventLoopGroup();
			channelClass = EpollSocketChannel.class;
		} else {
			eventLoop = new NioEventLoopGroup();
			channelClass = NioSocketChannel.class;
		}
	}
	@Override
	public QueryConnection newConnection(SocketAddress address) {
		QueryConnectionImpl conn = new QueryConnectionImpl(this, address, null);
		conn.getEventBus().addParent(getEventBus());
		connections.add(conn);
		return conn;
	}

	@Override
	public QueryConnection injectConnection(Channel channel) {
		synchronized (connections) {
			for (int i = 0; i < connections.size(); i++) {
				QueryConnection conn = connections.get(i);
				if (channel.equals(conn.getChannel())) {
					// already injected, wont inject any other.
					return conn;
				}
			}
		}
		QueryConnectionImpl conn = new QueryConnectionImpl(this, channel.remoteAddress(), channel);
		conn.getEventBus().addParent(getEventBus());
		connections.add(conn);
		return conn;
	}

	@Override
	public Collection<? extends QueryConnection> getConnections() {
		return new ArrayList<>(connections);
	}

	@Override
	public void broadcastQuery(String channel, byte[] message) {
		synchronized (connections) {
			for (int i = 0; i < connections.size(); i++) {
				QueryConnection conn = connections.get(i);
				conn.sendQuery(channel, message);
			}
		}
	}

	@Override
	public QueryMetadata getMetadata() {
		return metadata;
	}

	@Override
	public QueryEventBus getEventBus() {
		return eventBus;
	}

	@Override
	public void broadcastQuery(String channel, byte[] message, boolean queue) {
		synchronized (connections) {
			for (int i = 0; i < connections.size(); i++) {
				QueryConnection conn = connections.get(i);
				conn.sendQuery(channel, message, queue);
			}
		}
	}

	@Override
	public QueryPipeline getPipeline() {
		return pipeline;
	}

	@Override
	public EventLoopGroup getEventLoopGroup() {
		return eventLoop;
	}
	@Override
	public Class<? extends Channel> getChannelClass() {
		return channelClass;
	}
	@Override
	public void closeConnection(QueryConnection connection) {
		if (connection.isConnected()) {
			connection.disconnect();
		}
		synchronized(connections) {
			connections.remove(connection);
		}
	}

}