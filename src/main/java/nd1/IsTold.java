package nd1;


import java.util.HashMap;
import java.util.List;

public class IsTold {
    //判断这个slot是不是给别人推荐过来的
    public boolean isRecommend;
    //表示由谁在什么slot推荐过来的
    public HashMap<Node, Integer> whenRecommend;
    //表示在前面的slot推荐过来的 在SLOTt时刻的邻居表有什么
    public HashMap<Node, List<Node>> neiList;
    //置信度是否ok了 如果有一个节点判断ok了 那么这个时隙该节点就肯定激活了
    public boolean isDecided;
    //比如 20记录 23使用  那么就表示在23时刻激活的时候 是谁推荐的
    public HashMap<Integer, List<Node>> whoRecommend;

    public IsTold(){
        isRecommend = false;
        whenRecommend = new HashMap<Node,Integer>();
        neiList = new HashMap<Node,List<Node>>();
        isDecided = false;
        whoRecommend = new HashMap<Integer, List<Node>>();
    }

}
