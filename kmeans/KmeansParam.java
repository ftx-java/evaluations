package cn.tx.evaluation.kmeans;

/**
 * k-means算法 参数类
 *
 * @author tx
 * @date 2019/04/18
 */
public class KmeansParam {
    /**
     * 新老质心变动距离的阈值  MIN_CRITERIA
     * 阈值 criteria
     * 尝试次数  attempts
     * 初始化聚类中心点方式 initCenterMehtord
     * 是否直接显示结果 isDisplay
     */
    public static final int CENTER_ORDER = 0;
    public static final int CENTER_RANDOM = 1;
    public static final int MAX_ATTEMPTS = 1000;

    public static final double MIN_CRITERIA = 0.001;
    public double criteria = MIN_CRITERIA;
    public int attempts = MAX_ATTEMPTS;
    public int initCenterMethord = CENTER_ORDER;
    public boolean isDisplay = true;
}
