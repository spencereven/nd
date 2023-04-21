package nd1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author spencerReid
 * @date 2022年12月22日11:22
 */
public class Node {
    public List<int[]> choosePrimes;
    public HashSet<Integer> slots;
    public HashSet<Integer> newAddSlots;
    public int[] allPrimes;
    public int[] basicPrimes;
    public int id;
    public int initialSlot;
    public double x;
    public double y;
    //TTL对应的是Node和Node之间第一时刻成为邻居时 生成的过期最长时间
    int[][] TTL_Check;
    public double v;
    public double dir;
    double dc;
    //邻居表
    public HashSet<Node> neighbors;
    //由谁推荐过来的邻居表
    HashMap<Node, List<Node>> referredList;
    boolean flag;

    //某个时隙的激活信息
    public HashMap<Integer,IsTold> wakeList;

    //和其他节点的RSSI值
    public HashMap<Node, Double> rssiList;












    public Node(double dc, int initialSlot, int id, double x,
                double y, double v, double dir, int nums, int endSlot){
        wakeList = new HashMap<Integer, IsTold>();
        choosePrimes = new ArrayList<int[]>();
        allPrimes = getAllPrimes();
        this.id = id;
        this.dc = dc;
        this.initialSlot = initialSlot;
        this.basicPrimes = getPrimesDisco();
        this.slots = getDiscos();
        this.x = x;
        this.y = y;
        this.v = v;
        this.dir = dir;
        this.flag = true;
        this.rssiList = new HashMap<>();
        neighbors = new HashSet<Node>();
        referredList = new HashMap<Node, List<Node>>();
        newAddSlots = new HashSet<Integer>();
        TTL_Check = new int[nums+1][nums+1];


        //初始化IsTold
        for (int i = 0; i < endSlot; i++) {
            wakeList.put(i,new IsTold());
        }
    }

    //--------------------------------------------------------------------------------
    //1：底层Disco生成的所有slots(1w以内)
    public HashSet<Integer> getDiscos(){
        HashSet<Integer> results = new HashSet<Integer>();
        int[] primes = basicPrimes;
        int slot = initialSlot;
        while (slot <= 21000){
            if ((slot-initialSlot)%primes[0] == 0 || (slot-initialSlot)%primes[1] == 0){
                results.add(slot);
            }
            slot++;
        }
        return results;
    }
    //1: Disco获得质数对
    public int[] getPrimesDisco() {
        //先选择小的质数
        int a = getPrime(1.0/dc);
        int b = getPrime(1.0/(dc-1.0/a));
        choosePrimes.add(new int[]{a,b});
        for (int i = 0; i < 4; i++) {
            int small = getPrime(a);
            int big = getPrime(1.0/(dc-1.0/small));
            a = small;
            b = big;
            choosePrimes.add(new int[]{small,big});
        }
        return choosePrimes.get((int)(Math.random()*5));
    }
    //1：获取比质数c大最近的质数
    private int getPrime(double c) {
        int l = 0;
        int r = allPrimes.length-1;
        while (l < r){
            int mid = l+r>>1;
            if (allPrimes[mid] > c){
                r = mid;
            }else {
                l = mid+1;
            }
        }
        return allPrimes[l];
    }
    //1 获取所有12000以内的质数数组
    private int[] getAllPrimes(){
        int i;
        int j;
        int index = 0;
        int[] result = new int[2361];
        for ( i = 1; i < 21000; i++) {
            for ( j = 2; j < i; j++) {
                if (i % j == 0) break;
            }
            if (j >= i) result[index++] = i;
        }
        return result;
    }
    //--------------------------------------------------------------------------------

    public static void recordNodePositions(ArrayList<Node> nodes, ArrayList<ArrayList<double[]>> nodePositions) {
        if (nodePositions.isEmpty()) {
            for (int i = 0; i < nodes.size(); i++) {
                ArrayList<double[]> positions = new ArrayList<>();
                positions.add(new double[]{nodes.get(i).x, nodes.get(i).y});
                nodePositions.add(positions);
            }
        } else {
            for (int i = 0; i < nodes.size(); i++) {
                ArrayList<double[]> positions = nodePositions.get(i);
                positions.add(new double[]{nodes.get(i).x, nodes.get(i).y});
            }
        }
    }



    public static void main(String[] args) {

    }

}
