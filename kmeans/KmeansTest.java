package cn.tx.evaluation.kmeans;

import cn.tx.evaluation.utils.ArrayUtil;

import java.io.*;
import java.util.*;

/**
 * k-means算法 测试类
 *
 * @author tx
 * @date 2019/04/18
 */
public class KmeansTest {
    public static void main(String[] args) {
        // 测试数据
        //double[][] points = {{0, 0}, {4, 10}, {1, 1}, {5, 8}};
        KmeansTest test = new KmeansTest();
        double[][] points = test.ioRead("C:\\Study\\评估项目\\评估项目部分算法流程\\算法\\聚类算法\\k-means\\test1.txt");
        // 初始化数据结构
        KmeansData data = new KmeansData(points, points.length, 2);
        // 初始化参数结构
        KmeansParam param = new KmeansParam();
        // 设置聚类中心点的初始化模式为随机模式
        param.initCenterMethord = KmeansParam.CENTER_RANDOM;
        // 做kmeans计算，分两类
        int k = 3;
        Kmeans.doKmeans(k, data, param);
        // 打印最终结果
        System.out.println("原始数据对应的分类为: ");
        Map<Integer, double[]> res = new HashMap<Integer, double[]>();
        for (int z = 0; z < k; z++) {
            System.out.println("属于" + z + "簇的数据：");
            for (int i = 0; i < data.data.length; i++) {
                for (int j = 0; j < data.dim; j++) {
                    res.put(i, data.data[i]);
                }
                if (data.labels[i] == z) {
                    System.out.println(Arrays.toString(res.get(i)));
                }
            }
            System.out.println("第" + z + "簇数据归纳完毕");
        }
        System.out.println("计算完毕");
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
}
