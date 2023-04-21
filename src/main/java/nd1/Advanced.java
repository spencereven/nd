package nd1;


import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static java.lang.StrictMath.acos;
import static java.lang.StrictMath.pow;

public class Advanced {
    static HashMap<Integer,Double> map;
    static int[][] FirstTime;
    static int[][] FoundTime;
    static int[][] inRange;
    static int[][] hadFound;
    static int second_found_count= 0;

    private static  int AREA_SIZE;
    private static  int GRID_SIZE;
    private static ArrayList<ArrayList<double[]>> nodePositions;

    public static void main(String[] args) {

        //一般记录的表的初始化
        int nums = 100;
        int range =1000;
        int gridSize = 10;

        AREA_SIZE = range;
        GRID_SIZE = gridSize;

        FirstTime = new int[nums][nums];
        FoundTime = new int[nums][nums];
        inRange = new int[nums][nums];
        hadFound = new int[nums][nums];
        Found_range(5,nums,100,range,gridSize,0.5,0,0.05,5000,0.03);


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // 创建一个 JFrame 实例
                JFrame frame = new JFrame("Node Movement Visualization Advanced");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // 将 NodeDisplay 面板添加到 JFrame 中
                NodeDisplay nodeDisplay = new NodeDisplay(nodePositions);
                frame.add(nodeDisplay);
                frame.setSize(AREA_SIZE + 16, AREA_SIZE + 39);
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
                frame.setVisible(true);
            }
        });













    }

    private static int cal_out_range_nodes(List<Node> nodeList, int nums, int range) {
        int res = 0;
        for (Node node : nodeList) {
            if (node.x < 0 || node.x > range || node.y < 0 || node.y > range) res++;
        }
        return res;
    }

    public static void Found_range(int N, int nums, double R, int range, int gridSize,
    double threshold1, double threshold2, double vl, int len, double dc){

        double[] res = new double[N];
        double[] res1 = new double[N];
        double[] res2 = new double[N];
        double[] res3 = new double[N];
        int[] res4 = new int[N];


        for (int i = 0; i < N; i++) {
            nodePositions = new ArrayList<>();
            List<Node> nodeList = gn(nums,range,gridSize, dc, vl,len+100);
            //System.out.println(nodeList.size());
            initMap();
            initTable();
            int beginSlot = foundMinSlot(nodeList);
            int curSlot = beginSlot;
            int endSlot = beginSlot+len;
            recordNodePositions((ArrayList<Node>) nodeList, nodePositions);

            while (curSlot <= endSlot){

                for (int j = 0; j < nodeList.size()-1; j++) {
                    for (int k = j+1; k < nodeList.size(); k++) {
                        Node a = nodeList.get(j), b = nodeList.get(k);
                        if (isIn(a.x,b.x,a.y,b.y,R,range)){
                            if (FirstTime[j][k] == -1) FirstTime[j][k] = curSlot;
                            if (inRange[j][k] == -1) inRange[j][k] = 1;
                        }
                        if (isWake(curSlot,a) && isWake(curSlot,b)) {
                            //如果是第一次发现的话 则要么是别人推荐的,要么就是自己底层的pairwise
                            if (isFirstFound(a, b)) {
                                if (isIn(a.x, b.x, a.y, b.y, R, range)) {
                                    if (FoundTime[j][k] == -1) FoundTime[j][k] = curSlot;
                                    if (hadFound[j][k] == -1) hadFound[j][k] = 1;
                                    firstFound(a, b, curSlot, endSlot);
                                }
                            }else{
                                if (isIn(a.x,b.x,a.y,b.y,R,range)){
                                    secondFound(a,b,curSlot,R,threshold1,endSlot);
                                }
                            }
                        }
                    }
                }
                curSlot++;
                un(nodeList,curSlot,beginSlot,vl,gridSize);
                recordNodePositions((ArrayList<Node>) nodeList, nodePositions);
            }
            res[i] = calDelay();
            res1[i] = calAvgDutyCycle(endSlot,nodeList);
            res2[i] = cal_Found_Range1(nodeList,R,range);
            res3[i] = cal_Dis_percentage();
            res4[i] = cal_out_range_nodes(nodeList,nums,range);
        }
        printFunction(res,res1,res2,res3,res4);
    }


    public static void printFunction(double[] res, double[] res1, double[] res2, double[] res3,int[] res4){
        System.out.println("Discovery Delay");
        for (int i = 0; i <  res.length; i++) {
            System.out.println(res[i]);
        }
        System.out.println("Avg DutyCycle");
        for (int i = 0; i <  res.length; i++) {
            System.out.println(res1[i]);
        }
        System.out.println("Dis Probability");
        for (int i = 0; i <  res.length; i++) {
            System.out.println(res2[i]);
        }
        System.out.println("Dis Percentage");
        for (int i = 0; i <  res.length; i++) {
            System.out.println(res3[i]);
        }
        System.out.println("Out of range nodes");
        for (int i = 0; i <  res.length; i++) {
            System.out.println(res4[i]);
        }

        System.out.println(second_found_count+"-");
    }

    public static double calAvgDutyCycle(int endSlot, List<Node> nodeList){
        double res = 0;
        for (Node curNode : nodeList) {
            double curNum = 0;
            for (Integer slot : curNode.slots) {
                if (slot > endSlot) continue;
                curNum++;
            }
            res += curNum/(double)endSlot;
        }
        return res/(double) nodeList.size();
    }

    private static double cal_Dis_percentage() {
        double inRangeSum = 0, hadFoundSum = 0;
        for (int i = 0; i < hadFound.length; i++) {
            for (int j = 0; j < hadFound[0].length; j++) {
                if (hadFound[i][j] == 1) hadFoundSum++;
                if (inRange[i][j] == 1) inRangeSum++;
            }
        }
        return hadFoundSum/inRangeSum;
    }

    public static double cal_Found_Range1(List<Node> nodeList, double R, double range){
        double res = 0;
        for (int i = 0; i < nodeList.size()-1; i++) {
            double numInRange = 0;
            double trueNeighbor =0;
            for (int j = i+1; j < nodeList.size(); j++) {
                Node a = nodeList.get(i), b = nodeList.get(j);
                if (isIn(a.x,b.x,a.y,b.y,R,range)){
                    numInRange++;
                    if (a.neighbors.contains(b)) trueNeighbor++;
                }
            }
            if (numInRange == 0) continue;
            res +=  (double) trueNeighbor/(double) numInRange;
        }
        return res/(double)nodeList.size();
    }


    public static List<Node> gn(int nums, int range, int gridSize, double dc, double vl, int len){
        List<Node> results = new ArrayList<>();
        int id = 1;
        Random random = new Random();
        for (int i = 0; i < nums; i++) {
            //System.out.println("--");
            int col = random.nextInt(range/gridSize);
            int row = random.nextInt(range/gridSize);

            double xStart = col * gridSize;
            double yStart = row * gridSize;

            double x = xStart;
            double y = yStart;

            int edge = random.nextInt(4);
            switch (edge) {
                case 0:  // 底边
                    x = xStart + random.nextDouble() * gridSize;
                    y = yStart;
                    break;
                case 1:  // 右边
                    x = xStart + gridSize;
                    y = yStart + random.nextDouble() * gridSize;
                    break;
                case 2:  // 顶边
                    x = xStart + random.nextDouble() * gridSize;
                    y = yStart + gridSize;
                    break;
                default:  // 左边
                    x = xStart;
                    y = yStart + random.nextDouble() * gridSize;
                    break;
            }

            double dir = edge * Math.PI / 2; // 方向初始化，按照边所在方向进行初始化

            int initSlot = (int)(Math.random()*100);
            results.add(new Node(dc,initSlot,id++,x,y,vl,dir,nums,len));
        }

        return results;
    }

    public static void initMap(){
        map = new HashMap<Integer, Double>();
        map.put(1,156.45);
        map.put(2,129.84);
        map.put(3,106.83);
        map.put(4,85.68);
        map.put(5,65.66);
        map.put(6,46.36);
        map.put(7,27.50);
        map.put(8,8.89);
        map.put(9,-9.63);
        map.put(10,-28.25);
        map.put(11,-47.1189);
        map.put(12,-66.45);
    }

    public static void initTable(){
        for (int i = 0; i < FirstTime.length; i++) {
            for (int j = 0; j < FirstTime[0].length; j++) {
                FirstTime[i][j] = -1;
                FoundTime[i][j] = -1;
                hadFound[i][j] = -1;
                inRange[i][j] = -1;
            }
        }
    }

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

    private static int foundMinSlot(List<Node> nodeList) {
        int res = Integer.MAX_VALUE;
        for (Node node : nodeList) {
            res = node.initialSlot < res ? node.initialSlot : res;
        }
        return res;
    }

    public static boolean isIn(double x1, double x2, double y1, double y2,double R,double range){
        if (x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0 || x1 > range || x2 > range || y1 > range || y2 > range) return false;
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        return distance <= R;
    }

    private static boolean isWake(int curSlot, Node cur) {
        return cur.slots.contains(curSlot);
    }

    private static boolean isFirstFound(Node a, Node b) {
        return !a.neighbors.contains(b) && !b.neighbors.contains(a);
    }

    public static void firstFound(Node a, Node b, int curSlot, int endSlot){
        if (a.neighbors.contains(b) || b.neighbors.contains(a)) return;

        int minAddSlot = Integer.MAX_VALUE;

        for (Integer slot : a.slots) {
            if (slot >= curSlot && slot <= endSlot){
                minAddSlot = Math.min(minAddSlot,slot);
            }
        }
        b.slots.add(minAddSlot);
        b.newAddSlots.add(minAddSlot);

        minAddSlot = Integer.MAX_VALUE;

        for (Integer slot : b.slots) {
            if (slot >= curSlot && slot <= endSlot){
                minAddSlot = Math.min(minAddSlot,slot);
            }
        }
        a.slots.add(minAddSlot);
        a.newAddSlots.add(minAddSlot);

        //加入各自的邻居表
        a.neighbors.add(b);
        b.neighbors.add(a);
    }

    public static void secondFound(Node a, Node b, int curSlot, double r, double R, double threshold){
        //second_found_count++;

        if (!a.neighbors.contains(b) || !b.neighbors.contains(a)) return;
        //1.只是添加邻居的未来激活时刻
        for (Node node : a.neighbors) {
            //判断a是否将node推荐给b
            if (isRecommend(a,b,node,R,threshold)) {
                if (!b.neighbors.contains(node)) {
                    int minAddSlot = Integer.MAX_VALUE;
                    for (Integer slot : node.slots) {
                        if (slot > curSlot){
                            minAddSlot = Math.min(minAddSlot,slot);
                        }
                    }
                    b.slots.add(minAddSlot);
                }
            }
        }
        for (Node node : b.neighbors) {
            if (isRecommend(b,a,node,R,threshold)) {
                second_found_count++;
                if (!a.neighbors.contains(node)) {
                    int minAddSlot = Integer.MAX_VALUE;
                    for (Integer slot : node.slots) {
                        if (slot > curSlot){
                            minAddSlot = Math.min(minAddSlot,slot);
                        }
                    }
                    a.slots.add(minAddSlot);
                }
            }
        }
    }

    private static boolean isRecommend(Node a, Node b, Node node, double R, double threshold1) {
        //1.计算a和node 以及b和node的共同邻居结点个数
        int com1 =0, com2 =0;
        for (Node cur : a.neighbors) {
            if (node.neighbors.contains(cur)) com1++;
        }
        for (Node cur : b.neighbors) {
            if (node.neighbors.contains(cur)) com2++;
        }
        System.out.println(com1+"---"+com2);
        //2.len1+len2 <= R的话直接推荐
        if (com1 ==0 || com2 ==0) return false;
        if (com1 > 12 || com2 > 12) return true;
        double len1 = map.get(com1), len2 = map.get(com2);
        if (len1 + len2 <= R) return true;
        //3.如果len1 len2 R不能组成三角形则不推荐
        if (!isTriangle(len1,len2,R)) return false;
        //4.剩下按照概率公式推荐
        double res = calculate(len1,len2,R);
        if (res >= threshold1) return true;
        //if (isApproaching(b,node,b.dir,node.dir)) return true;
        return false;
    }

    public static boolean isTriangle(double a, double b, double c) {
        if (a <= 0 || b <= 0 || c <= 0) { // 三边必须大于 0
            return false;
        }
        if (a + b <= c || a + c <= b || b + c <= a) { // 两边之和必须大于第三边
            return false;
        }
        return true;
    }

    public static double calculate(double l1, double l2, double r) {
        double numerator = pow(l1, 2) + pow(l2, 2) - pow(r, 2);
        double denominator = 2 * l1 * l2;
        double angleInDegrees = acos(numerator / denominator) * (1.0/Math.PI);
        System.out.println(angleInDegrees+"angle");
        return angleInDegrees;
    }

    public static void un(List<Node> nodeList, int curSlot, int beginSlot, double vl, int gridSize){
        Random random = new Random();
        for (Node node : nodeList) {
            double dx = node.v * Math.cos(node.dir);
            double dy = node.v * Math.sin(node.dir);

            double newX = node.x + dx;
            double newY = node.y + dy;

            int col = (int) (newX / gridSize);
            int row = (int) (newY / gridSize);

            double xStart = col * gridSize;
            double yStart = row * gridSize;

            // 检查节点是否超出边界
            if (newX >= xStart && newX <= xStart + gridSize && newY >= yStart && newY <= yStart + gridSize) {
                node.x = newX;
                node.y = newY;
            } else {
                // 如果超出边界，保持原位置
                node.x = node.x;
                node.y = node.y;

                // 随机选择一个新方向
                int newDirection = random.nextInt(4);
                switch (newDirection) {
                    case 0:
                        node.dir = 0;
                        break;
                    case 1:
                        node.dir = Math.PI / 2;
                        break;
                    case 2:
                        node.dir = Math.PI;
                        break;
                    case 3:
                        node.dir = 3 * Math.PI / 2;
                        break;
                }
            }

        }
    }

    public static double calDelay(){
        int cnt = 0;
        double res = 0;
        for (int i = 0; i < FirstTime.length-1; i++) {
            for (int j = i+1; j < FirstTime[0].length; j++) {
                //System.out.println(FoundTime[i][j]+" "+FirstTime[i][j]);
                if (FirstTime[i][j] == -1 || FoundTime[i][j] == -1) continue;
                //System.out.println(FoundTime[i][j]+" "+FirstTime[i][j]);
                res += FoundTime[i][j]-FirstTime[i][j];
                cnt++;

            }
        }
        System.out.println(cnt+"---cnt");
        System.out.println(res+"---res");
        return res/cnt;
    }

}
