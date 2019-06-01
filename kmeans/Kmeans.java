package cn.tx.evaluation.kmeans;

import cn.tx.evaluation.utils.ArrayUtil;

import java.util.*;

/**
 * k-means算法
 *
 * @author tx
 * @date 2019/04/18
 */
public class Kmeans {

    /**
     * 做Kmeans运算
     *
     * @param k     int 聚类个数
     * @param data  kmeans_data kmeans数据类
     * @param param kmeans_param kmeans参数类
     * @return kmeans_result kmeans运行信息类
     */
    public static KmeansResult doKmeans(int k, KmeansData data, KmeansParam param) {
        // 预处理
        // 聚类中心点集
        double[][] centers = new double[k][data.dim];
        data.centers = centers;
        // 各聚类的包含点个数
        int[] centerCounts = new int[k];
        data.centerCounts = centerCounts;
        Arrays.fill(centerCounts, 0);
        // 各个点所属聚类标号
        int[] labels = new int[data.length];
        data.labels = labels;
        // 临时缓存旧的聚类中心坐标
        double[][] oldCenters = new double[k][data.dim];

        // 初始化聚类中心（随机或者依序选择data内的k个不重复点）
        // 随机选取k个初始聚类中心
        if (param.initCenterMethord == KmeansParam.CENTER_RANDOM) {
            Random rn = new Random();
            List<Integer> seeds = new LinkedList<Integer>();
            while (seeds.size() < k) {
                int randomInt = rn.nextInt(data.length);
                if (!seeds.contains(randomInt)) {
                    seeds.add(randomInt);
                }
            }
            Collections.sort(seeds);
            for (int i = 0; i < k; i++) {
                int m = seeds.remove(0);
                for (int j = 0; j < data.dim; j++) {
                    centers[i][j] = data.data[m][j];
                }
            }
        } else { // 选取前k个点位初始聚类中心
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < data.dim; j++) {
                    centers[i][j] = data.data[i][j];
                }
            }
        }
        System.out.println("初始质心坐标为：");
        ArrayUtil.displayArray(centers);
        // 第一轮迭代
        for (int i = 0; i < data.length; i++) {
            double minDist = dist(data.data[i], centers[0], data.dim);
            int label = 0;
            for (int j = 1; j < k; j++) {
                double tempDist = dist(data.data[i], centers[j], data.dim);
                if (tempDist < minDist) {
                    minDist = tempDist;
                    label = j;
                }
            }
            labels[i] = label;
            centerCounts[label]++;
        }
        updateCenters(k, data);
        copyCenters(oldCenters, centers, k, data.dim);
        System.out.println("第1次迭代质心坐标为：");
        ArrayUtil.displayArray(centers);

        // 迭代预处理
        int maxAttempts = param.attempts > 0 ? param.attempts : KmeansParam.MAX_ATTEMPTS;
        int attempts = 1;
        double criteria = param.criteria > 0 ? param.criteria : KmeansParam.MIN_CRITERIA;
        double criteriaBreakCondition = 0;
        // 标记哪些中心被修改过
        boolean[] flags = new boolean[k];

        // 通过迭代器进行迭代
        iterate:
        // 迭代次数不超过最大值，最大中心改变量不超过阈值
        while (attempts < maxAttempts) {
            // 初始化中心点“是否被修改过”标记
            for (int i = 0; i < k; i++) {
                flags[i] = false;
            }
            // 遍历data内所有点
            for (int i = 0; i < data.length; i++) {
                double minDist = dist(data.data[i], centers[0], data.dim);
                int label = 0;
                for (int j = 1; j < k; j++) {
                    double tempDist = dist(data.data[i], centers[j], data.dim);
                    if (tempDist < minDist) {
                        minDist = tempDist;
                        label = j;
                    }
                }
                // 如果当前点被聚类到新的类别则做更新
                if (label != labels[i]) {
                    int oldLabel = labels[i];
                    labels[i] = label;
                    centerCounts[oldLabel]--;
                    centerCounts[label]++;
                    flags[oldLabel] = true;
                    flags[label] = true;
                }
            }
            updateCenters(k, data);
            attempts++;

            // 计算被修改过的中心点最大修改量是否超过阈值
            //新大佬和老大佬之间的距离是否小于某一个设置的阈值
            double maxDist = 0;
            for (int i = 0; i < k; i++) {
                if (flags[i]) {
                    double tempDist = dist(centers[i], oldCenters[i], data.dim);
                    if (maxDist < tempDist) {
                        maxDist = tempDist;
                    }
                    // 更新oldCenter
                    for (int j = 0; j < data.dim; j++) {
                        oldCenters[i][j] = centers[i][j];
                    }
                }
            }
            System.out.println("第" + attempts + "次迭代的质心坐标为：");
            ArrayUtil.displayArray(centers);
            if (maxDist < criteria) {
                //退出迭代时的最大距离（小于阈值）
                criteriaBreakCondition = maxDist;
                break iterate;
            }
        }

        // 输出信息
        System.out.println("迭代完毕");
        System.out.println("***********************************************************************");
        KmeansResult rvInfo = new KmeansResult();
        rvInfo.attempts = attempts;
        rvInfo.criteriaBreakCondition = criteriaBreakCondition;
        if (param.isDisplay) {
            System.out.println("聚类个数=" + k);
            System.out.println("迭代次数=" + attempts);
            System.out.println("退出迭代时的新旧质点间最大距离（小于阈值）=" + criteriaBreakCondition);
            System.out.println("每个聚类包含的点的个数: ");
            for (int i = 0; i < k; i++) {
                System.out.print(centerCounts[i] + " ");
            }
            System.out.print("\n\n");
        }
        return rvInfo;
    }

    /**
     * 计算两点欧氏距离
     *
     * @param pa  double[]
     * @param pb  double[]
     * @param dim int 维数
     * @return double 距离
     */
    public static double dist(double[] pa, double[] pb, int dim) {
        double rv = 0;
        for (int i = 0; i < dim; i++) {
            double temp = pa[i] - pb[i];
            temp = temp * temp;
            rv += temp;
        }
        return Math.sqrt(rv);
    }

    /**
     * 更新聚类中心坐标
     *
     * @param k    int 分类个数
     * @param data kmeans_data
     */
    private static void updateCenters(int k, KmeansData data) {
        double[][] centers = data.centers;
        //centers元素全置0
        setDouble2Zero(centers, k, data.dim);
        int[] labels = data.labels;
        int[] centerCounts = data.centerCounts;
        for (int i = 0; i < data.dim; i++) {
            for (int j = 0; j < data.length; j++) {
                centers[labels[j]][i] += data.data[j][i];
            }
        }
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < data.dim; j++) {
                centers[i][j] = centers[i][j] / centerCounts[i];
            }
        }
    }

    /**
     * double[][] 元素全置0
     *
     * @param matrix  double[][]
     * @param highDim int
     * @param lowDim  int <br/>
     *                double[highDim][lowDim]
     */
    private static void setDouble2Zero(double[][] matrix, int highDim, int lowDim) {
        for (int i = 0; i < highDim; i++) {
            for (int j = 0; j < lowDim; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    /**
     * 拷贝源二维矩阵元素到目标二维矩阵。 foreach (dests[highDim][lowDim] = sources[highDim][lowDim]);
     *
     * @param dests   double[][]
     * @param sources double[][]
     * @param highDim int
     * @param lowDim  int
     */
    private static void copyCenters(double[][] dests, double[][] sources, int highDim, int lowDim) {
        for (int i = 0; i < highDim; i++) {
            for (int j = 0; j < lowDim; j++) {
                dests[i][j] = sources[i][j];
            }
        }
    }

}
