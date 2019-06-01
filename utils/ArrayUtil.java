package cn.tx.evaluation.utils;

import java.util.Random;

/**
 * Demo class
 * 离差最大化算法
 * 数组工具类
 *
 * @author tx
 * @date 2019/01/15
 */
public class ArrayUtil {

    private ArrayUtil() {
    }

    /**
     * 数组打印（一维与二维）
     * 保留4位小数
     */
    public static void displayArray(double[] arr1) {
        for (int i = 0; i < arr1.length; i++) {
            System.out.print(String.format("%.4f", arr1[i]) + "\t");
        }
        System.out.println();
    }

    public static void displayArray(double[][] arr2) {
        for (int i = 0; i < arr2.length; i++) {
            for (int j = 0; j < arr2[i].length; j++) {
                System.out.print(String.format("%.4f", arr2[i][j]) + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * 二维数组转置
     *
     * @param arr 二维数组
     * @return 转置后的二维数组
     */
    public static double[][] transposeTwoDArray(double[][] arr) {
        for (int x = 1; x < arr.length; x++) {
            if (arr[x].length != arr[0].length) {
                System.out.print("数组各行数据个数不相同");
                return null;
            }
        }

        double[][] transposedArr = new double[arr[0].length][arr.length];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                transposedArr[j][i] = arr[i][j];
            }
        }
        return transposedArr;
    }

    /**
     * 一维数组和二维数组相乘
     *
     * @param arr1
     * @param arr2
     * @return 相乘后的一维数组
     */
    public static double[] arrayMultiplyOneTwo(double[] arr1, double[][] arr2) {
        if (arr1.length != arr2.length) {
            return null;
        }

        double[] productArr = new double[arr2[0].length];

        for (int j = 0; j < arr2[0].length; j++) {
            for (int i = 0; i < arr1.length; i++) {
                productArr[j] += arr1[i] * arr2[i][j];
            }
        }
        return productArr;
    }

    /**
     * 数乘 ：二维数组与一维数组相乘
     *
     * @param twoDArr 二维数组
     * @param oneDArr 一维数组
     * @return 加权单位化后的矩阵
     */
    public static double[][] arrayMultiplyTwoOne(double[][] twoDArr, double[] oneDArr) {
        // TODO Auto-generated method stub
        //加权单位化后的矩阵
        double[][] multiplyArr = new double[twoDArr.length][twoDArr[0].length];
        if (oneDArr.length != twoDArr[0].length) {
            System.out.println("维数不合适，无法相乘！");
        }
        for (int i = 0; i < twoDArr.length; i++) {
            for (int j = 0; j < twoDArr[0].length; j++) {
                multiplyArr[i][j] = oneDArr[j] * twoDArr[i][j];
            }
        }
        return multiplyArr;
    }

    /**
     * 两个二维数组相乘
     *
     * @param arr1 二维数组1
     * @param arr2 二维数组2
     * @return 相乘后的二维数组
     */
    public static double[][] arrayMultiplyTwoTwo(double[][] arr1, double[][] arr2) {
        if (arr1[0].length != arr2.length) {
            return null;
        }
        double[][] productArr = new double[arr1.length][arr2[0].length];
        for (int i = 0; i < arr1.length; i++) {
            for (int j = 0; j < arr2[0].length; j++) {
                for (int k = 0; k < arr1[0].length; k++) {
                    productArr[i][j] += arr1[i][k] * arr2[k][j];
                }
            }
        }
        return productArr;
    }

    /**
     * 计算样本均值
     *
     * @param arr 样本数据组成的一维数组
     * @return 样本均值
     */
    public static double calAverage(double[] arr) {
        if (arr == null) {
            System.out.print("输入的数组为空");
            return -1;
        }
        double average;
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        average = sum / arr.length;
        return average;
    }

    /**
     * 计算样本一阶绝对中心矩
     *
     * @param arr 样本数据组成的一维数组
     * @return 样本一阶绝对中心矩
     */
    public static double calFirstOrderCenterMoment(double[] arr) {
        if (arr == null) {
            System.out.print("输入的数组为空");
            return -1;
        }
        double firstOrderCenterMoment;
        double sum = 0;
        double expectedNum = calAverage(arr);
        for (int i = 0; i < arr.length; i++) {

            sum = Math.abs(arr[i] - expectedNum) + sum;
        }
        firstOrderCenterMoment = sum / arr.length;
        return firstOrderCenterMoment;
    }

    /**
     * 计算样本的熵
     *
     * @param arr 样本数据组成的一维数组
     * @return 样本熵
     */
    public static double calEntropy(double[] arr) {
        if (arr == null) {
            System.out.print("输入的数组为空");
            return -1;
        }
        double entropy;
        entropy = calFirstOrderCenterMoment(arr) * Math.sqrt(Math.PI / 2);
        return entropy;
    }

    /**
     * 计算样本的方差
     *
     * @param arr 样本数据组成的一维数组
     * @return 样本方差
     */
    public static double calVariance(double[] arr) {
        if (arr == null) {
            System.out.print("输入的数组为空");
            return -1;
        }
        double variance;
        double sum = 0;
        double expectedNum = calAverage(arr);
        for (int i = 0; i < arr.length; i++) {

            sum += Math.pow((arr[i] - expectedNum), 2);
        }
        variance = sum / (arr.length - 1);
        return variance;

    }

    /**
     * 计算样本超熵
     *
     * @param arr 样本数据组成的一维数组
     * @return 样本超熵
     */
    public static double calExcessEntropy(double[] arr) {
        if (arr == null) {
            System.out.print("输入的数组为空");
            return -1;
        }
        double excessEntropy;
        double variance = calVariance(arr);
        double entropy = calEntropy(arr);
        excessEntropy = Math.sqrt(Math.abs(variance - Math.pow(entropy, 2)));
        return excessEntropy;
    }

    /***
     * 生成一个正态随机数
     * @param expect        期望
     * @param variance        方差
     */
    public static double normalDistributedData(double expect, double variance) {
        double random = 0;
        Random r = new Random();
        double x = Math.sqrt(variance) * r.nextGaussian() + expect;
//		if(x>0 && x<1)
        random = x;
        return random;
    }

    /**
     * n个一维数组组成二维数组
     *
     * @param arr 输入的一维数组
     * @return 结果二维数组
     */
    public static double[][] OneDArrCombineToTwoD(double[]... arr) {
        // 评价矩阵
        double[][] EvaluationMatrix = new double[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; i++) {
            EvaluationMatrix[i] = arr[i];
        }
        return EvaluationMatrix;
    }

    /**
     * 一维数组与一个数相乘
     *
     * @param arr
     * @param number
     * @return
     */
    public static double[] arrayNumberMultiply(double[] arr, double number) {
        double[] result = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i] * number;
        }
        return result;
    }

    /**
     * 两个一维数组相加
     *
     * @param arr1
     * @param arr2
     * @return
     */
    public static double[] arrayAddation(double[] arr1, double[] arr2) {
        double[] resultArr = new double[arr1.length];
        if (arr1.length == arr2.length) {
            for (int i = 0; i < arr1.length; i++) {
                resultArr[i] = arr1[i] + arr2[i];
            }
            return resultArr;
        } else {
            System.out.println("数组长度不一致，无法相加");
            return null;
        }
    }

    /**
     * n个一维数组求平均
     *
     * @param arr
     * @return 平均后的一维数组
     */
    public static double[] average(double[]... arr) {

        for (int x = 1; x < arr.length; x++) {
            if (arr[x].length != arr[0].length) {
                //判断输入的数组中数据个数是否一致
                System.out.println("输入的数组中数据个数不一致");
                return null;
            }
        }
        //存放平均后的数组
        double[] averageArr = new double[arr[0].length];
        for (int i = 0; i < arr[0].length; i++) {
            double sum = 0;
            for (int j = 0; j < arr.length; j++) {
                sum += arr[j][i];
            }
            averageArr[i] = sum / arr.length;
        }
        return averageArr;
    }

    /**
     * n个二维矩阵求平均
     *
     * @param arr 输入数组
     * @return 平均后的数组
     */
    public static double[][] average(double[][]... arr) {
        double[][] averageArr = new double[arr[0].length][arr[0][0].length];
        for (int x = 1; x < arr.length; x++) {
            if (arr[x].length != arr[0].length) {
                //判断输入的数组中数据个数是否一致
                System.out.println("输入的数组行数不一致");
                return null;
            }
            for (int y = 0; y < arr[0].length; y++) {
                if (arr[x][y].length != arr[0][0].length) {
                    //判断输入的数组中数据个数是否一致
                    System.out.println("输入的数组列数不一致");
                    return null;
                }
            }
        }
        for (int i = 0; i < arr[0].length; i++) {
            for (int j = 0; j < arr[0][0].length; j++) {
                double sum = 0;
                for (int k = 0; k < arr.length; k++) {
                    sum += arr[k][i][j];
                }
                averageArr[i][j] = sum / arr.length;
            }
        }
        return averageArr;
    }
}

