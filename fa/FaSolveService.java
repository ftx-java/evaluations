package cn.tx.evaluation.fa;

import Jama.Matrix;
import cn.tx.evaluation.utils.ArrayUtil;

import java.util.Arrays;
import java.util.Date;

/**
 * 因子分析法
 *
 * @author tx
 * @date 2019/03/16
 */
public class FaSolveService {
    /**
     * 将原始数据标准化
     * n 二维矩阵的行号
     * p 二维矩阵的列号
     * average 每一列的平均值
     * result 标准化后的向量
     * var 方差
     */
    public double[][] standard(double[][] x) {
        int n = x.length;
        int p = x[0].length;
        double[] average = new double[p];
        double[][] result = new double[n][p];
        double[] var = new double[p];
        //取得每一列的平均值
        for (int k = 0; k < p; k++) {
            double temp = 0;
            for (int i = 0; i < n; i++) {
                temp += x[i][k];
            }
            average[k] = temp / n;
        }

        //取得方差
        for (int k = 0; k < p; k++) {
            double temp = 0;
            for (int i = 0; i < n; i++) {
                temp += (x[i][k] - average[k]) * (x[i][k] - average[k]);
            }
            var[k] = temp / (n - 1);
        }

        //获得标准化的矩阵
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                result[i][j] = (double) ((x[i][j] - average[j]) / Math.sqrt(var[j]));
            }
        }
        System.out.println("原始数据标准化矩阵为：");
        ArrayUtil.displayArray(result);
        return result;
    }

    /**
     * 计算样本相关系数矩阵
     * n 二维矩阵的行号
     * p 二维矩阵的列号
     * result 相关系数矩阵
     *
     * @param x 处理后的标准矩阵
     * @return 系数矩阵
     */
    public double[][] coefficientOfAssociation(double[][] x) {
        int n = x.length;
        int p = x[0].length;
        double[][] result = new double[p][p];
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < p; j++) {
                double temp = 0;
                for (int k = 0; k < n; k++) {
                    temp += x[k][i] * x[k][j];
                }
                result[i][j] = temp / (n - 1);
            }
        }
        System.out.println("***********************************************************************");
        System.out.println("计算样本相关系数矩阵：");
        ArrayUtil.displayArray(result);
        return result;
    }

    /**
     * 计算相关系数矩阵的特征值
     *
     * @param x 相关系数矩阵
     * @return 矩阵特征值
     */
    public double[][] flagValue(double[][] x) {
        //定义一个矩阵
        Matrix A = new Matrix(x);
        //由特征值组成的对角矩阵
        Matrix B = A.eig().getD();
        double[][] result = B.getArray();
        System.out.println("***********************************************************************");
        System.out.println("计算相关系数矩阵的特征值：");
        ArrayUtil.displayArray(result);
        return result;
    }

    /**
     * 计算相关系数矩阵的特征向量
     *
     * @param x 相关系数举证
     * @return 矩阵特向量
     */
    public double[][] flagVector(double[][] x) {
        //定义一个矩阵
        Matrix A = new Matrix(x);
        //由特征向量组成的对角矩阵
        Matrix B = A.eig().getV();
        double[][] result = B.getArray();
        System.out.println("***********************************************************************");
        System.out.println("计算相关系数矩阵的特征向量：");
        ArrayUtil.displayArray(result);
        return result;
    }

    /**
     * 假设阈值是85%，选取最大的前几个主成分
     * n 二维矩阵的行号
     *
     * @param x 特征值 Threshold阈值
     * @return 选取前N个主成分
     */
    public int[] selectPrincipalComponent(double[][] x, double threshold) {
        int n = x.length;
        double[] a = new double[n];
        int[] result = new int[n];
        int k = 0;
        double temp = 0;
        int m = 0;
        double total = 0;
        //a[] = temp1[] 样本相关系数矩阵的特征值矩阵(一维)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    a[k] = x[i][j];
                }
            }
            k++;
        }
        double[] temp1 = new double[a.length];
        System.arraycopy(a, 0, temp1, 0, a.length);
        System.out.println("***********************************************************************");
        System.out.println("样本相关系数矩阵的特征值矩阵(一维)为：");
        ArrayUtil.displayArray(temp1);

        //将样本相关系数矩阵的特征值矩阵中的数据从大到小排序
        for (int i = 0; i < n; i++) {
            temp = temp1[i];
            for (int j = i; j < n; j++) {
                if (temp <= temp1[j]) {
                    temp = temp1[j];
                    temp1[j] = temp1[i];
                }
                temp1[i] = temp;
            }
        }
        System.out.println("将样本相关系数矩阵的特征值矩阵中的数据从大到小排序:");
        ArrayUtil.displayArray(temp1);

        //相关系数矩阵的特征值矩阵排序后  特征值在a中对应的索引值矩阵result
        for (int i = 0; i < n; i++) {
            temp = a[i];
            for (int j = 0; j < n; j++) {
                if (a[j] >= temp) {
                    temp = a[j];
                    k = j;
                }
                result[m] = k;
            }
            a[k] = -10000;
            m++;
        }

        //求取满足85%阈值的对应特征值的索引值end
        for (int i = 0; i < n; i++) {
            total += temp1[i];
        }
        int p = 1;
        temp = temp1[0];
        for (int i = 0; i < n; i++) {
            if (temp / total <= (threshold / 100)) {
                temp += temp1[i + 1];
                p++;
            }
        }
        int[] end = new int[p];
        System.arraycopy(result, 0, end, 0, p);
        System.out.println("选取前N个主成分对应的特征值数组的索引值为：");
        Arrays.sort(end);
        System.out.println(Arrays.toString(end));
        return end;
    }

    /**
     * 计算因子载荷矩阵
     *
     * @param flag  特征值矩阵
     * @param x     特征向量矩阵
     * @param index 前N个主成分
     * @return 因子载荷矩阵
     */
    public double[][] principalComponent(double[][] flag, double[][] x, int[] index) {
        double[][] z = new double[x.length][index.length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                for (int k = 0; k < index.length; k++) {
                    if (j == index[k]) {
                        z[i][k] = x[i][j] * Math.sqrt(flag[j][j]);
                    }
                }
            }
        }
        System.out.println("***********************************************************************");
        System.out.println("因子载荷矩阵为：");
        ArrayUtil.displayArray(z);
        return z;
    }

    /**
     * 计算指标权重
     *
     * @param arr 因子载荷矩阵
     * @return 各指标权重（归一化）
     */
    public double[] indexWeight(double[][] arr) {
        double[] sum = new double[arr[0].length];
        double[] result = new double[arr.length];
        for (int i = 0; i < arr[0].length; i++) {
            for (int j = 0; j < arr.length; j++) {
                sum[i] += Math.abs(arr[j][i]);
            }
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                result[i] += Math.abs(arr[i][j]) / sum[j];
            }
        }

        System.out.println("***********************************************************************");
        System.out.println("各指标权重为：");
        ArrayUtil.displayArray(result);

        //各指标权重归一化
        double sum1 = 0;
        for (int i = 0; i < result.length; i++) {
            sum1 += result[i];
        }

        double[] result1 = new double[result.length];
        for (int i = 0; i < result.length; i++) {
            result1[i] = result[i] / sum1;
        }
        System.out.println("各指标权重归一化结果为：");
        ArrayUtil.displayArray(result1);
        return result1;
    }

    /**
     * 编写测试类
     *
     * @param data      原始数据输入矩阵
     * @param threshold 设置阈值大小（85）
     * @return 各指标权重归一化结果
     */
    public void test(double[][] data, double threshold) {
        Date date = new Date();
        double[][] standard = this.standard(data);
        System.out.println("数据标准化完毕");

        double[][] association = this.coefficientOfAssociation(standard);
        System.out.println("样本相关系数矩阵计算完毕");

        Kmo.kmo(association);
        System.out.println("KMO检验完毕");

        double[][] flagValue = this.flagValue(association);
        System.out.println("样本相关系数矩阵的特征值计算完毕");

        double[][] flagVector = this.flagVector(association);
        System.out.println("样本相关系数矩阵的特征向量计算完毕");

        int[] index = this.selectPrincipalComponent(flagValue, threshold);
        System.out.println("获取主成分完毕");

        double[][] arr = this.principalComponent(flagValue, flagVector, index);
        System.out.println("计算因子载荷矩阵完毕");

        double[] result = this.indexWeight(arr);
        System.out.println("各指标权重计算完毕");
    }

    public static void main(String[] args) {
        double[][] data = {
                {10.51, -1.08, 78.03, 98.37, 1.08, 4.62, 0.63, 3.90, 735.97, 6.1, 8.96, 7.04, 30.31, 23.70},
                {10.50, 1.27, 87.06, 95.44, 1.14, 17.98, 0.31, 3.76, 1050.49, 5.5, 8.57, 5.26, 31.8, 19.46},
                {10.37, 7.96, 74.05, 96.09, 1.11, 9.70, 0.05, 3.41, 1398.9, 6.2, 7.81, 5.61, 31.69, 22.66},
                {9.66, 5.90, 68.26, 93.98, 1.06, 34.42, 0.03, 3.03, 1449.59, 6.7, 7.23, 4.80, 31.66, 20.91},
                {9.62, 13.94, 54.44, 94.10, 1.06, 38.27, 0.42, 2.85, 1546.75, 6.2, 6.99, 3.96, 32.97, 18.58},
                {9.61, 26.66, 59.20, 93.09, 1.09, 53.41, 0.54, 2.72, 1655.74, 6.7, 6.54, 3.92, 33.53, 19.95},
                {10.86, 22.86, 57.96, 94.78, 1.22, 16.59, 0.70, 2.82, 2121.65, 8.4, 6.32, 3.53, 35.15, 19.48},
                {11.26, 24.79, 56.64, 92.16, 1.32, 0.78, 0.99, 2.82, 2864.07, 8.2, 6.37, 3.2, 38.34, 19.27},
                {12.38, 29.92, 59.15, 90.47, 1.53, 12.65, 1.62, 2.98, 4032.51, 7.4, 6.83, 3.11, 45.23, 20.59},
                {13.65, 36.58, 60.14, 89.50, 1.77, 29.28, 1.67, 3.16, 6099.32, 7.6, 7.3, 3.09, 53.23, 22.55},
                {14.66, 36.50, 61.13, 87.63, 2.00, 37.81, 1.18, 3.22, 8188.72, 7.4, 7.49, 3.14, 60.84, 25.49},
                {15.21, 39.6, 64.23, 85.44, 2.18, 15.75, 0.76, 3.13, 10663.44, 7.4, 7.28, 2.83, 66.62, 25.89},
                {15.90, 42.51, 61.33, 84.82, 2.36, 8.04, 0.61, 2.98, 15282.49, 7.5, 6.91, 2.36, 72.24, 24.68},
                {16.48, 46.64, 63.62, 86.53, 2.41, 29.38, 0.3, 2.8, 19460.3, 8.4, 6.42, 2.02, 73.62, 23.21},
                {17.26, 51.51, 61.00, 85.11, 2.52, 36.36, 0.51, 2.68, 23991.52, 8.5, 6.13, 1.76, 76.93, 22.14},
                {17.95, 53.47, 57.41, 86.55, 2.69, 26.82, 0.69, 2.6, 28473.38, 9.4, 5.85, 1.57, 81.19, 21.85},
                {19.12, 55.26, 57.94, 87.89, 2.87, 35.67, 0.77, 2.55, 31811.48, 8.4, 5.79, 1.46, 88.07, 22.18},
                {19.45, 56.18, 58.99, 87.29, 2.97, 1.66, 0.49, 2.45, 33115.89, 9.7, 5.48, 1.29, 89.79, 21.18},
                {19.52, 56.19, 59.36, 86.06, 3.06, 4.190, 0.47, 2.36, 38213.15, 10.2, 5.22, 1.16, 92.19, 20.44},
                {19.5, 60.44, 58.94, 84.98, 3.11, 10.39, 0.29, 2.25, 38430.18, 11.3, 4.87, 1.04, 92.24, 19.74},
                {19.34, 59.42, 58.38, 84.08, 3.13, 47.12, 0.14, 2.12, 33303.62, 12.1, 4.52, 0.92, 91.65, 18.59},
                {19.13, 60.42, 59.38, 85.08, 3.15, 17.56, 0.21, 2.02, 30105.00, 13.3, 4.22, 0.51, 91.23, 11.03},
        };
        FaSolveService pcaSolveService = new FaSolveService();
        pcaSolveService.test(data, 85);
    }
}