package cn.tx.evaluation.mdcw;

import java.util.ArrayList;

import cn.tx.evaluation.utils.ArrayUtil;

/**
 * Demo class
 * 基于离差最大化的组合赋权算法
 *
 * @author tx
 * @date 2019/01/15
 */
public class MdcwSolverService {

    private MdcwSolverService() {
    }

    /**
     * 预处理结果 arrPretreatment
     * 由原始决策矩阵预处理结果计算其n维行向量 xn
     * 归一化处理结果 arrNormalized
     * 最优组合赋权向量计算结果 arrW
     */
    private double[][] arrPretreatment = new double[4][4];
    private double[] xn = new double[arrPretreatment.length];
    private double[] arrNormalized = new double[xn.length];
    private double[] arrW = new double[arrNormalized.length];

    /**
     * 定义原始决策矩阵
     */
    private double[][] originalDecisionMatrix = {
            {3.0, 1.0, 1.0, 70},
            {2.5, 0.8, 0.8, 50},
            {1.8, 0.5, 2.0, 110},
            {2.2, 0.7, 1.2, 90}
    };

    /**
     * 定义专家对属性权值的打分数(4名专家) 必须与属性个数保持一致
     */
    private double[][] w = {
            {0.3, 0.4, 0.15, 0.15},
            {0.4, 0.3, 0.15, 0.15},
            {0.25, 0.25, 0.25, 0.25},
            {0.2403, 0.2249, 0.3062, 0.2242}
    };

    /**
     * 定义预处理方法
     */
    private double[][] pretreatment(double[][] arr) {
        double[][] arr1 = arr;
        double maxIndex;
        double minIndex;
        double max;
        double min;
        double a;
        ArrayList arrayListMax = new ArrayList();
        ArrayList arrayListMin = new ArrayList();

        for (int i = 0; i < arr1[0].length; i++) {
            maxIndex = arr1[0][i];
            minIndex = arr1[0][i];
            for (int j = 0; j < arr1.length; j++) {
                if (maxIndex < arr1[j][i]) {
                    maxIndex = arr1[j][i];
                }
                if (minIndex > arr1[j][i]) {
                    minIndex = arr1[j][i];
                }
            }
            arrayListMax.add(maxIndex);
            arrayListMin.add(minIndex);
        }

        for (int i = 0; i < arr1[0].length; i++) {
            for (int j = 0; j < arr1.length; j++) {
                if (i == 0 || i == 2) {
                    max = (double) arrayListMax.get(i);
                    min = (double) arrayListMin.get(i);
                    a = arr1[j][i];
                    arr1[j][i] = (max - a) / (max - min);
                }
                if (i == 1 || i == 3) {
                    max = (double) arrayListMax.get(i);
                    min = (double) arrayListMin.get(i);
                    a = arr1[j][i];
                    arr1[j][i] = (a - min) / (max - min);
                }
            }
        }
        arrPretreatment = arr1;
        return arrPretreatment;
    }

    /**
     * 由原始决策矩阵预处理结果计算其n维行向量
     */
    private double[] nDimensionalVector(double[][] arr) {
        double[][] arrN = arr;
        double x1 = 0;
        for (int i = 0; i < arrN[0].length; i++) {
            for (int j = 0; j < arrN.length; j++) {
                for (int k = 0; k < arrN.length; k++) {
                    x1 = x1 + Math.abs(arrN[j][i] - arrN[k][i]);
                }
            }
            xn[i] = x1;
            x1 = 0;
        }
        return xn;
    }

    /**
     * 计算由原始决策矩阵预处理结果的n维行向量与专家属性权重打分矩阵的乘积
     * 对结果进行归一化处理
     */
    private double[] normalized(double[] arr) {
        double[] arrX = arr;
        double[] arrY = ArrayUtil.arrayMultiplyOneTwo(arrX, w);
        double sum = 0;
        for (int i = 0; i < arrY.length; i++) {
            sum += arrY[i];
        }
        for (int i = 0; i < arrY.length; i++) {
            arrNormalized[i] = arrY[i] / sum;
        }
        return arrNormalized;
    }

    /**
     * 计算最优组合赋权向量
     */
    private double[] optimalCombinationWeightingVector(double[] arr) {
        arrW = ArrayUtil.arrayMultiplyOneTwo(arr, w);
        return arrW;
    }

    /**
     * 计算最终多属性综合评价值
     */
    private double[] result() {
        double[][] arrB = ArrayUtil.transposeTwoDArray(arrPretreatment);
        double[] arrC = ArrayUtil.arrayMultiplyOneTwo(arrW, arrB);
        return arrC;
    }

    /**
     * 编写测试类
     */
    private void test(){
        //数据预处理 打印预处理结果
        System.out.println("原始决策矩阵的预处理结果为：");
        ArrayUtil.displayArray(pretreatment(originalDecisionMatrix));

        //由原始决策矩阵预处理结果计算其n维行向量
        System.out.println("***********************************************************************");
        System.out.println("由原始决策矩阵预处理结果计算其n维行向量为：");
        ArrayUtil.displayArray(nDimensionalVector(arrPretreatment));

        //计算由原始决策矩阵预处理结果的n维行向量与专家属性权重打分矩阵的乘积 并做归一化处理
        System.out.println("***********************************************************************");
        System.out.println("归一化处理结果为：");
        ArrayUtil.displayArray(normalized(xn));

        //计算最优组合赋权向量
        System.out.println("***********************************************************************");
        System.out.println("计算最优组合赋权向量为：");
        ArrayUtil.displayArray(optimalCombinationWeightingVector(arrNormalized));

        // 计算最终多属性综合评价值
        System.out.println("***********************************************************************");
        System.out.println("计算最终多属性综合评价值：");
        ArrayUtil.displayArray(result());
    }

    public static void main(String[] args) {
        MdcwSolverService mdSolverService = new MdcwSolverService();
        mdSolverService.test();
    }
}
