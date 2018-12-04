package tool;

import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;

public class DrawPicture {
	static int width = 800;
	static int height = 600;
	static HashMap<Integer, String> colorMap = new HashMap<>();
	static {
		colorMap.put(0, "#5151A2"); // "blue"
		colorMap.put(1, "#FF9224"); // "yellow"
		colorMap.put(2, "#8F4588"); // "purple"
	}

	@SuppressWarnings("deprecation")
	public static void DrawChart(DefaultCategoryDataset dataset, String chartTitle, String xName, String yName) {
		JFreeChart chart = ChartFactory.createBarChart(chartTitle, // 图表标题
				xName, // 目录轴的显示标签
				yName, // 数值轴的显示标签
				dataset, // 数据集
				PlotOrientation.VERTICAL, // 图表方向：水平、垂直
				true, // 是否显示图例(对于简单的柱状图必须是false)
				false, // 是否生成工具
				false // 是否生成URL链接
		);

		// 设置柱状图颜色
		CategoryPlot plot = chart.getCategoryPlot();
		BarRenderer customBarRenderer = (BarRenderer) plot.getRenderer();
		for (int i = 0; i < colorMap.size(); i++) {
			customBarRenderer.setSeriesPaint(i, Color.decode(colorMap.get(i))); // 设置柱状图颜色
			customBarRenderer.setSeriesOutlinePaint(i, Color.BLACK); // 边框为黑色
		}

		// 设置文字标签
		DecimalFormat decimalformat1 = new DecimalFormat("##.##");
		customBarRenderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", decimalformat1));
		customBarRenderer.setItemLabelsVisible(true);

		// 显示图表
		ChartPanel chartpanel = new ChartPanel(chart, true);
		Frame(chartpanel);
	}

	public static void DrawTimeLine(XYDataset xydataset, String chartTitle, String xName, String yName) {

		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(chartTitle, xName, yName, xydataset, true, true,
				true);

		// XYPlot xyplot = (XYPlot) jfreechart.getPlot();
		// DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
		// dateaxis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

		// 显示图标
		ChartPanel frame = new ChartPanel(jfreechart, true);
		Frame(frame);
	}

	private static void Frame(ChartPanel panel) {
		JFrame frame = new JFrame("Java Picture");
		frame.setLayout(new GridLayout(1, 1, 10, 10));
		frame.add(panel);
		frame.setBounds(50, 50, width, height);
		frame.setVisible(true);
	}

	public static void waitExit() {
		// 等待推出
		Scanner sc = new Scanner(System.in);
		System.out.println(sc.nextLine());
		sc.close();
	}

}
