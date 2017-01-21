package Model;

import java.util.ArrayList;

/**
 * Created by 付旭东 on 2017/1/4.
 */

public class Group {
    public int id;

    public String name;

    public String createdTime;

    public String sign;

    public ArrayList<Integer> users;

    public Group() {
        users=new ArrayList<>();
    }
}
