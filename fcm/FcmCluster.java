package cn.tx.evaluation.fcm;

import cn.tx.evaluation.utils.ArrayUtil;
import weka.classifiers.rules.DecisionTableHashKey;
import weka.clusterers.NumberOfClustersRequestable;
import weka.clusterers.RandomizableClusterer;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.Capabilities.Capability;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import java.io.*;
import java.util.*;

/**
 * 模糊聚类 FCM
 *
 * @author tx
 * @date 2019/05/05
 */
public class FcmCluster
        extends RandomizableClusterer
        implements NumberOfClustersRequestable, WeightedInstancesHandler {

    /**
     * 用于序列化
     */
    static final long serialVersionUID = -2134543132156464L;

    /**
     * 替换训练集中的缺省值
     */
    private ReplaceMissingValues m_ReplaceMissingFilter;

    /**
     * 产生聚类的个数
     */
    private int m_NumClusters = 2;

    /**
     * D: d(i,j)=||c(i)-x(j)||为第i个聚类中心与第j个数据点间的欧几里德距离
     */
    private Matrix D;

    /**
     * 模糊算子(加权指数)
     */
    private double m_fuzzifier = 2;

    /**
     * 聚类中心
     */
    private Instances m_ClusterCentroids;

    /**
     * 每个聚类的标准差
     */
    private Instances m_ClusterStdDevs;


    /**
     * 对于每个群集，保存每个群集的值的频率计数
     */
    private int[][][] m_ClusterNominalCounts;

    /**
     * 每个聚类包含的实例个数
     */
    private int[] m_ClusterSizes;

    /**
     * 属性最小值
     */
    private double[] m_Min;

    /**
     * 属性最大值
     */
    private double[] m_Max;

    /**
     * 迭代次数
     */
    private int m_Iterations = 0;

    /**
     * 平方误差
     */
    private double[] m_squaredErrors;

    /**
     * 初始构造器
     */
    public FcmCluster() {
        super();
        //初始化种子个数
        m_SeedDefault = 10;
        setSeed(m_SeedDefault);
    }

    /**
     * 聚类容器
     */
    @Override
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.enable(Capability.NUMERIC_ATTRIBUTES);
        result.enable(Capability.MISSING_VALUES);
        return result;
    }

    /**
     * 聚类产生函数
     */
    @Override
    public void buildClusterer(Instances data) throws Exception {
//        getCapabilities().testWithFail(data);
        //迭代次数
        m_Iterations = 0;
        m_ReplaceMissingFilter = new ReplaceMissingValues();
        //实例
        Instances instances = new Instances(data);
        instances.setClassIndex(-1);
        m_ReplaceMissingFilter.setInputFormat(instances);
        instances = Filter.useFilter(instances, m_ReplaceMissingFilter);

        m_Min = new double[instances.numAttributes()];
        m_Max = new double[instances.numAttributes()];
        for (int i = 0; i < instances.numAttributes(); i++) {
            //随机分配不定值
            m_Min[i] = m_Max[i] = Double.NaN;
        }
        //聚类中心
        m_ClusterCentroids = new Instances(instances, m_NumClusters);
        int[] clusterAssignments = new int[instances.numInstances()];

        for (int i = 0; i < instances.numInstances(); i++) {
            //更新最大最小值
            updateMinMax(instances.instance(i));
        }
        //随机数
        Random RandomO = new Random(getSeed());
        int instIndex;
        HashMap initC = new HashMap();
        DecisionTableHashKey hk = null;
        // 利用决策表随机生成聚类中心
        for (int j = instances.numInstances() - 1; j >= 0; j--) {
            instIndex = RandomO.nextInt(j + 1);
            hk = new DecisionTableHashKey(instances.instance(instIndex),
                    instances.numAttributes(), true);
            if (!initC.containsKey(hk)) {
                m_ClusterCentroids.add(instances.instance(instIndex));
                initC.put(hk, null);
            }
            instances.swap(j, instIndex);

            if (m_ClusterCentroids.numInstances() == m_NumClusters) {
                break;
            }
        }
        //聚类个数=聚类中心个数
        m_NumClusters = m_ClusterCentroids.numInstances();
        //求聚类中心到每个实例的距离
        D = new Matrix(solveD(instances).getArray());

        int i, j;
        int n = instances.numInstances();
        Instances[] tempI = new Instances[m_NumClusters];
        m_squaredErrors = new double[m_NumClusters];
        m_ClusterNominalCounts = new int[m_NumClusters][instances.numAttributes()][0];
        //初始化隶属矩阵U
        Matrix U = new Matrix(solveU(instances).getArray());
        //初始化价值函数值
        double q = 0;
        while (true) {
            m_Iterations++;
            for (i = 0; i < instances.numInstances(); i++) {
                Instance toCluster = instances.instance(i);
                //聚类处理实例,即输入的实例应该聚到哪一个簇?
                int newC = clusterProcessedInstance(toCluster, true);
                clusterAssignments[i] = newC;
            }
            // update centroids 更新聚类中心
            m_ClusterCentroids = new Instances(instances, m_NumClusters);
            for (i = 0; i < m_NumClusters; i++) {
                tempI[i] = new Instances(instances, 0);
            }
            for (i = 0; i < instances.numInstances(); i++) {
                tempI[clusterAssignments[i]].add(instances.instance(i));
            }
            for (i = 0; i < m_NumClusters; i++) {
                double[] vals = new double[instances.numAttributes()];
                for (j = 0; j < instances.numAttributes(); j++) {
                    double sum1 = 0, sum2 = 0;
                    for (int k = 0; k < n; k++) {
                        sum1 += U.get(i, k) * U.get(i, k) * instances.instance(k).value(j);
                        sum2 += U.get(i, k) * U.get(i, k);
                    }
                    vals[j] = sum1 / sum2;
                }
                m_ClusterCentroids.add(new Instance(1.0, vals));
            }
            D = new Matrix(solveD(instances).getArray());
            //计算新的聿属矩阵U
            U = new Matrix(solveU(instances).getArray());
            //新的价值函数值
            double q1 = 0;
            for (i = 0; i < m_NumClusters; i++) {
                for (j = 0; j < n; j++) {
                    // 计算价值函数值 即q1 += U(i,j)^m * d(i,j)^2
                    q1 += Math.pow(U.get(i, j), getFuzzifier()) * D.get(i, j) * D.get(i, j);
                }
            }
            //上次价值函数值的改变量(q1 -q)小于某个阀值(这里用机器精度:2.2204e-16)
            if (q1 - q < 2.2204e-16) {
                break;
            }
            q = q1;
        }

        // 计算标准差 跟K均值一样
        m_ClusterStdDevs = new Instances(instances, m_NumClusters);
        m_ClusterSizes = new int[m_NumClusters];
        for (i = 0; i < m_NumClusters; i++) {
            double[] vals2 = new double[instances.numAttributes()];
            for (j = 0; j < instances.numAttributes(); j++) {
                //判断属性是否是数值型的?!
                if (instances.attribute(j).isNumeric()) {
                    vals2[j] = Math.sqrt(tempI[i].variance(j));
                } else {
                    vals2[j] = Instance.missingValue();
                }
            }
            //1.0代表权值, vals2代表属性值
            m_ClusterStdDevs.add(new Instance(1.0, vals2));
            m_ClusterSizes[i] = tempI[i].numInstances();
        }
    }

    /**
     * 聚类一个实例, 返回实例应属于哪一个簇的编号
     * 首先计算输入的实例到所有聚类中心的距离, 哪里距离最小
     * 这个实例就属于哪一个聚类中心所在簇
     */
    private int clusterProcessedInstance(Instance instance, boolean updateErrors) {
        double minDist = Integer.MAX_VALUE;
        int bestCluster = 0;
        for (int i = 0; i < m_NumClusters; i++) {
            double dist = distance(instance, m_ClusterCentroids.instance(i));
            if (dist < minDist) {
                minDist = dist;
                bestCluster = i;
            }
        }
        if (updateErrors) {
            m_squaredErrors[bestCluster] += minDist;
        }
        return bestCluster;
    }

    /**
     * 分类一个实例, 调用clusterProcessedInstance()函数
     */
    @Override
    public int clusterInstance(Instance instance) throws Exception {
        m_ReplaceMissingFilter.input(instance);
        m_ReplaceMissingFilter.batchFinished();
        Instance inst = m_ReplaceMissingFilter.output();
        return clusterProcessedInstance(inst, false);
    }

    /**
     * 计算矩阵D, 即 d(i,j)=||c(i)-x(j)||
     */
    private Matrix solveD(Instances instances) {
        int n = instances.numInstances();
        Matrix D = new Matrix(m_NumClusters, n);
        for (int i = 0; i < m_NumClusters; i++) {
            for (int j = 0; j < n; j++) {
                D.set(i, j, distance(instances.instance(j), m_ClusterCentroids.instance(i)));
                if (D.get(i, j) == 0) {
                    D.set(i, j, 0.000000000001);
                }
            }
        }
        return D;
    }

    /**
     * 计算聿属矩阵U, 即U(i,j) = 1 / sum(d(i,j)/ d(k,j))^(2/(m-1)
     */
    private Matrix solveU(Instances instances) {
        int n = instances.numInstances();
        int i, j;
        Matrix U = new Matrix(m_NumClusters, n);
        for (i = 0; i < m_NumClusters; i++) {
            for (j = 0; j < n; j++) {
                double sum = 0;
                for (int k = 0; k < m_NumClusters; k++) {
                    //d(i,j)/d(k,j)^(2/(m-1)
                    sum += Math.pow(D.get(i, j) / D.get(k, j), 2 / (getFuzzifier() - 1));
                }
                U.set(i, j, Math.pow(sum, -1));
            }
        }
        return U;
    }

    /**
     * 计算两个实例之间的距离, 返回欧几里德距离
     */
    private double distance(Instance first, Instance second) {
        double val1;
        double val2;
        double dist = 0.0;
        for (int i = 0; i < first.numAttributes(); i++) {
            val1 = first.value(i);
            val2 = second.value(i);
            dist += (val1 - val2) * (val1 - val2);
        }
        dist = Math.sqrt(dist);
        return dist;
    }

    /**
     * 更新所有属性最大最小值, 跟K均值里的函数一样
     */
    private void updateMinMax(Instance instance) {

        for (int j = 0; j < m_ClusterCentroids.numAttributes(); j++) {
            if (!instance.isMissing(j)) {
                if (Double.isNaN(m_Min[j])) {
                    m_Min[j] = instance.value(j);
                    m_Max[j] = instance.value(j);
                } else {
                    if (instance.value(j) < m_Min[j]) {
                        m_Min[j] = instance.value(j);
                    } else {
                        if (instance.value(j) > m_Max[j]) {
                            m_Max[j] = instance.value(j);
                        }
                    }
                }
            }
        }
    }

    /**
     * 返回聚类个数
     */
    @Override
    public int numberOfClusters() throws Exception {
        return m_NumClusters;
    }

    /**
     * 返回模糊算子, 即加权指数
     *
     * @return 加权指数
     * @throws Exception 加权指数不能成功返回
     */
    public double fuzzifier() throws Exception {
        return m_fuzzifier;
    }

    /**
     * 返回一个枚举描述的活动选项
     */
    @Override
    public Enumeration listOptions() {
        Vector result = new Vector();

        result.addElement(new Option(
                "\tnumber of clusters.\n"
                        + "\t(default 2).",
                "N", 1, "-N <num>"));

        result.addElement(new Option(
                "\texponent.\n"
                        + "\t(default 2.0).",
                "F", 1, "-F <num>"));

        Enumeration en = super.listOptions();
        while (en.hasMoreElements()) {
            result.addElement(en.nextElement());
        }
        return result.elements();
    }

    /**
     * 返回文本信息
     */
    public String numClustersTipText() {
        return "set number of clusters";
    }

    /**
     * 设置聚类个数
     */
    @Override
    public void setNumClusters(int n) throws Exception {
        if (n <= 0) {
            throw new Exception("Number of clusters must be > 0");
        }
        m_NumClusters = n;
    }

    /**
     * 取聚类个数
     */
    public int getNumClusters() {
        return m_NumClusters;
    }

    /**
     * 返回文本信息
     */
    public String fuzzifierTipText() {
        return "set fuzzifier";
    }

    /**
     * 设置模糊算子
     */
    public void setFuzzifier(double f) throws Exception {
        if (f <= 1) {
            throw new Exception("F must be > 1");
        }
        m_fuzzifier = f;
    }

    /**
     * 取得模糊算子
     */
    public double getFuzzifier() {
        return m_fuzzifier;
    }

    /**
     * 设置活动选项
     */
    @Override
    public void setOptions(String[] options)
            throws Exception {
        String optionString = Utils.getOption('N', options);
        if (optionString.length() != 0) {
            setNumClusters(Integer.parseInt(optionString));
        }
        optionString = Utils.getOption('F', options);
        if (optionString.length() != 0) {
            setFuzzifier((new Double(optionString)).doubleValue());
        }
        super.setOptions(options);
    }

    /**
     * 取得活动选项
     */
    @Override
    public String[] getOptions() {
        int i;
        Vector result;
        String[] options;
        result = new Vector();
        result.add("-N");
        result.add("" + getNumClusters());
        result.add("-F");
        result.add("" + getFuzzifier());
        options = super.getOptions();
        for (i = 0; i < options.length; i++) {
            result.add(options[i]);
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    /**
     * 结果显示
     */
    @Override
    public String toString() {
        int maxWidth = 0;
        for (int i = 0; i < m_NumClusters; i++) {
            for (int j = 0; j < m_ClusterCentroids.numAttributes(); j++) {
                if (m_ClusterCentroids.attribute(j).isNumeric()) {
                    double width = Math.log(Math.abs(m_ClusterCentroids.instance(i).value(j))) /
                            Math.log(10.0);
                    width += 1.0;
                    if ((int) width > maxWidth) {
                        maxWidth = (int) width;
                    }
                }
            }
        }
        StringBuffer temp = new StringBuffer();
        String naString = "N/A";
        for (int i = 0; i < maxWidth + 2; i++) {
            naString += " ";
        }
        temp.append("\nFuzzy C-means\n======\n");
        temp.append("\nNumber of iterations: " + m_Iterations + "\n");
        temp.append("Within cluster sum of squared errors: " + Utils.sum(m_squaredErrors));

        temp.append("\n\nCluster centroids:\n");
        for (int i = 0; i < m_NumClusters; i++) {
            temp.append("\nCluster " + i + "\n\t");
            temp.append("\n\tStd Devs:  ");
            for (int j = 0; j < m_ClusterStdDevs.numAttributes(); j++) {
                if (m_ClusterStdDevs.attribute(j).isNumeric()) {
                    temp.append(" " + Utils.doubleToString(m_ClusterStdDevs.instance(i).value(j),
                            maxWidth + 5, 4));
                } else {
                    temp.append(" " + naString);
                }
            }
        }
        temp.append("\n\n");
        return temp.toString();
    }

    /**
     * 取得聚类中心
     */
    public Instances getClusterCentroids() {
        return m_ClusterCentroids;
    }

    /**
     * 聚得标准差
     */
    public Instances getClusterStandardDevs() {
        return m_ClusterStdDevs;
    }

    /**
     * 每个群集的返回频率计算每个群集的值s
     */
    public int[][][] getClusterNominalCounts() {
        return m_ClusterNominalCounts;
    }

    /**
     * 取得平方差
     */
    public double getSquaredError() {
        return Utils.sum(m_squaredErrors);
    }

    /**
     * 取每个簇的实例个数
     */
    public int[] getClusterSizes() {
        return m_ClusterSizes;
    }

    /**
     * I/O流读取初始数据
     */
    double[][] arrIo;

    public double[][] ioRead(String filePath) {
        //1，建立联系：  File对象
        //C:/Users/ftx/Desktop/gwc.txt
        File file = new File(filePath);
        String str;
        List<double[]> list = new ArrayList<double[]>();
        //2,选择流  提升is作用域
        BufferedReader bre = null;
        try {
            bre = new BufferedReader(new FileReader(file));
            // 3，操作 不断读取 缓冲数组
            //循环读取 按行扫描 每行先保存在arr中 再将arr中数据塞到Arr一维数组中
            while ((str = bre.readLine()) != null) {
                int a = 0;
                //txt中每行数据中每个数据间隔为4个空格
                String[] arr = str.split("   ");
                double[] Arr = new double[arr.length];
                for (String b : arr) {
                    if (b != null) {
                        Arr[a++] = Double.parseDouble(b);
                    }
                }
                list.add(Arr);
            }
            //将Arr中的数据存到二维数组中
            //找到最大的列数
            int max = 0;
            for (int i = 0; i < list.size(); i++) {
                if (max < list.get(i).length) {
                    max = list.get(i).length;
                }
            }
            double[][] array = new double[list.size()][max];
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    array[i][j] = list.get(i)[j];
                }
            }
            arrIo = array;
            System.out.println("输入的测试数据为：");
            ArrayUtil.displayArray(array);
            System.out.println("***********************************************************************");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("文件不存在");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("读取文件失败");
        } finally {
            try {
                //4，释放资源
                if (null != bre) {
                    bre.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("文件输入流关闭失败");
            }
        }
        return arrIo;
    }

    /**
     * 主函数
     */
    public static void main(String[] argv) {
//        FcmCluster fcm = new FcmCluster();
//        fcm.ioRead(argv[1]);
//        System.out.println(argv[1]);
        runClusterer(new FcmCluster(), argv);
    }
}
