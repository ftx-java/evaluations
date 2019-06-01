package cn.tx.evaluation.hdimcluster;

/**
 * k-means算法  结果类
 *
 * @author tx
 * @date 2019/04/18
 */
public class KmeansResult {
    /**
     * 退出迭代时的尝试次数
     */

    public int attempts;
    /**
     * 退出迭代时的最大距离（小于阈值）
     */

    public double criteriaBreakCondition;
    /**
     * 聚类数
     */
    public int k;
}
