package nd1;



import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import static java.lang.StrictMath.acos;
import static java.lang.StrictMath.pow;


public class Gpnd1 {
    static HashMap<Integer,Double> map;
    static int[][] FirstTime = new int[500][500];
    static int[][] FoundTime = new int[500][500];
    private static final int AREA_SIZE = 500;
    private static final int GRID_SIZE = 10;
    private static ArrayList<ArrayList<double[]>> nodePositions;

    public static void main(String[] args) {
        Found_range(1,40,100,500,10,0.5,0.62,0.05,5000,0.03);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // 创建一个 JFrame 实例
                JFrame frame = new JFrame("Node Movement Visualization GPND");
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

    public static void Found_range(int N, int nums, double R, int range,int gridSize, double threshold1,
                                   double threshold2, double vl, int len, double dc){
        double[] res = new double[N];

        for (int i = 0; i < N; i++) {
            nodePositions = new ArrayList<>();
            List<Node> nodeList = gn(nums,range,gridSize, dc, vl,len+100,R);

            //System.out.println(nodeList.size());

            initMap();
            initTable();
            int beginSlot = foundMinSlot(nodeList);
            int curSlot = beginSlot;
            int endSlot = beginSlot+len;
            recordNodePositions((ArrayList<Node>) nodeList, nodePositions);


            //2. 每次实验跑多少个slot
            while(curSlot <= endSlot){


                //3. 两两节点判断
                for (int j = 0; j < nodeList.size()-1; j++) {
                    for (int k = j+1; k < nodeList.size(); k++) {
                        Node a = nodeList.get(j), b = nodeList.get(k);
                        if (isIn(a.x,b.x,a.y,b.y,R,range)){
                            if (FirstTime[j][k] == -1) FirstTime[j][k] = curSlot;
                        }
                        //4. 判断该slot是否激活(包含pairwise 或者 middleware)
                        if (isWake(curSlot,a) && isWake(curSlot,b)){
                            //5. 判断是否为第一次接触(不能单独理解为发现)
                            if(isFirstFound(a,b)){
                                //5.1. 判断是否是别人推荐的(两者都是别人推荐的也不是不行)
                                IsTold isToldA = a.wakeList.get(curSlot), isToldB = b.wakeList.get(curSlot);
                                boolean isR1 = isToldA.isRecommend, isR2 = isToldB.isRecommend;
                                if (isR1 || isR2){
                                    boolean isAWake = false, isBWake = false;
                                    //5.1.1 根据置信度判断是否激活(a和b节点要单独判断 并不是说一个激活就ok 因为有可能两个都是被推荐情况)
                                    if (isR1){
                                        //5.1.2所有在此时刻给a节点推荐的节点带来信息的置信度都要计算(true表示有存在满足)
                                        if (calAllConfidence(a,b,curSlot,R,threshold2)){
                                            //5.1.3 则只能说明a节点可以激活
                                            isAWake = true;
                                        }else{
                                            //置信度不满足符合的话 则不需要在此处激活
                                            a.slots.remove(curSlot);
                                        }
                                    }
                                    if (isR2){
                                        if (calAllConfidence(b,a,curSlot, R,threshold2)){
                                            isBWake = true;
                                        }else{
                                            //置信度不满足符合的话 则不需要在此处激活
                                            b.slots.remove(curSlot);
                                        }
                                    }
                                    //只有两个节点都决定能激活才能进行下一步判断
                                    if (isAWake && isBWake){
                                        if (isIn(a.x,b.x,a.y,b.y,R,range)){
                                            if (FoundTime[j][k] == -1) FoundTime[j][k] = curSlot;
                                            firstFound(a,b,curSlot,endSlot);
                                        }
                                    }
                                }else{
                                    //5.2 如果不是别人推荐的话 那么就是正常pairwise底层处理
                                    if (isIn(a.x,b.x,a.y,b.y,R,range)){
                                        if (FoundTime[j][k] == -1) FoundTime[j][k] = curSlot;
                                        firstFound(a,b,curSlot,endSlot);
                                    }
                                }
                            }else{
                                //6. 则说明是第二次及以上次数发现(必然不是接受别的节点推荐)
                                if (isIn(a.x,b.x,a.y,b.y,R,range)){
                                    secondFound(a,b,curSlot,R,threshold1,endSlot);
                                }
                            }
                        }
                    }
                }
                curSlot++;
                un(nodeList,curSlot,beginSlot,vl,gridSize,R);
                recordNodePositions((ArrayList<Node>) nodeList, nodePositions);
                //System.out.println(nodeList.size());
            }
            res[i] = calDelay();
        }
        System.out.println("Discovery Delay");
        for (int i = 0; i <  res.length; i++) {
            System.out.println(res[i]);
        }
    }


    public static double calculateRSSI(double distance, double communicationRange, double txPower, double pathLossExponent) {
        if (distance <= 0) return 0;
        if (distance > communicationRange) {
            // 当距离超过通信范围时，应用衰减因子
            double decayFactor = 100; // 可以根据需要调整衰减因子的值
            double decayedDistance = distance * decayFactor;
            double rssi = txPower - 10 * pathLossExponent * Math.log10(decayedDistance);
            return rssi;
        } else {
            // 当距离在通信范围内时，正常计算RSSI值
            double rssi = txPower - 10 * pathLossExponent * Math.log10(distance);
            return rssi;
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

    public static List<Node> gn(int nums, int range, int gridSize, double dc, double vl, int len, double R){
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

        //初始化RSSI
        for (int i = 0; i < results.size(); i++) {
            for (int j = 0; j < results.size(); j++) {
                Node a = results.get(i), b = results.get(j);
                if (a == b){
                    a.rssiList.put(b,(double)0);
                    b.rssiList.put(a,(double)0);
                }else{
                    double distance = calTwoDistance(a,b);
                    double rssi = calculateRSSI(distance, R, 0, 2);
                    a.rssiList.put(b,rssi);
                    b.rssiList.put(a,rssi);
                }
            }
        }


        return results;
    }
    public static double calTwoDistance(Node a, Node b){
        double x1 = a.x, y1 = a.y, x2 = b.x, y2 = b.y;
        return Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
    }

    public static void un(List<Node> nodeList, int curSlot,  int beginSlot, double vl, int gridSize, double R){
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

        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = 0; j < nodeList.size(); j++) {
                Node a = nodeList.get(i), b = nodeList.get(j);
                if (a == b){
                    a.rssiList.put(b,(double)0);
                    b.rssiList.put(a,(double)0);
                }else{
                    double distance = calTwoDistance(a,b);
                    double rssi = calculateRSSI(distance, R, 0, 2);
                    a.rssiList.put(b,rssi);
                    b.rssiList.put(a,rssi);
                }
            }
        }
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
            }
        }
    }

    private static void secondFound(Node a, Node b, int curSlot, double R, double threshold1, int endSlot) {
        //这个threshold是判断推荐或者不推荐的阈值
        //1. 如果对方的邻居表还没有你 则说明并不是多次发现
        if (!a.neighbors.contains(b) || !b.neighbors.contains(a)) return;
        //2. 具体添加的就是: (1) 最近的一个未来激活时隙 (2) 由谁在哪个slot推荐的其他节点此时的neighborList

        //这些都是b节点要添加的信息
        HashMap<Integer,IsTold> waitList = b.wakeList;


        for (Node node : a.neighbors) {
            // group-based 直接推荐

                if (!b.neighbors.contains(node)) {
                    int minAddSlot = Integer.MAX_VALUE;
                    for (Integer slot : node.slots) {
                        if (slot > curSlot && slot <= endSlot){
                            minAddSlot = Math.min(minAddSlot,slot);
                        }
                    }
                    b.slots.add(minAddSlot);
                    //waitList的添加处理:
                    if (waitList.get(minAddSlot) == null){
                        waitList.put(minAddSlot, new IsTold());
                    }
                    IsTold isTold = waitList.get(minAddSlot);

                    List<Node> list = new ArrayList<Node>();
                    for (Node neighbor : node.neighbors) {
                        list.add(neighbor);
                    }
                    //slot20 node节点所包含的邻居
                    isTold.neiList.put(node,list);
                    //什么节点 在什么时候推荐的
                    isTold.whenRecommend.put(a,curSlot);
                    if (isTold.whoRecommend.get(minAddSlot) == null) {
                        isTold.whoRecommend.put(minAddSlot,new ArrayList<Node>());
                    }
                    //在什么slot是 谁推荐的信息
                    isTold.whoRecommend.get(minAddSlot).add(a);
                }

        }

        for (Node node : b.neighbors) {
            // group-based Advance推荐形式阈值

                if (!a.neighbors.contains(node)) {
                    int minAddSlot = Integer.MAX_VALUE;
                    for (Integer slot : node.slots) {
                        if (slot > curSlot && slot <= endSlot){
                            minAddSlot = Math.min(minAddSlot,slot);
                        }
                    }
                    a.slots.add(minAddSlot);
                    //waitList的添加处理:
                    if (waitList.get(minAddSlot) == null){
                        waitList.put(minAddSlot, new IsTold());
                    }
                    IsTold isTold = waitList.get(minAddSlot);

                    List<Node> list = new ArrayList<Node>();
                    for (Node neighbor : node.neighbors) {
                        list.add(neighbor);
                    }
                    //slot20 node节点所包含的邻居
                    isTold.neiList.put(node,list);
                    //什么节点 在什么时候推荐的
                    isTold.whenRecommend.put(b,curSlot);
                    if (isTold.whoRecommend.get(minAddSlot) == null) {
                        isTold.whoRecommend.put(minAddSlot,new ArrayList<Node>());
                    }
                    //在什么slot是 谁推荐的信息
                    isTold.whoRecommend.get(minAddSlot).add(b);
                }
            }

    }

    private static boolean isRecommnd(Node a, Node b, Node node, double R, double threshold1) {
        //1.计算a和node 以及b和node的共同邻居结点个数
        int com1 =0, com2 =0;
        for (Node cur : a.neighbors) {
            if (node.neighbors.contains(cur)) com1++;
        }
        for (Node cur : b.neighbors) {
            if (node.neighbors.contains(cur)) com2++;
        }
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
        return angleInDegrees;
    }

    public static boolean isIn(double x1, double x2, double y1, double y2,double R,double range){
        if (x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0 || x1 > range || x2 > range || y1 > range || y2 > range) return false;
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        return distance <= R;
    }

    //一个节点可能由很多其他节点告诉它 推荐在这个时隙激活
    private static boolean calAllConfidence(Node cur, Node valid, int curSlot, double R ,double threshold) {
        double maxConfidence = -1;
        IsTold isTold = cur.wakeList.get(curSlot);
        List<Node> nodes = isTold.whoRecommend.get(curSlot);
        for (Node node : nodes) {
            if (maxConfidence > threshold) return true;
            //每个节点推荐所带来的置信度计算
            //1. node节点在t1时刻推荐的
            int t1 = isTold.whenRecommend.get(node);
            int deltaT = curSlot - t1;
            //2. node节点在t1时刻推荐的  valid(待验证节点)在t1时刻的邻居表
            double commonNeighbors = 0;
            List<Node> list = isTold.neiList.get(valid);
            double totalNeighborNodes = list.size()+cur.neighbors.size();
            if (list != null){
                for (int i = 0; i < list.size(); i++) {
                    if (valid.neighbors.contains(list.get(i))){
                        commonNeighbors++;
                    }
                }
            }
            //第一个置信度
            double confidence1 = 1*2*totalNeighborNodes/commonNeighbors;
            //第二个置信度
            double v = cur.v;
            double confidence2 = 10*(-2.04+0.001*deltaT+1.952*v+0.006*deltaT*v-0.269*v*v)/R;
            double totalConfidence = confidence1-confidence2;
            maxConfidence = totalConfidence > maxConfidence ? totalConfidence : maxConfidence;
        }
        return false;
    }

    private static boolean isFirstFound(Node a, Node b) {
        return !a.neighbors.contains(b) && !b.neighbors.contains(a);
    }

    private static boolean isWake(int curSlot, Node cur) {
        return cur.slots.contains(curSlot);
    }

    private static int foundMinSlot(List<Node> nodeList) {
        int res = Integer.MAX_VALUE;
        for (Node node : nodeList) {
            res = node.initialSlot < res ? node.initialSlot : res;
        }
        return res;
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

class NodeDisplay extends JPanel {
    private static final int AREA_SIZE = 600;
    private static final int GRID_SIZE = 10;
    private ArrayList<ArrayList<double[]>> nodePositions;

    public NodeDisplay(ArrayList<ArrayList<double[]>> nodePositions) {
        this.nodePositions = nodePositions;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制网格
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= AREA_SIZE; i += GRID_SIZE) {
            g.drawLine(i, 0, i, AREA_SIZE);
            g.drawLine(0, i, AREA_SIZE, i);
        }

        // 绘制节点的运动轨迹
        g.setColor(Color.BLUE);
        for (ArrayList<double[]> positions : nodePositions) {
            for (int i = 0; i < positions.size() - 1; i++) {
                double[] startPos = positions.get(i);
                double[] endPos = positions.get(i + 1);

                g.drawLine((int) startPos[0], (int) startPos[1], (int) endPos[0], (int) endPos[1]);
            }
        }
    }
}