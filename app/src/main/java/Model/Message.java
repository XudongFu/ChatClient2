package Model;

import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by 付旭东 on 2017/1/4.
 */

public class Message {
    int from;
    int to;
    String action;
    Node value;
    String data;
    public int th;

    public Message(String data) {
        this.data=data;

        try
        {
            DocumentBuilderFactory factor=DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=factor.newDocumentBuilder();
            Document doc=builder.parse(new InputSource(new StringReader(data)));
            Element root=doc.getDocumentElement();
            NodeList child=root.getChildNodes();
           if(child!=null)
           for(int i=0;i<child.getLength();i++)
           {
                switch (child.item(i).getNodeName())
                {
                    case "th":
                        th=Integer.valueOf(child.item(i).getTextContent()) ;
                        break;
                    case"value":
                        value=child.item(i);
                        break;
                    case"action":
                        action=child.item(i).getTextContent();
                        break;
                    default:
                        break;
                }
           }

        }
        catch (Exception e)
        {
            throw  new RuntimeException(e.getMessage());
        }
    }

    public   Message()
    {

    }

    public String getAction()
    {
        if(action!=null && !action.equals(""))
        return  action;
        return "";
    }

    public Node getValue()
    {
        return value;
    }


    public String toString()
    {
        return data;
    }
}
