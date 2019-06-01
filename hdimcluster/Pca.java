package cn.tx.evaluation.hdimcluster;

import Jama.Matrix;
import cn.tx.evaluation.utils.ArrayUtil;

import java.util.Arrays;
import java.util.Date;

/**
 * 主成分分析法
 *
 * @author tx
 * @date 2019/02/20
 */
public class Pca {

    double[][] result;

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
    public double[][] CoefficientOfAssociation(double[][] x) {
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
    public double[][] FlagValue(double[][] x) {
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
    public double[][] FlagVector(double[][] x) {
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
    public int[] SelectPrincipalComponent(double[][] x, double threshold) {
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
     * 求取缩减后的综合指标矩阵
     *
     * @param x     特征向量矩阵
     * @param y     原始数据标准化矩阵
     * @param index 前N个主成分对应的特征值数组的索引值
     * @return 缩减后的综合指标矩阵
     */
    public double[][] PrincipalComponent(double[][] x, double[][] y, int[] index) {
        double[][] z = new double[x.length][index.length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                for (int k = 0; k < index.length; k++) {
                    if (j == index[k]) {
                        z[i][k] = x[i][j];
                    }
                }
            }
        }
        System.out.println("***********************************************************************");
        System.out.println("前N个主成分对应的特征值的特征向量矩阵为：");
        ArrayUtil.displayArray(z);
        System.out.println("缩减后的综合指标矩阵为：");
        result = ArrayUtil.arrayMultiplyTwoTwo(y, z);
        ArrayUtil.displayArray(result);
        return result;
    }

    /**
     * 编写测试类
     *
     * @param data      原始数据输入矩阵
     * @param threshold 设置阈值大小（85）
     * @return 缩减后的综合指标矩阵
     */
    public void test(double[][] data, double threshold) {
        Date date = new Date();
        double[][] Standard = this.standard(data);
        System.out.println("数据标准化完毕");

        double[][] Assosiation = this.CoefficientOfAssociation(Standard);
        System.out.println("样本相关系数矩阵计算完毕");

        double[][] FlagValue = this.FlagValue(Assosiation);
        System.out.println("样本相关系数矩阵的特征值计算完毕");

        double[][] FlagVector = this.FlagVector(Assosiation);
        System.out.println("样本相关系数矩阵的特征向量计算完毕");

        int[] index = this.SelectPrincipalComponent(FlagValue, threshold);
        System.out.println("获取主成分完毕");

        this.PrincipalComponent(FlagVector, Standard, index);
        System.out.println("求取缩减后的综合指标矩阵完毕");
        System.out.println("主成分分析计算完毕");
        System.out.println("***********************************************************************");
    }

}