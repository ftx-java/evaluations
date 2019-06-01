package cn.tx.evaluation.kmeans;

/**
 * k-means算法 数据类
 *
 * @author tx
 * @date 2019/04/18
 */
public class KmeansData {
    /**
     * 初始数据 data
     * 数据个数 length
     * 数据维数 dim
     * 各个点所属聚类标号 labels
     * 聚类中心点集 centers
     * 各聚类的包含点个数 centerCounts
     */
    public double[][] data;
    public int length;
    public int dim;
    public int[] labels;
    public double[][] centers;
    public int[] centerCounts;

    public KmeansData(double[][] data, int length, int dim) {
        this.data = data;
        this.length = length;
        this.dim = dim;
    }
}
