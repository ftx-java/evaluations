package cn.tx.evaluation.fcm;

import cn.tx.evaluation.utils.ArrayUtil;

import java.lang.Math;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 模糊聚类 FCM
 *
 * @author tx
 * @date 2019/05/06
 */
public class Fcm {
    public static void main(String[] args) {
        //numOfClasses--C:分类数
        int numOfClasses = 2;
        //e:精度
        double E = 0.001;
        //q:FCM样本指数
        double Q = 2;
        //选择距离计算公式//1.Chebyshev切比雪夫距离 //2. Euclid欧式距离 3. //Hamming汉明距离
        int D = 2;
        //读取数据集.txt
        Fcm fcm = new Fcm();
        fcm.ioRead("C:\\Study\\评估项目\\评估项目部分算法流程\\算法\\聚类算法\\FCM聚类\\test.txt");
        //特征指标矩阵
        double x[][] = fcm.arrIo;
        //numOfData--N:样本个数
        int numOfData = x.length;
        //numOfFeatures--M:特征值个数
        int numOfFeatures = x[0].length;
        //由特征指标矩阵X和初始分类矩阵R0和FCM公式，得到的聚类中心矩阵
        double VV[][];
        //由特征指标矩阵X和聚类中心V修正得到的隶属矩阵RR
        double RR[][];
        //确定性矩阵
        double CertainClass[][] = new double[100][];
        int i, j, k;
        double ClassF[];

        //初始化初始分类矩阵R0 对于所有的点n，n对于所有的类别的隶属度函数之和为1。
        //R0:初始分类矩阵,生成满足条件的随机数。
        double R0[][] = new double[numOfClasses][numOfData];
        for (i = 0; i < numOfData; i++) {
            double sum = 0;
            for (j = 0; j < numOfClasses; j++) {
                if (j == numOfClasses - 1) {
                    R0[j][i] = 1 - sum;
                } else {
                    R0[j][i] = (1 - sum) * Math.random();
                    sum = sum + R0[j][i];
                }
            }
        }

        System.out.println(" ---------------FCM聚类算法--------------");
        System.out.println("初始化初始隶属度矩阵R0");
        fcm.display(R0);
        //由构造函数创建类的实例对象
        double max;
        int times = 0;
        do {
            max = 0;
            //由特征指标矩阵X和初始分类矩阵R0和FCM公式，计算聚类中心的坐标
            VV = fcm.FCMCenter(R0, x, numOfData, numOfFeatures, numOfClasses, Q);
            //由特征指标矩阵X和聚类中心V修正分类矩阵RR
            RR = fcm.modifyR(x, VV, numOfData, numOfFeatures, numOfClasses, D, Q);
            System.out.println("第" + (times + 1) + "次迭代的聚类中心矩阵");
            fcm.display(VV);
            System.out.println("第" + (times + 1) + "次迭代的隶属矩阵");
            fcm.display(RR);
            //计算RR和R0的精度差
            for (i = 0; i < numOfClasses; i++) {
                for (j = 0; j < numOfData; j++) {
                    if (Math.abs(RR[i][j] - R0[i][j]) > max) {
                        max = Math.abs(RR[i][j] - R0[i][j]);
                    }
                }
            }
            if (max > E) {
                for (i = 0; i < numOfClasses; i++) {
                    for (j = 0; j < numOfData; j++) {
                        R0[i][j] = RR[i][j];
                    }
                }
            }
            times++;
        } while (max > E);

        //---------------------------------显示实验结果---------------------------------
        //输出迭代次数
        System.out.println(" ");
        System.out.println("迭代了   " + times + "   次");
        //输出聚类中心
        System.out.println("-------------------聚类中心矩阵--------------------VV");
        System.out.println();
        fcm.display(VV);
        //输出分类矩阵
        System.out.println("---------------------隶属矩阵--------------------RR");
        System.out.println();
        fcm.display(RR);
        //输出确定性分类
        System.out.println("------------------确定性分类矩阵--------------------CR");
        System.out.println();
        CertainClass = fcm.certainClass(RR, numOfData, numOfClasses);
        //计算确定性分类
        fcm.display(CertainClass);
    }

    /**
     * 计算距离 -d:距离公式选择
     */
    public double compute_dis(double mat1[][], int k_row, double mat2[][], int i_row, int n_col, int d) {
        double x, max;
        int j;
        max = 0;
        switch (d) {
            case 1:
                max = 0;
                for (j = 0; j < n_col; j++) {
                    x = Math.abs(mat1[k_row][j] - mat2[i_row][j]);
                    if (x > max) {
                        max = x;
                    }
                }
                return max;
            case 2:
                max = 0;
                for (j = 0; j < n_col; j++) {
                    max += Math.pow((mat1[k_row][j] - mat2[i_row][j]), 2);
                }
                return Math.sqrt(max);
            case 3:
                max = 0;
                for (j = 0; j < n_col; j++) {
                    max += Math.abs(mat1[k_row][j] - mat2[i_row][j]);
                }
                return max;
                default:
        }
        return max;
    }

    /**
     * 计算聚类中心
     */
    public double[][] FCMCenter(double R[][], double X[][], int numOfData, int numOfFeatures, int numOfClasses, double Q) {
        double V[][] = new double[numOfClasses][numOfFeatures];
        double n_sum, m_sum;
        for (int i = 0; i < numOfClasses; i++) {
            for (int j = 0; j < numOfFeatures; j++) {
                n_sum = 0;
                m_sum = 0;
                for (int k = 0; k < numOfData; k++) {
                    n_sum += Math.pow(R[i][k], Q) * X[k][j];
                    m_sum += Math.pow(R[i][k], Q);
                }
                V[i][j] = n_sum / m_sum;
            }
        }
        return V;
    }

    /**
     * 修正隶属矩阵
     */
    public double[][] modifyR(double X[][], double V[][], int numOfData, int numOfFeatures, int numOfClasses, int D, double q) {
        double kj_sum;
        double R1[][] = new double[numOfClasses][numOfData];
        int i, k, j;
        for (i = 0; i < numOfClasses; i++) {
            for (k = 0; k < numOfData; k++) {
                kj_sum = 0;
                for (j = 0; j < numOfClasses; j++) {
                    if (j != i) {
                        kj_sum += Math.pow((compute_dis(X, k, V, i, numOfFeatures, D) / compute_dis(X, k, V, j, numOfFeatures, D)), (2 / (q - 1)));

                    }
                }
                R1[i][k] = Math.pow((kj_sum + 1), -1);
            }
        }
        return R1;
    }

    /**
     * 输出矩阵
     */
    public void display(double Matrix[][]) {
        for (int i = 0; i < Matrix.length; i++) {
            for (int j = 0; j < Matrix[0].length; j++) {
                System.out.printf("%.4f", Matrix[i][j]);
                System.out.print("  ");
            }
            System.out.println("");
        }
    }

    /**
     * 转换为确定性分类
     */
    public double[][] certainClass(double RR[][], int numOfData, int numOfClasses) {
        double max;
        double CertainClass[][] = new double[numOfClasses][numOfData];
        for (int j = 0; j < numOfData; j++) {
            max = RR[0][j];
            CertainClass[0][j] = 1;
            for (int i = 1; i < numOfClasses; i++) {
                if (RR[i][j] > max) {
                    max = RR[i][j];
                    CertainClass[i][j] = 1;
                    if (i == 1) {
                        CertainClass[0][j] = 0;
                    }
                } else {
                    CertainClass[i][j] = 0;
                }
            }
        }
        return CertainClass;
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

