package nd1;

import java.util.Random;

/**
 * @author spencerReid
 * @date 2023年04月10日17:32
 */
public class Rssi {

    public static void main(String[] args) {
        double distance = 1;
        double communicationRange = 50;
        double txPower = 0;
        double pathLossExponent = 2;
        double d0 = 1;
        double rssi = calculateRSSI(distance, communicationRange, txPower, pathLossExponent,d0);
        System.out.println(rssi);
    }

    public static double calculateRSSI(double distance, double communicationRange, double txPower, double pathLossExponent, double d0) {
        if (distance <= 0) return 0;
        if (distance > communicationRange) {
            // 当距离超过通信范围时，应用衰减因子
            double decayFactor = 100; // 可以根据需要调整衰减因子的值
            double decayedDistance = distance * decayFactor;
            double rssi = txPower - 10 * pathLossExponent * Math.log10(decayedDistance / d0);
            return rssi;
        } else {
            // 当距离在通信范围内时，正常计算RSSI值
            double rssi = txPower - 10 * pathLossExponent * Math.log10(distance / d0);
            return rssi;
        }
    }

    public static double estimateDistance(double rssi, double communicationRange, double txPower, double pathLossExponent, double d0) {
        // 当RSSI值为0时，返回0
        if (rssi == 0) return 0;

        // 计算距离
        double distance = d0 * Math.pow(10, (txPower - rssi) / (10 * pathLossExponent));

        // 在计算出的距离上添加一个随机误差
        double errorRange = 0.1; // 可以根据需要调整误差范围的值
        Random random = new Random();
        double errorFactor = 1 + errorRange * (random.nextDouble() * 2 - 1); // 在[-errorRange, errorRange]之间生成一个随机数
        double estimatedDistance = distance * errorFactor;

        // 将估计距离限制在通信范围内
        if (estimatedDistance > communicationRange) {
            estimatedDistance = communicationRange;
        }

        return estimatedDistance;
    }



}
