package Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import Model.Group;
import Model.LocalUser;
import Model.Message;
import Model.User;
import chatClient.ConnectionClient;

/**
 * Created by 付旭东 on 2017/1/4.
 */

public class DbManager extends SQLiteOpenHelper
{

    //我需要在表上添加主键信息
    /**
     * 用户的联系人列表信息
     */
    String friendsTable="create table friends(user int ,friend int,version int," +
            "primary key(user,friend))";
    /**
     * 用户参加的群的信息
     */
    String groups="create table groups(groupId int ,localId int, " +
            "name char[30],createdTime char[25],sign char[30],version int," +
            "primary key(groupId,localId)" +
            ")";
    /**
     * 群的实体信息
     */
    String GroupEntry="create table groupEntry(localUserId int," +
            "groupId int,userId int,version int,condition char)";
    /***
     * 用户的好友信息
     */
    String user="create table user(id int primary key,name char[20]," +
            " sex char[10],sign char[30], " +
            "birthDay char[25],college char[25]," +
            " company char[25],place char[30])";
    /**
     * 与好友的聊天信息
     */
    String communication="create table communication(localUser int," +
            "commType char,userFrom int ,message nchar[255],time nchar[30]," +
            "primary key(localUser)" +
            ")";
    /**
     * 本地登录的用户记录
     */
    String LocalUser="create table LocalUser(localUserId int primary key," +
            "passWord char[20])";

    SharedPreferences local;
    Context myContext;
    SQLiteDatabase database;

    String localUserIdMark="localUserId";
    String localUserPassCode="localUserPassCode";


    private static DbManager dbManager;

    ConnectionClient client;


    public void addUser(int id,String name,String birthday,String company,String sex,
                        String where,String sign ,String colloge)
    {
        Cursor reslut= database.rawQuery("select * from friends where user="+getLocalUserId()+
                " and friend="+id,null);
        Cursor userCondition=database.rawQuery("select * from user where id="+id,null);

        String addfriend=String.format("insert into friends " +
                "values(%d,%d,1)".toLowerCase(),getLocalUserId(),id) ;

        String sql=String.format("insert into user values(%d,'%s','%s','%s'," +
                "'%s','%s','%s','%s')".toLowerCase(),id, name,sex,sign,birthday,colloge,company,where);

        String update=String.format("update user set name='%s',birthDay='%s',company='%s'," +
                        "sex='%s', place='%s',sign='%s',college='%s' where id=%d".toLowerCase()
                ,name,birthday,company,sex,where,sign,colloge,id);



        if(reslut.getCount()==0 && userCondition.getCount()==0)
        {
           database.execSQL(addfriend);
            database.execSQL(sql);
        }
        if(reslut.getCount()!=0 && userCondition.getCount()==0)
        {
            database.execSQL(sql);
        }
        if(reslut.getCount()==0 && userCondition.getCount()!=0)
        {
            database.execSQL(addfriend);
            database.execSQL(update);
        }
        if(reslut.getCount()!=0 && userCondition.getCount()!=0)
        {
            database.execSQL(update);
        }
    }


    /**
     * 检查服务器和客户端的联系人一致情况
     */
    public void checkData()
    {
        int verison= local.getInt("maxVersion",0);
        String request="<message><action>dataRequest</action><th>" +getTh()+
                "</th><version>" +verison+
                "</version><from>" +getLocalUserId()+
                "</from></message>";
        Message send=new Message(request);

        Message message=client.sendMessageWithConfirm(send);
        Node value=message.getValue();
        NodeList info= value.getChildNodes();
        if(info!=null)
            for(int i=0;i<info.getLength();i++)
            {
                Node node=info.item(i);
                switch (node.getNodeName())
                {
                    case"user":
                        NamedNodeMap temp=node.getAttributes();
                         String condition= temp.getNamedItem("condition").getNodeValue();
                        int id=Integer.valueOf(node.getTextContent());
                        userChange(id,condition);
                        break;
                    case "group":
                        if(node.hasAttributes())
                        {
                            NamedNodeMap groupTemp=node.getAttributes();
                            String con= groupTemp.getNamedItem("condition").getNodeValue();
                            int groupId=Integer.valueOf(node.getTextContent());
                            groupChange(groupId,con);
                        }
                        else
                        {
                            NamedNodeMap group=node.getAttributes();
                            int groupId= Integer.valueOf(group.getNamedItem("groupId").getNodeValue());
                            groupUserChange(groupId,node.getChildNodes());
                        }
                        break;
                }
            }
    }


    private  void userChange(int id,String condition)
    {
        switch (condition)
        {
            case"add":
            case "changed":
                String request="<message>" +
                        "<action>userInfoUpdate</action>" +
                        "<from>" +getLocalUserId()+
                        "</from><th>" +getTh()+
                        "</th><value>" +"<friendId>"+id+
                        "</friendId></value></message>";
                Message message=new Message(request);
                ConnectionClient client=ConnectionClient.getInstance(myContext);
                Message result= client.sendMessageWithConfirm(message);
                Node temp=result.getValue();
                String name="",sex="", company="",college="",birthDay="",sign="", where="";
                for(int i=0;i<temp.getChildNodes().getLength();i++)
                {
                    Node node=temp.getChildNodes().item(i);
                    switch (node.getNodeName())
                    {
                        case "name":
                            name=node.getTextContent();
                            break;
                        case"sex":
                            sex=node.getTextContent();
                            break;
                        case"company":
                            company=node.getTextContent();
                            break;
                        case"college":
                            college=node.getTextContent();
                            break;
                        case"birthDay":
                            birthDay=node.getTextContent();
                            break;
                        case"sign":
                            sign=node.getTextContent();
                            break;
                        case"where":
                            where=node.getTextContent();
                            break;
                    }
                }
                addUser(id,name,birthDay,company,sex,where,sign,college);
                break;
            case "deleted":
                String delete="delete from user where friend="+id;
                database.execSQL(delete);
                String deleteInfo="delete from friends where friend="+id;
                database.execSQL(deleteInfo);
                break;
        }
    }
    private  void groupChange(int id,String condition)
    {

    }

    private void groupUserChange(int id,NodeList change)
    {

    }

    int getTh()
    {
        Random suiji=new Random();
        Calendar calendar=Calendar.getInstance();
        suiji.setSeed(calendar.getFirstDayOfWeek()*calendar.getTimeInMillis());
        return suiji.nextInt(99999999);
    }

    private int getDataVersion()
    {
        return 0;
    }


    public DbManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context,name,factory,version);
        myContext=context;
        local=myContext.getSharedPreferences("currentLocalUser",Context.MODE_PRIVATE);
        client=ConnectionClient.getInstance(context);
        database=getWritableDatabase();
    }


    public static DbManager getDbManager(Context context)
    {
        if(dbManager==null)
        {
            dbManager=new DbManager(context,"DataBase",null,1);
            return dbManager;
        }
        else
        {
            return  dbManager;
        }
    }


    public int getLocalUserId() {
        return local.getInt(localUserIdMark,0);
    }


    /**
     * 先检查 是否包含这个本地账户，包含的话就不添加，不包含就添加进去
     * @param id
     * @param password
     */
    public void AddLocalUser(int id,String password)
    {
        String sql="select * from LocalUser where LocalUserId="+id;
        Cursor res=database.rawQuery(sql,null);
        if(res.getCount()==0)
        {
            String cmd="insert into LocalUser(localUserId,passWord) values('"+id+"','"+password+"')";
            database.execSQL(cmd);
        }
    }



    public void setCurrentLocalUser(int localUserId,String passWord) {
        SharedPreferences.Editor bianji= local.edit();
        bianji.putInt(localUserIdMark,localUserId);
        bianji.putString(localUserPassCode,passWord);
        bianji.commit();
    }

    public Model.LocalUser getLocalUser()
    {
        int id=local.getInt(localUserIdMark,0);
        String pass=local.getString(localUserPassCode,"");
        Model.LocalUser user=new LocalUser(myContext,id,pass);
        return user;
    }

    /**
     * 根据当前用户来获取用户的好友
     * @param id
     * @return
     */
    public User getUser(int id)
    {
        String cmd="select * from user where id= "+String.valueOf(id)+
                " and id in(select friend from friends where id= "
                +String.valueOf(getLocalUserId())+" )";
        Cursor result= database.rawQuery(cmd,null);
        User user =new User();
        user.id=id;

        if(result.moveToNext()) {
            user.name = result.getString(result.getColumnIndex("name"));
            user.colloge = result.getString(result.getColumnIndex("college"));
            user.company = result.getString(result.getColumnIndex("company"));
            user.place = result.getString(result.getColumnIndex("place"));
            user.sign = result.getString(result.getColumnIndex("sign"));
        }
        return user;
    }

    /**
     * 获取当前用户的所有联系人
     * @return
     */
    public ArrayList<User> getUserList()
    {
        String cmd="select friend from friends where user ="+getLocalUserId();
        Cursor result=database.rawQuery(cmd,null);
        ArrayList<Integer> userIds=new ArrayList<>();

        while( result.moveToNext()) {
           int id = result.getInt(result.getColumnIndex("friend"));
           userIds.add(id);
       }
        ArrayList<User> users=new ArrayList<>();
        for(int id:userIds) {
            users.add(getUser(id));
        }
        return  users;
    }


    public Group getGroup( int id)
    {
        String cmd="select * from groups where localId= "+getLocalUserId()+"and id="+id;
        Cursor result=database.rawQuery(cmd,null);
        result.moveToFirst();
        Group group=new Group();
        group.id=id;
        group.createdTime=result.getString(result.getColumnIndex("createdTime"));
         group.name=result.getString(result.getColumnIndex("name"));
        group.sign=result.getString(result.getColumnIndex("sign"));
        return group;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        database=db;
        db.execSQL(friendsTable);
        db.execSQL(groups);
        db.execSQL(user);
        db.execSQL(communication);
        db.execSQL(LocalUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
