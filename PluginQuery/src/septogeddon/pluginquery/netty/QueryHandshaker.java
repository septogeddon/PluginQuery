package septogeddon.pluginquery.netty;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import septogeddon.pluginquery.PreparedQueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.utils.QueryUtil;

public class QueryHandshaker extends ChannelInboundHandlerAdapter {

	protected QueryProtocol protocol;
	public QueryHandshaker(QueryProtocol protocol) {
		this.protocol = protocol;
	}	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ChannelPipeline pipe = ctx.channel().pipeline();
		if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf)msg;
			buf.markReaderIndex();
			try {
				byte length = buf.readByte();
				// prevent overflow byte read
				if (length == QueryContext.PACKET_HANDSHAKE.length()) {
					// read "query"
					byte[] bytes = new byte[length];
					buf.readBytes(bytes);
					if (new String(bytes).equals(QueryContext.PACKET_HANDSHAKE)) {
						// read UUID
						long most = buf.readLong();
						long least = buf.readLong();
						String uuid = new UUID(most, least).toString();
						// read encrypted UUID
						length = buf.readByte();
						bytes = new byte[length];
						buf.readBytes(bytes);
						try {
							// decrypt UUID
							bytes = protocol.getMessenger().getPipeline().dispatchReceiving(protocol.getConnection(), bytes);
							QueryUtil.nonNull(bytes, "unique handshake token");
							// match the decrypted UUID with the UUID
							if (new String(bytes).equals(uuid)) {
								// remove minecraft packet handlers and this handler
								// in this process, read timeout also removed
								// we don't use read timeout, keep it open as long as possible
								pipe.forEach(entry->pipe.remove(entry.getKey()));
								// initialize query channel
								PreparedQueryConnection.handshakenConnection(protocol, pipe);
							} else {
								throw new IllegalArgumentException("invalid encryption");
							}
						} catch (Throwable t) {
							protocol.getConnection().disconnect();
						}
						buf.release();
						return;
					} 
				} else {
					byte[] toAll = new byte[buf.readableBytes()];
					buf.readBytes(toAll);
				}
			} catch (Throwable t) {
			}
			buf.resetReaderIndex();
		}
		try {
			pipe.remove(this);
			remove(pipe, QueryContext.PIPELINE_TIMEOUT);
			remove(pipe, "query_initiator");
			remove(pipe, "query_pushback");
		} catch (Throwable t) {
		}
		ctx.fireChannelRead(msg);
	}
	
	private void remove(ChannelPipeline pipe, String name) {
		try {
			pipe.remove(name);
		} catch (Throwable t) {
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		protocol.getConnection().disconnect();
	}
	
}
