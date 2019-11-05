package com.gram.gram_landlord.api;

import com.google.gson.Gson;
import com.gram.gram_landlord.Constant;
import com.gram.gram_landlord.entity.HallTable;
import com.gram.gram_landlord.entity.Player;
import com.gram.gram_landlord.entity.Table;
import com.gram.gram_landlord.protocols.request.*;
import com.gram.gram_landlord.protocols.response.*;
import com.gram.gram_landlord.util.EncryptUtil;
import com.gram.gram_landlord.util.LandlordUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
public class ApiHandler {
    /**
     * 全局变量
     */
    private volatile Map<Integer, Player> playerMap;//座位号-玩家实例，有效范围：游戏房间
    private volatile Map<Integer, Table> tableMap;//牌桌号-牌桌实例，有效范围：游戏房间
    private volatile Map<String, Player> userName2Player;//用户名-玩家实例，有效范围：大厅
    private volatile ArrayList<HallTable> hallList = new ArrayList<>(100); //游戏大厅中游戏房间人员信息
    private Gson gson;
    /**
     * 个人保存变量
     */
    private Player player; //你

    public ApiHandler(Map<Integer, Player> playerMap, Map<Integer, Table> tableMap,
                      Map<String, Player> userName2Player, Player player) {
        this.playerMap = playerMap;
        this.tableMap = tableMap;
        this.userName2Player = userName2Player;
        this.player = player;
        gson = new Gson();
    }

    public void handle(Request request) {
        //首先登录反馈，然后群发消息，最后返回大厅牌桌信息
        if(request instanceof LoginRequest) {
            LoginRequest loginRequest = (LoginRequest) request;
            LoginResponse loginResponse;
            String userName = loginRequest.getUserName();
            //登录失败反馈
            if(!loginAuthentication(loginRequest.getUserName(), loginRequest.getPassword())) {
                loginResponse = new LoginResponse(userName, false,
                        "用户名或密码错误！！");
                singleSendMsg(player, loginResponse.getClass().getSimpleName() + gson.toJson(loginResponse));
            } else {
                //登录成功反馈
                loginResponse = new LoginResponse(userName, true,
                        "登录成功");
                singleSendMsg(player, loginResponse.getClass().getSimpleName() + gson.toJson(loginResponse));
                //群发提示信息
                player.setUserName(userName);
                userName2Player.put(userName, player);
                ChatMsgResponse chatMsgResponse = new ChatMsgResponse(1, userName,
                        userName + "骑着母猪大摇大摆溜进游戏室！", -1);
                batchSendMsg(chatMsgResponse.getClass().getSimpleName() + gson.toJson(chatMsgResponse),
                        userName2Player.values(),true);
                //反馈大厅房间信息
                InitHallResponse initHallResponse = new InitHallResponse();
                for(Map.Entry<Integer, Table> entry : tableMap.entrySet()) {
                    List<Player> players = entry.getValue().getPlayers();
                    ArrayList<String> userNames = new ArrayList<>();
                    for(Player player : players) userNames.add(player.getUserName());
                    HallTable hallTable = new HallTable(entry.getKey(), userNames, entry.getValue().isPlay(),
                            entry.getValue().getPlayCount() == 3);
                    hallList.add(hallTable);
                }
                initHallResponse.setTableList(hallList);
                singleSendMsg(player, initHallResponse.getClass().getSimpleName() + gson.toJson(initHallResponse));
            }
        }
        if(request instanceof InitHallRequest) {
            //反馈大厅房间信息
            InitHallResponse initHallResponse = new InitHallResponse();
            for(Map.Entry<Integer, Table> entry : tableMap.entrySet()) {
                List<Player> players = entry.getValue().getPlayers();
                ArrayList<String> userNames = new ArrayList<>();
                for(Player player : players) userNames.add(player.getUserName());
                HallTable hallTable = new HallTable(entry.getKey(), userNames, entry.getValue().isPlay(),
                        entry.getValue().getPlayCount() == 3);
                hallList.add(hallTable);
            }
            initHallResponse.setTableList(hallList);
            singleSendMsg(player, initHallResponse.getClass().getSimpleName() + gson.toJson(initHallResponse));
        }
        //进入游戏房间
        if(request instanceof EnterTableRequest) {
            EnterTableRequest enterTableRequest = (EnterTableRequest) request;
            EnterTableResponse enterTableResponse;
            int tableNum = enterTableRequest.getTableNum();
            Table table = tableMap.get(tableNum);
            int playerCount = table.getPlayCount();
            if(playerCount < 3) {
                //player存入牌桌号和座位号
                player.setTableNum(tableNum);
                //生成座位号
                int newSeatNum = LandlordUtil.getSeatNum(tableNum, playerCount);
                player.setSeatNum(newSeatNum);
                //替换空player
                playerMap.put(newSeatNum, player);
                //table加入玩家，先洗好牌
                table.getPlayers().add(player);
                table.setPlayCount(playerCount + 1);
                table.setCards(LandlordUtil.getRandomCards());
                table.setTableNum(tableNum);
                //更新userName2Player
                userName2Player.put(enterTableRequest.getUserName(), player);
                //添加座位号-用户名集合
                HashMap<Integer, String> tablePlayers = refreshSeatNum2UserName(table);
                enterTableResponse = new EnterTableResponse(true, tablePlayers);
                batchSendMsg(enterTableResponse.getClass().getSimpleName() + gson.toJson(enterTableResponse),
                        table.getPlayers(), false);
                //通知游戏大厅房间信息刷新
                for(HallTable hallTable : hallList) {
                    if(hallTable.getTableNum() == table.getTableNum()) {
                        hallTable.getUserNames().add(enterTableRequest.getUserName());
                        hallTable.setPlay(false);
                        hallTable.setFull(hallTable.getUserNames().size() == 3);
                    }
                }
                RefreshHallResponse response = new RefreshHallResponse(hallList);
                batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                        userName2Player.values(), true);
            } else { //如果你慢人家一步，就会进入房间失败
                enterTableResponse = new EnterTableResponse(false, null);
                singleSendMsg(player, enterTableResponse.getClass().getSimpleName() + gson.toJson(enterTableResponse));
            }
        }
        //发送聊天信息
        if(request instanceof ChatMsgRequest) {
            ChatMsgRequest chatMsgRequest = (ChatMsgRequest) request;
            ChatMsgResponse chatMsgResponse = new ChatMsgResponse(chatMsgRequest.getChatFlag(),
                    chatMsgRequest.getUserName(), chatMsgRequest.getMsg(), chatMsgRequest.getTableNum());
            if(chatMsgRequest.getChatFlag() == 1) {
                batchSendMsg(chatMsgResponse.getClass().getSimpleName() + gson.toJson(chatMsgResponse),
                        userName2Player.values(),false);
            } else if(chatMsgRequest.getChatFlag() == 2) {
                batchSendMsg(chatMsgResponse.getClass().getSimpleName() + gson.toJson(chatMsgResponse),
                        tableMap.get(chatMsgRequest.getTableNum()).getPlayers(),false);
            }
        }
        //接受准备请求
        if(request instanceof ReadyRequest) {
            ReadyRequest readyRequest = (ReadyRequest) request;
            Table table = tableMap.get(player.getTableNum());
            int readyCount = table.getReadyCount();
            table.setReadyCount(readyCount + 1);
            if(readyRequest.isReady()) {
                ReadyResponse readyResponse = new ReadyResponse(true);
                singleSendMsg(player, readyResponse.getClass().getSimpleName() + gson.toJson(readyResponse));
            }
            //人满了，开始抢地主，随机选择地主并分发给同桌牌友
            Table tableNow = tableMap.get(player.getTableNum());
            if(tableNow.getReadyCount() == 3) {
                tableNow.setWait(false);
                tableNow.setRob(true);
                log.info("房间当前准备人数：" + tableNow.getReadyCount());
                List<Integer> totalCards = tableNow.getCards();
                HashMap<Integer, List<Integer>> cardsMap = new HashMap<>();
                //发牌
                cardsMap.put(0, totalCards.subList(0,17));
                cardsMap.put(1, totalCards.subList(17,34));
                cardsMap.put(2, totalCards.subList(34,51));
                List<Player> players = tableNow.getPlayers();
                //地主的三张牌
                ArrayList<Integer> threeCards = new ArrayList<>();
                threeCards.add(totalCards.get(51));
                threeCards.add(totalCards.get(52));
                threeCards.add(totalCards.get(53));
                tableNow.setThreeCards(threeCards);
//                int landlordNum = LandlordUtil.getRandomLandlord(tableNow.getTableNum());
                int landlordNum = 3; //为了方便测试，地主号固定为3
                for(int i=0; i<3; i++) {
                    //初始化玩家手持牌数
                    tableNow.getPlayersCardsCount().put(players.get(i).getSeatNum(), 17);
                    GrabLandlordResponse response = new GrabLandlordResponse(landlordNum,
                            refreshSeatNum2UserName(tableNow), threeCards, cardsMap.get(i));
//                    log.info("生成消息：" + gson.toJson(response));
                    singleSendMsg(players.get(i), response.getClass().getSimpleName() + gson.toJson(response));
                }
                tableMap.get(player.getTableNum()).setReadyCount(0); //准备数清零
            }
        }
        //取消准备请求
        if(request instanceof CancelReadyRequest) {
            CancelReadyRequest cancelReadyRequest = (CancelReadyRequest) request;
            Table oldTable = tableMap.get(player.getTableNum());
            int readyCount = oldTable.getReadyCount();
            oldTable.setReadyCount(readyCount - 1);
            if(cancelReadyRequest.isCancelReady()) {
                CancelReadyResponse response = new CancelReadyResponse(true);
                singleSendMsg(player, response.getClass().getSimpleName() + gson.toJson(response));
            }
        }
        //放弃抢地主，順移给下一位
        if(request instanceof GiveUpLandlordRequest) {
            Table table = tableMap.get(player.getTableNum());
            int passLandlordCount = table.getPassLandlordCount();
            passLandlordCount += 1;
            log.info("累计放弃地主次数: " + passLandlordCount);
            table.setPassLandlordCount(passLandlordCount);
            GiveUpLandlordRequest giveUpLandlordRequest = (GiveUpLandlordRequest) request;
            GiveUpLandlordResponse response = new GiveUpLandlordResponse(
                    LandlordUtil.getRightRivalSeatNum(giveUpLandlordRequest.getSeatNum(), table.getPlayers()));
            batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                    table.getPlayers(), false);
            if(table.getPassLandlordCount() == 3) {
                log.info("三次扔地主自动结束抢地主");
                table.setPassLandlordCount(0);
                int landlord = LandlordUtil.getRightRivalSeatNum(giveUpLandlordRequest.getSeatNum(), table.getPlayers());
                for(Integer key : table.getPlayersCardsCount().keySet()) {
                    if(key == landlord) {
                        table.getPlayersCardsCount().put(key, 20);
                    }
                }
                EndGrabLandlordResponse response1 = new EndGrabLandlordResponse(
                        landlord, table.getThreeCards());
                batchSendMsg(response1.getClass().getSimpleName() + gson.toJson(response1),
                        table.getPlayers(), false);
            }
        }
        //抢地主请求，同时发牌。
        if(request instanceof EndGrabLandlordRequest) {
            EndGrabLandlordRequest endGrabLandlordRequest = (EndGrabLandlordRequest) request;
            int landlord = endGrabLandlordRequest.getMeSeatNum();
            for(Integer key : tableMap.get(player.getTableNum()).getPlayersCardsCount().keySet()) {
                if(key == landlord) {
                    tableMap.get(player.getTableNum()).getPlayersCardsCount().put(key, 20);
                }
            }
            EndGrabLandlordResponse response = new EndGrabLandlordResponse(landlord, tableMap.get(player.getTableNum()).getThreeCards());
            batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                    tableMap.get(player.getTableNum()).getPlayers(), false);
        }
        //地主申请赌注加倍，转发给其余两个玩家
        if(request instanceof LandlordMultipleWagerRequest) {
            LandlordMultipleWagerRequest wagerRequest = (LandlordMultipleWagerRequest) request;
            Table table = tableMap.get(player.getTableNum());
            int wagerMultipleNum = wagerRequest.getMultipleNum();
            table.setWagerMultipleNum(wagerMultipleNum); //存储加倍数
            if(wagerMultipleNum == 1) { //不加倍，其他玩家也不需要反馈
                MultipleWagerResponse response = new MultipleWagerResponse(1);
                batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                        table.getPlayers(), false);
            } else {
                LandlordMultipleWagerResponse response = new LandlordMultipleWagerResponse(wagerMultipleNum);
                batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response), 
                        table.getPlayers(), true);
            }
        }
        //玩家对地主提议的加倍的反馈
        if(request instanceof MultipleWagerRequest) {
            MultipleWagerRequest wagerRequest = (MultipleWagerRequest) request;
            MultipleWagerResponse response;
            Table table = tableMap.get(player.getTableNum());
            int answerNum = table.getAnswerMultipleNum();
            answerNum++;
            int agreedResult = table.getAgreedMultipleResult();
            agreedResult += wagerRequest.getAgreed();
            table.setAnswerMultipleNum(answerNum);
            table.setAgreedMultipleResult(agreedResult);
            if(table.getAnswerMultipleNum() == 2) {
                if(table.getAgreedMultipleResult() < 2) {
                    response = new MultipleWagerResponse(1);
                } else {
                    response = new MultipleWagerResponse(table.getWagerMultipleNum());
                }
                batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                        table.getPlayers(), false);
                tableMap.get(player.getTableNum()).setAgreedMultipleResult(0);
                tableMap.get(player.getTableNum()).setWagerMultipleNum(1);
                tableMap.get(player.getTableNum()).setAnswerMultipleNum(0);
            }
        }
        //出牌请求，每出一次牌，在总表中记录，并群发给房间
        if(request instanceof CardsOutRequest) {
            CardsOutRequest cardsOutRequest = (CardsOutRequest) request;
            CardsOutResponse response;
            Table table = tableMap.get(player.getTableNum());
            //出牌表
            HashMap<Integer, Integer> throwOutCards = table.getThrowOutCards();
            //table持有最后的一组出牌
            List<Integer> lastCardsOut = table.getLastCardsOut();
            //每个玩家手中持有的牌数
            HashMap<Integer, Integer> playersCardsCount = table.getPlayersCardsCount();
            int continuousPass = table.getContinuousPass();
            if(cardsOutRequest.isPass()) {
                continuousPass = continuousPass + 1;
                if(continuousPass >= 2) {
                    response = new CardsOutResponse(cardsOutRequest.isPass(), true, cardsOutRequest.getFromSeatNum(),
                            cardsOutRequest.getToSeatNum(),lastCardsOut, throwOutCards, playersCardsCount);
                    table.setContinuousPass(0);
                } else {
                    response = new CardsOutResponse(cardsOutRequest.isPass(), false, cardsOutRequest.getFromSeatNum(),
                            cardsOutRequest.getToSeatNum(),lastCardsOut, throwOutCards, playersCardsCount);
                    table.setContinuousPass(continuousPass);
                }
            } else {
                table.setContinuousPass(0);
                table.setLastCardsOut(cardsOutRequest.getCardsOut());
                //更新顶上的出牌表
                for(int i=0; i<cardsOutRequest.getCardsOut().size(); i++) {
                    int tmp = throwOutCards.get(cardsOutRequest.getCardsOut().get(i) % 20);
                    tmp++;
                    throwOutCards.put(cardsOutRequest.getCardsOut().get(i) % 20, tmp);
                }
                //该玩家出牌了，更新他的持牌数
                int newCount = playersCardsCount.get(cardsOutRequest.getFromSeatNum()) - cardsOutRequest.getCardsOut().size();
                playersCardsCount.put(cardsOutRequest.getFromSeatNum(), newCount);
                response = new CardsOutResponse(cardsOutRequest.isPass(), false, cardsOutRequest.getFromSeatNum(),
                        cardsOutRequest.getToSeatNum(), cardsOutRequest.getCardsOut(), throwOutCards, playersCardsCount);
            }
            batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                    table.getPlayers(), false);
        }
        //有玩家的牌出完了
        if(request instanceof EndGameRequest) {
            EndGameRequest endGameRequest = (EndGameRequest) request;
            Table table = tableMap.get(player.getTableNum());
            int winnerSeatNum = endGameRequest.getWinnerSeatNum();
            //赢了之后的分数和战绩全部提交到assist服务器中
            EndGameResponse response = new EndGameResponse(winnerSeatNum);
            batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                    table.getPlayers(), false);
        }
        //退出游戏房间
        if(request instanceof ExitSeatRequest) {
            ExitSeatRequest exitSeatRequest = (ExitSeatRequest) request;
            int seatNum = exitSeatRequest.getYourSeatNum();
            Player player = playerMap.get(seatNum);
            Table thisTable = tableMap.get(player.getTableNum());
            thisTable.getPlayers().remove(player);
            int count = thisTable.getPlayCount();
            thisTable.setPlayCount(count - 1);
            thisTable.setPlay(false);
            thisTable.setRob(false);
            thisTable.setWait(true);
            //通知游戏房间里其他玩家
            ExitSeatResponse response = new ExitSeatResponse(player.getUserName(), seatNum, refreshSeatNum2UserName(thisTable));
            batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                    thisTable.getPlayers(), true);
            //通知游戏大厅里所有玩家有人退出房间
            for(HallTable hallTable : hallList) {
                if(hallTable.getTableNum() == thisTable.getTableNum()) {
                    hallTable.setFull(false);
                    hallTable.setPlay(false);
                    hallTable.getUserNames().remove(player.getUserName());
                }
            }
            playerMap.remove(seatNum);
            RefreshHallResponse response1 = new RefreshHallResponse(hallList);
            batchSendMsg(response1.getClass().getSimpleName() + gson.toJson(response1),
                    userName2Player.values(), true);

        }
        //退出游戏大厅
        if(request instanceof ExitHallRequest) {
            ExitHallRequest exitHallRequest = (ExitHallRequest) request;
            String userName = exitHallRequest.getUserName();
            userName2Player.remove(userName);
        }
    }

    /**
     * 退出游戏或者掉线，需要同时退出游戏房间和游戏大厅
     */
    public void exitOrException() {
        playerMap.remove(player.getSeatNum());
        Table thisTable = tableMap.get(player.getTableNum());
        thisTable.getPlayers().remove(player);
        int count = thisTable.getPlayCount();
        thisTable.setPlayCount(count - 1);
        thisTable.setPlay(false);
        thisTable.setRob(false);
        thisTable.setWait(true);
        //通知游戏房间里其他玩家
        ExitSeatResponse response = new ExitSeatResponse(player.getUserName(), player.getSeatNum(), refreshSeatNum2UserName(thisTable));
        batchSendMsg(response.getClass().getSimpleName() + gson.toJson(response),
                thisTable.getPlayers(), true);
        //通知游戏大厅里所有玩家有人退出房间
        for(HallTable hallTable : hallList) {
            if(hallTable.getTableNum() == thisTable.getTableNum()) {
                hallTable.setFull(false);
                hallTable.setPlay(false);
                hallTable.getUserNames().remove(player.getUserName());
            }
        }
        RefreshHallResponse response1 = new RefreshHallResponse(hallList);
        batchSendMsg(response1.getClass().getSimpleName() + gson.toJson(response1),
                userName2Player.values(), true);
        userName2Player.remove(player.getUserName());
    }

    /**
     * 群发消息
     */
    private void batchSendMsg(String sendMsg, Collection<Player> players, boolean excludeMyself) {
        for(Player player : players) {
            if(excludeMyself && player == this.player) continue;
            singleSendMsg(player, sendMsg);
        }
    }

    /**
     * 私发信息
     */
    private void singleSendMsg(Player player, String sendMsg) {
        if(player.getChannel() != null) {
            player.getChannel().writeAndFlush(sendMsg + Constant.LINE_SEPARATOR);
            log.info("向" + player.getUserName() + "发送消息："  + sendMsg);
        }
    }

    /**
     * 返回table中的座位号-用户名map
     */
    private HashMap<Integer, String> refreshSeatNum2UserName(Table table) {
        HashMap<Integer, String> tablePlayers = new HashMap<>();
        for(Player player : table.getPlayers()) tablePlayers.put(player.getSeatNum(), player.getUserName());
        return tablePlayers;
    }

    /**
     * 验证用户名和解密后的密码
     */
    private boolean loginAuthentication(String username, String password) {
        for(Map.Entry<String, String> entry : Constant.userName2Password.entrySet()) {
            if(entry.getKey().equals(username) && entry.getValue().equals(EncryptUtil.passwordDecryptDES(password))) return true;
        }
        return false;
    }
}
