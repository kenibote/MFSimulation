package demo;

import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class PictureDemo {
	ChartPanel frame1;

	@SuppressWarnings("deprecation")
	public PictureDemo() {
		CategoryDataset dataset = getDataSet();
		JFreeChart chart = ChartFactory.createBarChart("Fruits", // 图表标题
				"Catalog", // 目录轴的显示标签
				"Number", // 数值轴的显示标签
				dataset, // 数据集
				PlotOrientation.VERTICAL, // 图表方向：水平、垂直
				true, // 是否显示图例(对于简单的柱状图必须是false)
				false, // 是否生成工具
				false // 是否生成URL链接
		);
		CategoryPlot plot = chart.getCategoryPlot();
		BarRenderer customBarRenderer = (BarRenderer) plot.getRenderer();
		customBarRenderer.setSeriesPaint(0, Color.decode("#5151A2"));
		customBarRenderer.setSeriesOutlinePaint(0, Color.BLACK);// 边框为黑色
		DecimalFormat decimalformat1 = new DecimalFormat("##.##");
		customBarRenderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", decimalformat1));
		customBarRenderer.setItemLabelsVisible(true);

		// 从这里开始
		/*
		 * CategoryPlot plot = chart.getCategoryPlot();// 获取图表区域对象 CategoryAxis
		 * domainAxis = plot.getDomainAxis(); // 水平底部列表
		 * domainAxis.setLabelFont(new Font("黑体", Font.BOLD, 14)); // 水平底部标题
		 * domainAxis.setTickLabelFont(new Font("宋体", Font.BOLD, 12)); // 垂直标题
		 * ValueAxis rangeAxis = plot.getRangeAxis();// 获取柱状
		 * rangeAxis.setLabelFont(new Font("黑体", Font.BOLD, 15));
		 * chart.getLegend().setItemFont(new Font("黑体", Font.BOLD, 15));
		 * chart.getTitle().setFont(new Font("宋体", Font.BOLD, 20));// 设置标题字体
		 */

		// 到这里结束，虽然代码有点多，但只为一个目的，解决汉字乱码问题

		frame1 = new ChartPanel(chart, true); // 这里也可以用chartFrame,可以直接生成一个独立的Frame

	}

	private static CategoryDataset getDataSet() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(100, "1", "苹果");
		dataset.addValue(200, "1", "梨子");
		dataset.addValue(300, "1", "葡萄");
		dataset.addValue(400, "1", "香蕉");
		dataset.addValue(500, "1", "荔枝");
		return dataset;
	}

	public ChartPanel getChartPanel() {
		return frame1;
	}

	public static void main(String args[]) {
		JFrame frame = new JFrame("Java数据统计图");
		frame.setLayout(new GridLayout(1, 1, 10, 10));
		frame.add(new PictureDemo().getChartPanel()); // 添加柱形图
		frame.setBounds(50, 50, 800, 600);
		frame.setVisible(true);
	}

}