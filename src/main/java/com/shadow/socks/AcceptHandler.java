package com.shadow.socks;

import com.shadow.cipher.ICrypt;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * 接受s端数据，发给浏览器
 */
public final class AcceptHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getAnonymousLogger();
    private Channel channel;
    private ICrypt iCrypt;

    public AcceptHandler(Channel channel, ICrypt iCrypt) {
        this.channel = channel;
        this.iCrypt = iCrypt;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteArrayOutputStream stream = null;
        try {
            if (channel.isActive()) {
                ByteBuf bytebuff = (ByteBuf) msg;
                if (!bytebuff.hasArray()) {
                    int len = bytebuff.readableBytes();
                    byte[] arr = new byte[len];
                    bytebuff.getBytes(0, arr);
                    stream = new ByteArrayOutputStream();
                    iCrypt.decrypt(arr, stream);
                    arr = stream.toByteArray();
                    channel.writeAndFlush(Unpooled.wrappedBuffer(arr));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stream.close();
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("read_inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
