package cn.tx.evaluation.md;

import cn.tx.evaluation.utils.ArrayUtil;

import java.util.ArrayList;

/**
 * Demo class
 * 离差最大化算法
 *
 * @author tx
 * @date 2019/01/21
 */
public class MdSolverService {

    private MdSolverService() {
    }

    /**
     * 预处理结果 arrPretreatment
     * 由原始决策矩阵预处理结果计算其n维行向量 xn
     * 归一化处理结果 arrNormalized
     */
    private double[][] arrPretreatment = new double[4][4];
    private double[] xn = new double[arrPretreatment.length];
    private double[] arrNormalized = new double[xn.length];

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
     * 对结果进行归一化处理
     */
    private double[] normalized(double[] arr) {
        double[] arrX = arr;
        double sum = 0;
        for (int i = 0; i < arrX.length; i++) {
            sum += arrX[i];
        }
        for (int i = 0; i < arrX.length; i++) {
            arrNormalized[i] = arrX[i] / sum;
        }
        return arrNormalized;
    }

    /**
     * 计算最终多属性综合评价值
     */
    private double[] result() {
        double[][] arrB = ArrayUtil.transposeTwoDArray(arrPretreatment);
        double[] arrC = ArrayUtil.arrayMultiplyOneTwo(arrNormalized, arrB);
        return arrC;
    }

    /**
     * 编写测试类
     */
    private void test() {
        //数据预处理 打印预处理结果
        System.out.println("原始决策矩阵的预处理结果为：");
        ArrayUtil.displayArray(pretreatment(originalDecisionMatrix));

        //由原始决策矩阵预处理结果计算其n维行向量
        System.out.println("***********************************************************************");
        System.out.println("由原始决策矩阵预处理结果计算其n维行向量为：");
        ArrayUtil.displayArray(nDimensionalVector(arrPretreatment));

        //归一化处理
        System.out.println("***********************************************************************");
        System.out.println("归一化处理结果为：");
        ArrayUtil.displayArray(normalized(xn));

        // 计算最终多属性综合评价值
        System.out.println("***********************************************************************");
        System.out.println("计算最终多属性综合评价值：");
        ArrayUtil.displayArray(result());
    }

    public static void main(String[] args) {
        MdSolverService mdSolverService = new MdSolverService();
        mdSolverService.test();
    }
}
