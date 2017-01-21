package chatClient;

import java.util.TreeMap;

import Model.Message;

/**
 * Created by 付旭东 on 2017/1/4.
 */

public class SolveMessage {

    TreeMap<String,HanderMessage> solve=new TreeMap<String,HanderMessage>();

    ConnectionClient receiveMessage;

    public SolveMessage(ConnectionClient receive)
    {
        this.receiveMessage=receive;
    }

    public void registeHander(HanderMessage hander) {
        solve.put(hander.getSolveType(),hander);
    }

    /**
     *
     * @param message
     */
    void solve(Message message)
    {
       solve.get(message).solveMessage(message);
    }

}
