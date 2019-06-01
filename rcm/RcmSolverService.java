package cn.tx.evaluation.rcm;

import cn.tx.evaluation.utils.ArrayUtil;

/**
 * Demo class
 * 环比系数法
 *
 * @author tx
 * @date 2019/01/15
 */
public class RcmSolverService {
    /**
     * 定义专家权重打分数组
     * 定义计算修正权重数组
     * 定义各指标权重数组
     */
    private double[] A = {6, 3, 2, 1.5, 1};
    private double[] B = new double[A.length];
    private double[] C = new double[B.length];

    /**
     * 定义计算修正权重方法
     */
    private double[] rcmSolver(double[] arr) {
        for (int i = arr.length - 1; i >= 0; i--) {
            if (i == arr.length - 1) {
                B[i] = 1;
            } else {
                B[i] = arr[i] * B[i + 1];
            }
        }
        return B;
    }

    /**
     * 定义各指标权重计算方法
     */
    private double[] indexWeight(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }

        for (int i = 0; i < arr.length; i++) {
            C[i] = arr[i] / sum;
        }
        return C;
    }

    /**
     * 编写测试类
     */
    private void test(){
        //打印修正权重计算结果
        System.out.println("***********************************************************************");
        System.out.println("打印修正权重计算结果：");
        ArrayUtil.displayArray(rcmSolver(A));

        //打印各指标权重计算结果
        System.out.println("***********************************************************************");
        System.out.println("打印各指标权重计算结果：");
        ArrayUtil.displayArray(indexWeight(B));
    }

    public static void main(String[] args) {
        RcmSolverService rcmSolverService = new RcmSolverService();
        rcmSolverService.test();
    }
}
