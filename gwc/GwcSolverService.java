package cn.tx.evaluation.gwc;

import cn.tx.evaluation.utils.ArrayUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo class
 * 灰色白化权函数聚类法
 * indicatorData不限制路段个数与指标个数
 * indicatorEvaluationGrading指标个数不限制但每个指标的评价等级：优良中次差 这里受限
 * 在定义白化权函数的过程中 大多情况时良 中 次的时候使用典型白化权函数 这里补充了适度测度白化权函数
 *
 * @author tx
 * @date 2019/01/19
 */
public class GwcSolverService {

    /**
     * 保存txt数据的数组arrIo
     * 白化权函数阈值数组arrW
     * 灰色聚类权值数组arrG
     * 灰色聚类结果数组arrV
     */
    double[][] indicatorData = new double[8][3];
    double[][] indicatorEvaluationGrading = new double[3][4];
    double[][] arrIo;
    double[][] arrW = new double[indicatorEvaluationGrading.length][indicatorEvaluationGrading[0].length + 1];
    double[][] arrG = new double[arrW.length][arrW[0].length];
    double[][] arrV = new double[indicatorData.length][arrG[0].length];


//    /**
//     * 定义各指标数据
//     * 此实例指标为路况指标(列)：PCI ROI PSSI SRI RRD
//     */
//    private double[][] indicatorData = {
//            {80.4, 96.3, 76.9, 88.7, 11.1},
//            {90, 96.1, 75.8, 90.3, 10.6},
//            {93.8, 97.2, 97.8, 88.4, 10.1},
//            {88.4, 96.2, 91.2, 90.9, 12.5},
//            {98.4, 96.5, 86.7, 92.7, 12.2},
//            {77.7, 83.9, 75.2, 84.3, 7.6}
//    };
//
//    /**
//     * 定义各指标评价分级标准
//     * 每个指标(指标个数与indicatorData相对应)都有5个等级：优、良、中、次、差
//     */
//    private double[][] indicatorEvaluationGrading = {
//            {90, 80, 62, 40},
//            {90, 80, 62, 40},
//            {97, 92, 80, 59},
//            {85, 62, 40, 30},
//            {27, 13.5, 6.7, 4}
//    };

    /**
     * 定义求取白化权函数阈值的方法
     */
    private double[][] whiteningWeightFunctionThreshold(double[][] indicatorEvaluationGrading) {
        for (int i = 0; i < indicatorEvaluationGrading.length; i++) {
            for (int j = 0; j < indicatorEvaluationGrading[i].length + 1; j++) {
                if (j == 0) {
                    arrW[i][j] = indicatorEvaluationGrading[i][j];
                } else if (j == indicatorEvaluationGrading[i].length) {
                    arrW[i][j] = indicatorEvaluationGrading[i][j - 1];
                } else {
                    arrW[i][j] = (indicatorEvaluationGrading[i][j - 1] + indicatorEvaluationGrading[i][j]) / 2;
                }
            }
        }
        return arrW;
    }

    /**
     * 定义求取灰色聚类权值的方法(变权聚类法)
     */
    private double[][] greyClusterWeight(double[][] arrW) {
        double[] arrX = new double[arrW[0].length];
        for (int i = 0; i < arrW[0].length; i++) {
            for (int j = 0; j < arrW.length; j++) {
                arrX[i] += arrW[j][i];
            }
        }

        for (int i = 0; i < arrW.length; i++) {
            for (int j = 0; j < arrW[i].length; j++) {
                arrG[i][j] = arrW[i][j] / arrX[j];
            }
        }
        return arrG;
    }

    /**
     * 定义白化权函数
     * 优为上限测度白化权函数
     * 良中次为典型白化权函数(若为适应测度白化权函数时 良为good1 中为mid1 次为pass1)
     * 差为下限测度白化权函数
     * 优 excellent
     * 良 good
     * 中 mid
     * 次 pass
     * 差 bad
     * 形参 路况指标index 各指标评价分级标准数组 指标索引值i（0-4)分别对应5个指标）
     */
    private double excellent(double index, double[][] indicatorEvaluationGrading, int i) {
        double result;
        if (index < indicatorEvaluationGrading[i][1]) {
            result = 0;
        } else if (index >= indicatorEvaluationGrading[i][1] && index < indicatorEvaluationGrading[i][0]) {
            result = (index - indicatorEvaluationGrading[i][1]) / (indicatorEvaluationGrading[i][0] - indicatorEvaluationGrading[i][1]);
        } else {
            result = 1;
        }
        return result;
    }

    private double good(double index, double[][] indicatorEvaluationGrading, int i) {
        double result;
        if (index < indicatorEvaluationGrading[i][2] || index > 100) {
            result = 0;
        } else if (index >= indicatorEvaluationGrading[i][2] && index < indicatorEvaluationGrading[i][1]) {
            result = (index - indicatorEvaluationGrading[i][2]) / (indicatorEvaluationGrading[i][1] - indicatorEvaluationGrading[i][2]);
        } else if (index >= indicatorEvaluationGrading[i][1] && index < indicatorEvaluationGrading[i][0]) {
            result = 1;
        } else {
            result = (100 - index) / (100 - indicatorEvaluationGrading[i][0]);
        }
        return result;
    }

    private double good1(double index, double[][] indicatorEvaluationGrading, int i) {
        double result;
        if (index < indicatorEvaluationGrading[i][1] || index > 100) {
            result = 0;
        } else if (index >= indicatorEvaluationGrading[i][1] && index < indicatorEvaluationGrading[i][0]) {
            result = (index - indicatorEvaluationGrading[i][1]) / (indicatorEvaluationGrading[i][0] - indicatorEvaluationGrading[i][1]);
        } else {
            result = (100 - index) / (100 - indicatorEvaluationGrading[i][0]);
        }
        return result;
    }

    private double mid(double index, double[][] indicatorEvaluationGrading, int i) {
        double result;
        if (index < indicatorEvaluationGrading[i][3] || index > indicatorEvaluationGrading[i][0]) {
            result = 0;
        } else if (index >= indicatorEvaluationGrading[i][3] && index < indicatorEvaluationGrading[i][2]) {
            result = (index - indicatorEvaluationGrading[i][3]) / (indicatorEvaluationGrading[i][2] - indicatorEvaluationGrading[i][3]);
        } else if (index >= indicatorEvaluationGrading[i][2] && index < indicatorEvaluationGrading[i][1]) {
            result = 1;
        } else {
            result = (indicatorEvaluationGrading[i][0] - index) / (indicatorEvaluationGrading[i][0] - indicatorEvaluationGrading[i][1]);
        }
        return result;
    }

    private double mid1(double index, double[][] indicatorEvaluationGrading, int i) {
        double result;
        if (index < indicatorEvaluationGrading[i][2] || index > indicatorEvaluationGrading[i][0]) {
            result = 0;
        } else if (index >= indicatorEvaluationGrading[i][2] && index < indicatorEvaluationGrading[i][1]) {
            result = (index - indicatorEvaluationGrading[i][2]) / (indicatorEvaluationGrading[i][1] - indicatorEvaluationGrading[i][2]);
        } else {
            result = (indicatorEvaluationGrading[i][0] - index) / (indicatorEvaluationGrading[i][0] - indicatorEvaluationGrading[i][1]);
        }
        return result;
    }

    private double pass(double index, double[][] indicatorEvaluationGrading, int i) {
        double result;
        if (index < indicatorEvaluationGrading[i][3] / 2 || index > indicatorEvaluationGrading[i][1]) {
            result = 0;
        } else if (index >= indicatorEvaluationGrading[i][3] / 2 && index < indicatorEvaluationGrading[i][3]) {
            result = (index - indicatorEvaluationGrading[i][3] / 2) / (indicatorEvaluationGrading[i][3] - indicatorEvaluationGrading[i][3] / 2);
        } else if (index >= indicatorEvaluationGrading[i][3] && index < indicatorEvaluationGrading[i][2]) {
            result = 1;
        } else {
            result = (indicatorEvaluationGrading[i][1] - index) / (indicatorEvaluationGrading[i][1] - indicatorEvaluationGrading[i][2]);
        }
        return result;
    }

    private double pass1(double index, double[][] indicatorEvaluationGrading, int i) {
        double result;
        if (index < indicatorEvaluationGrading[i][3] || index > indicatorEvaluationGrading[i][1]) {
            result = 0;
        } else if (index >= indicatorEvaluationGrading[i][3] && index < indicatorEvaluationGrading[i][2]) {
            result = (index - indicatorEvaluationGrading[i][3]) / (indicatorEvaluationGrading[i][2] - indicatorEvaluationGrading[i][3]);
        } else {
            result = (indicatorEvaluationGrading[i][1] - index) / (indicatorEvaluationGrading[i][1] - indicatorEvaluationGrading[i][2]);
        }
        return result;
    }

    private double bad(double index, double[][] indicatorEvaluationGrading, int i) {
        double result;
        if (index < 0 || index > indicatorEvaluationGrading[i][2]) {
            result = 0;
        } else if (index >= indicatorEvaluationGrading[i][3] && index < indicatorEvaluationGrading[i][2]) {
            result = (indicatorEvaluationGrading[i][2] - index) / (indicatorEvaluationGrading[i][2] - indicatorEvaluationGrading[i][3]);
        } else {
            result = 1;
        }
        return result;
    }

    /**
     * 定义变权聚类法
     * 计算灰色聚类结果
     */
    private double[][] variableWeightClustering(double[][] indicatorData, double[][] arrG) {
        if (indicatorData[0].length != arrG.length) {
            return null;
        }
        for (int i = 0; i < indicatorData.length; i++) {
            for (int j = 0; j < arrG[0].length; j++) {
                for (int k = 0; k < indicatorData[0].length; k++) {
                    if (j == 0) {
                        arrV[i][j] += excellent(indicatorData[i][k], indicatorEvaluationGrading, k) * arrG[k][j];
                    } else if (j == 1) {
                        arrV[i][j] += good(indicatorData[i][k], indicatorEvaluationGrading, k) * arrG[k][j];
                    } else if (j == 2) {
                        arrV[i][j] += mid(indicatorData[i][k], indicatorEvaluationGrading, k) * arrG[k][j];
                    } else if (j == 3) {
                        arrV[i][j] += pass(indicatorData[i][k], indicatorEvaluationGrading, k) * arrG[k][j];
                    } else {
                        arrV[i][j] += bad(indicatorData[i][k], indicatorEvaluationGrading, k) * arrG[k][j];
                    }
                }
            }
        }
        return arrV;
    }

    /**
     * 定义求取灰色变权聚类的最大值方法
     * 0代表优秀 1代表良好 2代表中等 3代表次 4代表差
     */
    private void indexResult(double[][] arrV) {
        double maxIndex;
        double[] k = new double[arrV.length];
        for (int i = 0; i < arrV.length; i++) {
            maxIndex = arrV[i][0];
            for (int j = 0; j < arrV[i].length; j++) {
                if (maxIndex < arrV[i][j]) {
                    maxIndex = arrV[i][j];
                    k[i] = j;
                }
            }
            System.out.println("第" + (i + 1) + "个路况的灰色聚类法评价最大值为：" + String.format("%.4f", maxIndex) + "  评价的等级为：" + k[i]);
        }
    }

    /**
     * I/O流读取初始数据
     */
    private double[][] ioRead(String filePath) {
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
                String[] arr = str.split(",");
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
            ArrayUtil.displayArray(array);
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
     * 定义测试方法
     */
    private void test() {

        //打印I/O输入各指标数据
        System.out.println("打印I/O输入各指标数据：");
        ioRead("C:/Study/评估项目/评估项目部分算法流程/算法/灰色白化权函数聚类法/灰色白化权函数聚类法实例输入/gwc3.txt");
        indicatorData = arrIo;

        //打印I/O输入各指标评价分级标准
        System.out.println("***********************************************************************");
        System.out.println("打印I/O输入各指标评价分级标准：");
        ioRead("C:/Study/评估项目/评估项目部分算法流程/算法/灰色白化权函数聚类法/灰色白化权函数聚类法实例输入/ieg3.txt");
        indicatorEvaluationGrading = arrIo;

        //白化权函数阈值计算结果
        System.out.println("***********************************************************************");
        System.out.println("白化权函数阈值计算结果：");
        ArrayUtil.displayArray(whiteningWeightFunctionThreshold(indicatorEvaluationGrading));

        //打印灰色聚类权值结果
        System.out.println("***********************************************************************");
        System.out.println("打印灰色聚类权值结果：");
        ArrayUtil.displayArray(greyClusterWeight(arrW));

        //计算灰色聚类结果
        System.out.println("***********************************************************************");
        System.out.println("计算灰色聚类结果：");
        ArrayUtil.displayArray(variableWeightClustering(indicatorData, arrG));

        //求取灰色变权聚类的最大值结果
        System.out.println("***********************************************************************");
        indexResult(arrV);
    }

    public static void main(String[] args) {
        GwcSolverService gwcSolverService = new GwcSolverService();
        gwcSolverService.test();

    }
}
