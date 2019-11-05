package com.gram.gram_landlord.netty;

import com.google.gson.Gson;
import com.gram.gram_landlord.Constant;
import com.gram.gram_landlord.api.ApiHandler;
import com.gram.gram_landlord.entity.Player;
import com.gram.gram_landlord.entity.Table;
import com.gram.gram_landlord.protocols.request.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
    private Map<Integer, Player> playerMap;//一个座位对应一个玩家
    private Map<Integer, Table> tableMap;//一桌对应一个table实体类对象
    private Map<String, Player> userName2Player;//记录全部牌友信息
    private Player player;
    private ApiHandler apiHandler;
    private Gson gson;

    private static final String REQUEST_PATH = "com.gram.gram_landlord.protocols.request.";

    public NettyServerHandler(Map<Integer, Player> playerMap, Map<Integer, Table> tableMap, Map<String, Player> userName2Player) {
        if(playerMap == null || tableMap == null || userName2Player == null) throw new NullPointerException("参数不能为空！");
        this.playerMap = playerMap;
        this.tableMap = tableMap;
        this.userName2Player = userName2Player;
        player = new Player();
        gson = new Gson();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if(!msg.contains("HeartBeatRequest")) log.info("{}向服务器发送了消息：{}", ctx.channel().id().asShortText(), msg);
        try {
            int endIndex = msg.indexOf("{");
            String className = REQUEST_PATH + msg.substring(0, endIndex);
            String classContent = msg.substring(endIndex);
            if(!className.equals("HeartBeatRequest")) {
                Class<?> class1 = Class.forName(className);
                if(class1 != null) {
                    Request request = (Request) gson.fromJson(classContent, class1);
                    apiHandler = new ApiHandler(playerMap,tableMap,userName2Player,player);
                    apiHandler.handle(request);
                }
            } else {
                ctx.channel().attr(AttributeKey.valueOf("heartbeat")).set(System.currentTimeMillis());
                ctx.writeAndFlush("HeartBeatResponse" + Constant.LINE_SEPARATOR);
            }
            if(className.equals("SystemExitRequest")) ctx.channel().close();
        } catch (IndexOutOfBoundsException e) {
            log.error("数组越界异常：{}", e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("找不到类异常：{}", e.getMessage());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(player == null) throw new NullPointerException("player不能为空！");
        player.setChannel(ctx.channel());
        log.info("有位新玩家连接上服务器!其IP地址：{};当前在线人数为：{}", ctx.channel().remoteAddress(), userName2Player.size() + 1);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(player != null) {
            log.info("座位号为{}的玩家退出系统", player.getSeatNum());
            handleAfterExitOrException();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("出现异常: {}", cause.getMessage());
        cause.printStackTrace();
        if(player != null) {
            log.info("座位号为{}的玩家出现异常，退出系统", player.getSeatNum());
            handleAfterExitOrException();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(!(evt instanceof IdleStateEvent)) return;
        IdleStateEvent event = (IdleStateEvent) evt;
        if(event.state() == IdleState.READER_IDLE) {
            log.warn("read idle warning！！");
            ctx.channel().attr(AttributeKey.valueOf("heartbeat")).set(System.currentTimeMillis());
            ctx.writeAndFlush("HeartbeatResponse" + Constant.LINE_SEPARATOR);
            if(System.currentTimeMillis() - (Long)ctx.channel().attr(AttributeKey.valueOf("heartbeat")).get() >= 120000L) {
                if(player != null) handleAfterExitOrException();
                ctx.channel().attr(AttributeKey.valueOf("heartbeat")).set(null);
                ctx.close();
            }
        }
    }

    private void handleAfterExitOrException() {
        if(player != null && player.getSeatNum() > -1) {
            apiHandler.exitOrException();
        }
        if (player != null && player.getUserName() != null) {
            userName2Player.remove(player.getUserName());
            log.info("{}退出系统了", player.getUserName());
        }
        player = null;
    }
}
