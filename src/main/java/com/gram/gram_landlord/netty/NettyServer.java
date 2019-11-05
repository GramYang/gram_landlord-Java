package com.gram.gram_landlord.netty;

import com.gram.gram_landlord.Constant;
import com.gram.gram_landlord.entity.Player;
import com.gram.gram_landlord.entity.Table;
import com.gram.gram_landlord.util.ContextSSLFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLEngine;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NettyServer extends ChannelInitializer<NioSocketChannel> {
    private static volatile Map<Integer, Player> playerMap = new ConcurrentHashMap<Integer, Player>(300);//一个座位对应一个玩家
    private static volatile Map<Integer, Table> tableMap = new ConcurrentHashMap<Integer, Table>(100);//一桌对应一个table实体类对象
    private static volatile Map<String, Player> userName2Player = new ConcurrentHashMap<>(350);//记录全部牌友信息
    private static final EventExecutorGroup handleGroup = new DefaultEventExecutorGroup(16);
    private static EventLoopGroup boss = new NioEventLoopGroup(1);
    private static EventLoopGroup work = new NioEventLoopGroup();

    @PostConstruct
    public void startServer() {
        for(int i = 1; i <= 300; i++) playerMap.put(i, new Player());
        for(int j = 1; j <= 100; j++) tableMap.put(j, new Table());
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(this)
                .bind(6789)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        future.removeListener(this);
                        if(!future.isSuccess() && future.cause() != null) log.error("服务器绑定端口失败", future.cause());
                        if(future.isSuccess()) log.info("服务器绑定端口成功");
                    }
                });
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        ByteBuf delimiter = Unpooled.copiedBuffer(Constant.LINE_SEPARATOR.getBytes());
        pipeline.addLast(new DelimiterBasedFrameDecoder(10240,delimiter));
        pipeline.addLast(new IdleStateHandler(60,0, 0));
        pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
        pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));
        pipeline.addLast(handleGroup, new NettyServerHandler(playerMap, tableMap, userName2Player));
//        SSLEngine engine = ContextSSLFactory.getServerSslContext().createSSLEngine();
//        engine.setUseClientMode(false);
//        engine.setNeedClientAuth(true);
//        pipeline.addFirst(new SslHandler(engine));
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        boss.shutdownGracefully().sync();
        work.shutdownGracefully().sync();
        log.info("游戏服务器关闭！！");
    }
}
