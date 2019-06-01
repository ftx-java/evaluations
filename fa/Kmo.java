package cn.tx.evaluation.fa;

import Jama.Matrix;

/**
 * KMO检验
 * r为原始数据的相关系数矩阵
 *
 * @author tx
 * @date 2019/03/16
 */
public class Kmo {

    /**
     * KMO检验
     *
     * @param R 相关系数矩阵
     * @return KMO检验值
     */
    public static double kmo(double[][] R) {
        double score = 0;
        //避免数组的起别名问题 将原来的相关系数矩阵拷贝进行操作 不会改变R原有的值
        double[][] r = new double[R.length][R[0].length];
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r[0].length; j++) {
                r[i][j] = R[i][j];
            }
        }

        Matrix X = new Matrix(r);
        //求取X的逆矩阵
        Matrix iX = X.inverse();
        //将iX的对角线的元素取倒数，其余元素都变为0，得到矩阵S
        Matrix S = new Matrix(r);
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r[0].length; j++) {
                if (i == j) {
                    S.set(i, j, 1 / iX.get(i, j));
                } else {
                    S.set(i, j, 0);
                }
            }
        }

        //AIS是反映像协方差矩阵
        Matrix AIS = S.times(iX).times(S);
        //IS是映像协方差矩阵
        Matrix IS = X.plus(AIS).minus(S.times(2));
        //将矩阵AIS对角线上的元素开平方，并且将其余元素都变成0，得到矩阵Dai
        Matrix Dai = new Matrix(r);
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r[0].length; j++) {
                if (i == j) {
                    Dai.set(i, j, Math.sqrt(AIS.get(i, j)));
                } else {
                    Dai.set(i, j, 0);
                }
            }
        }

        //IR是映像相关矩阵
        Matrix IR = Dai.inverse().times(IS).times(Dai.inverse());
        //AIR是反映像相关矩阵
        Matrix AIR = Dai.inverse().times(AIS).times(Dai.inverse());

        //a1 = sum((AIR - diag(diag(AIR))).^2);    %diag(diag(AIR))表示将矩阵AIR的对角线取出来，
        // 再构造成一个对角矩阵（即对角线之外元素都是0）；
        // . 表示将偏相关系数矩阵AIR - diag(diag(AIR))的每一个元素乘方，这样得到矩阵a1。
        Matrix AIR1 = new Matrix(r);
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r[0].length; j++) {
                if (i == j) {
                    AIR1.set(i, j, 0);

                } else {
                    AIR1.set(i, j, AIR.get(i, j));
                }
            }
        }

        double[][] AIR2 = AIR1.getArray();
        double[][] a = new double[AIR2.length][AIR2[0].length];
        for (int i = 0; i < AIR2.length; i++) {
            for (int j = 0; j < AIR2[0].length; j++) {
                a[i][j] = AIR2[i][j] * AIR2[i][j];
            }
        }

        double[] a1 = new double[a[0].length];
        for (int i = 0; i < a[0].length; i++) {
            for (int j = 0; j < a.length; j++) {
                a1[i] += a[j][i];
            }
        }

        //AA = sum(a);%得到偏相关系数矩阵AIR - diag(diag(AIR))中所有偏相关系数的平方和AA，
        // 但不考虑其对角线上的数值。
        double AA = 0;
        for (int i = 0; i < a1.length; i++) {
            AA += a1[i];
        }

        //b = sum((X - eye(size(X))).^2);  %eye（）是单位矩阵；
        // b就是将相关系数矩阵R中每一个元素乘方，但R对角线元素全部变成0
        //避免数组的起别名问题 将原来的相关系数矩阵拷贝进行操作 不会改变R原有的值
        double[][] r1 = new double[R.length][R[0].length];
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < r1[0].length; j++) {
                r1[i][j] = R[i][j];
            }
        }

        double[][] b = new double[r1.length][r1[0].length];
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < r1[0].length; j++) {
                if (i == j) {
                    b[i][j] = 0;
                } else {
                    b[i][j] = r1[i][j] * r1[i][j];
                }
            }
        }

        double[] b1 = new double[b[0].length];
        for (int i = 0; i < b[0].length; i++) {
            for (int j = 0; j < b.length; j++) {
                b1[i] += b[j][i];
            }
        }

        //BB = sum(b);%BB就是所有变量之间（不包括变量自己与自己）的相关系数的平方和。
        double BB = 0;
        for (int i = 0; i < a1.length; i++) {
            BB += b1[i];
        }

        score = BB / (AA + BB);
        System.out.println("***********************************************************************");
        System.out.println("KMO检验值为：" + String.format("%.4f", score));

        //对是否适合因子分析进行判别
        System.out.print("因子分析适合情况:");
        if (score > 0.9) {
            System.out.println("非常合适");
        } else if (score <= 0.9 && score > 0.8) {
            System.out.println("非常合适");
        } else if (score <= 0.8 && score > 0.7) {
            System.out.println("很合适");
        } else if (score <= 0.7 && score > 0.6) {
            System.out.println("合适");
        } else if (score <= 0.6 && score > 0.5) {
            System.out.println("不太合适");
        } else {
            System.out.println("不合适");
        }
        return score;
    }

}
