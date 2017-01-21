package chatClient;

import Model.Message;

/**
 * Created by 付旭东 on 2017/1/6.
 */

public interface HanderMessage {
     String getSolveType();
     void sendMessage();
     boolean  solveMessage(Message message);
}
