package cn.tx.evaluation.lefit;

import cn.tx.evaluation.utils.ArrayUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 回归算法之指数拟合加对数拟合
 *
 * @author tx
 * @date 2019/05/11
 */
public class LEfit {
    public static void main(String[] args) {
        LEfit lEfit = new LEfit();
        lEfit.ioRead("C:\\Study\\评估项目\\评估项目部分算法流程\\算法\\回归算法\\指数对数拟合\\test.txt");
        lEfit.Test(lEfit.arrIo, 1);
    }

    /**
     * 测试函数
     *
     * @param input
     * @param k
     */
    public void Test(double[][] input, int k) {
        //处理输入数据
        LEfit lEfit = new LEfit();
        double[] arrx = new double[input.length];
        double[] arry = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                if (j == 0) {
                    arrx[i] = input[i][j];
                } else if (j == 1) {
                    arry[i] = input[i][j];
                }
            }
        }
        //选择拟合方式 0:指数拟合 1：对数拟合
        switch (k) {
            case 0:
                double[] y0 = lEfit.expFitting(arrx, arry);
                //画出拟合曲线
                lEfit.Figure(arrx, y0, k);
                break;
            case 1:
                double[] y1 = lEfit.logFitting(arrx, arry);
                //画出拟合曲线
                lEfit.Figure(arrx, y1, k);
            default:
        }

    }

    /**
     * 指数拟合 y = a*exp(bx) result[0] = a  result[1] = b
     * result[2] 数据点数  result[3] 确定系数
     *
     * @param x
     * @param y
     * @return
     */
    public double[] expFitting(double x[], double y[]) {
        int size = x.length;
        double xmean = 0.0;
        double ymean = 0.0;
        double rss = 0;
        double tss = 0;
        double result[] = new double[4];
        for (int i = 0; i < size; i++) {
            xmean += x[i];
            y[i] = Math.log(y[i]);
            ymean += y[i];
        }

        xmean /= size;
        ymean /= size;
        double sumx2 = 0.0f;
        double sumxy = 0.0f;
        for (int i = 0; i < size; i++) {
            sumx2 += (x[i] - xmean) * (x[i] - xmean);
            sumxy += (y[i] - ymean) * (x[i] - xmean);
        }

        double b = sumxy / sumx2;
        double a = ymean - b * xmean;
        for (int i = 0; i < size; i++) {
            rss += (y[i] - (a + b * x[i])) * (y[i] - (a + b * x[i]));
            tss += (y[i] - ymean) * (y[i] - ymean);
        }

        double r2 = Math.abs(1 - (rss / (size - 1 - 1)) / (tss / (size - 1)));
        //打印结果 存储参数计算结果
        System.out.println("进行指数拟合");
        System.out.println("决定系数" + r2);
        a = Math.exp(a);
        System.out.println("a = " + a + ";b= " + b);
        System.out.println("指数拟合结束");
        result[0] = a;
        result[1] = b;
        result[2] = x.length;
        result[3] = r2;
        //拟合
        double[] res = new double[y.length];
        for (int i = 0; i < size; i++) {
            res[i] = (a * Math.exp(b * x[i]));
        }
        return res;
    }

    /**
     * 对数拟合 y = ln(a * x + b) result[0] = a  result[1] = b
     * result[2] 数据点数 result[3] 确定系数
     *
     * @param x
     * @param y
     */
    public double[] logFitting(double x[], double y[]) {
        int size = x.length;
        double xmean = 0.0;
        double ymean = 0.0;
        double rss = 0;
        double tss = 0;
        double result[] = new double[4];
        for (int i = 0; i < size; i++) {
            xmean += x[i];
            y[i] = Math.exp(y[i]);
            ymean += y[i];
        }

        xmean /= size;
        ymean /= size;
        double sumx2 = 0.0f;
        double sumxy = 0.0f;
        for (int i = 0; i < size; i++) {
            sumx2 += (x[i] - xmean) * (x[i] - xmean);
            sumxy += (y[i] - ymean) * (x[i] - xmean);
        }

        double b = sumxy / sumx2;
        double a = ymean - b * xmean;
        for (int i = 0; i < size; i++) {
            rss += (y[i] - (a + b * x[i])) * (y[i] - (a + b * x[i]));
            tss += (y[i] - ymean) * (y[i] - ymean);
        }

        double r2 = Math.abs(1 - (rss / (size - 1 - 1)) / (tss / (size - 1)));
        //打印结果 存储参数计算结果
        System.out.println("进行对数拟合");
        System.out.println("决定系数" + r2);
        System.out.println("a = " + a + ";b= " + b);
        System.out.println("对数拟合结束");
        result[0] = a;
        result[1] = b;
        result[2] = x.length;
        result[3] = r2;
        //拟合
        double[] res = new double[y.length];
        for (int i = 0; i < size; i++) {
            res[i] = Math.log(Math.abs(a * x[i] + b));
        }
        return res;
    }

    /**
     * 画图
     *
     * @param x
     * @param res
     * @param k
     */
    public void Figure(double[] x, double[] res, int k) {
        XYSeries series = new XYSeries("xySeries");
        for (int i = 0; i < x.length; i++) {
            double arrx = x[i];
            double arry = res[i];
            series.add(arrx, arry);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        //选择画的曲线类型 0为指数拟合 1为对数拟合
        switch (k) {
            case 0:
                JFreeChart chart = ChartFactory.createXYLineChart(
                        "y = a*exp(bx)", // chart title
                        "x", // x axis label
                        "y", // y axis label
                        dataset, // data
                        PlotOrientation.VERTICAL,
                        false, // include legend
                        false, // tooltips
                        false // urls
                );
                ChartFrame frame = new ChartFrame("my picture", chart);
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                break;
            case 1:
                JFreeChart chart1 = ChartFactory.createXYLineChart(
                        " y = ln(a * x + b)", // chart title
                        "x", // x axis label
                        "y", // y axis label
                        dataset, // data
                        PlotOrientation.VERTICAL,
                        false, // include legend
                        false, // tooltips
                        false // urls
                );
                ChartFrame frame1 = new ChartFrame("my picture", chart1);
                frame1.pack();
                frame1.setVisible(true);
                frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            default:
        }
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
                //txt中每行数据中每个数据间隔为，
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
