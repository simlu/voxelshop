package com.pixelatedgames.fos;

import static org.jboss.netty.channel.Channels.*;

import com.pixelatedgames.fos.protobufs.FantasyMessages;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * User: J
 * Date: 3/25/12
 * Time: 11:36 PM
 */
public class FantasyServerPipelineFactory implements ChannelPipelineFactory {
    private final FantasyServer _fantasyServer;

    public FantasyServerPipelineFactory(FantasyServer fantasyServer) {
        _fantasyServer = fantasyServer;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = pipeline();
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufDecoder", new ProtobufDecoder(FantasyMessages.FantasyMessage.getDefaultInstance()));
        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        p.addLast("protobufEncoder", new ProtobufEncoder());
        p.addLast("handler", new FantasyServerHandler(_fantasyServer));
        return p;
    }
}
