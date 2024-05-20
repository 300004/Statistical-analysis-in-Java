package com.example.visualization;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.Values;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.util.stream.Collectors;

import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DataVisualizer {


    public static void createPieChart(Table table, String column, String[] categories) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        StringColumn stringColumn = table.stringColumn(column);
        String[] stringArray = new String[stringColumn.size()];
        for (int i = 0; i < stringColumn.size(); i++) {
            stringArray[i] = stringColumn.get(i);
        }
    
        for (String category : categories) {
            int count = 0;
            for (String value : stringArray) {
                if (value.equals(category)) {
                    count++;
                }
            }
            dataset.setValue(category, count);
        }
    
        JFreeChart chart = ChartFactory.createPieChart(
                column + " Count",
                dataset,
                true,
                true,
                false
        );
    
        JFrame frame = new JFrame("Pie Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        frame.getContentPane().add(chartPanel);
    
        frame.pack();
        frame.setVisible(true);
    }
    
    
    public static void createHistogram(Table table, String column) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        StringColumn stringColumn = table.stringColumn(column);
        String[] stringArray = new String[stringColumn.size()];
        for (int i = 0; i < stringColumn.size(); i++) {
            stringArray[i] = stringColumn.get(i);
    }

        int shipped = 0;
        int shippedDelivered = 0;
        int cancelled = 0;

        for (String value : stringArray) {
            if (value.equals("Shipped")) {
                shipped++;
            } else if (value.equals("Shipped - Delivered to Buyer")) {
                shippedDelivered++;
            } else if (value.equals("Cancelled")) {
                cancelled++;
            }
        }

        dataset.addValue(shipped, "Status", "Shipped");
        dataset.addValue(shippedDelivered, "Status", "Shipped-Delivered to Buyer");
        dataset.addValue(cancelled, "Status", "Cancelled");

        JFreeChart chart = ChartFactory.createBarChart(
            "Order Status Count",
            "Order Status",
            "Count",
            dataset
    );



    JFrame frame = new JFrame("Histogram");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(500, 300));
    frame.getContentPane().add(chartPanel);

    frame.pack();
    frame.setVisible(true);
}

public static void createShipPieChart(Table table, String column) {
    StringColumn stringColumn = table.stringColumn(column);
    String[] stringArray = new String[stringColumn.size()];
    for (int i = 0; i < stringColumn.size(); i++) {
        stringArray[i] = stringColumn.get(i);
    }

    int expedited = 0;
    int standard = 0;

    for (String value : stringArray) {
        if (value.equals("Expedited")) {
            expedited++;
        } else if (value.equals("Standard")) {
            standard++;
        }
    }

    DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
    dataset.setValue("Expedited", expedited);
    dataset.setValue("Standard", standard);

    JFreeChart chart = ChartFactory.createPieChart(
            "Ship Service Level Count",
            dataset
    );

    JFrame frame = new JFrame("Pie Chart");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(500, 300));
    frame.getContentPane().add(chartPanel);

    frame.pack();
    frame.setVisible(true);
}
public static void createCityHistogram(Table table, String column) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    StringColumn stringColumn = table.stringColumn(column);
    Map<String, Integer> counts = new HashMap<>();
    
    for (int i = 0; i < stringColumn.size(); i++) {
        String city = stringColumn.get(i);
        counts.put(city, counts.getOrDefault(city, 0) + 1);
    }

    Map<String, Integer> top5Counts = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));

                for (Map.Entry<String, Integer> entry : top5Counts.entrySet()) {
                    dataset.addValue(entry.getValue(), "Count", entry.getKey());
                }           


    JFreeChart chart = ChartFactory.createBarChart(
            "Top 5 Cities",
            "City",
            "Count",
            dataset
    );

    JFrame frame = new JFrame("City Histogram");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(500, 300));
    frame.getContentPane().add(chartPanel);

    frame.pack();
    frame.setVisible(true);
}



public static void createB2BPieChart(Table table, String column) {
    DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

    BooleanColumn booleanColumn = table.booleanColumn(column);

    int countTrue = 0;
    int countFalse = 0;

    for (int i = 0; i < booleanColumn.size(); i++) {
        boolean value = booleanColumn.get(i);
        if (value) {
            countTrue++;
        } else {
            countFalse++;
        }
    }

    dataset.setValue("True", countTrue);
    dataset.setValue("False", countFalse);

    JFreeChart chart = ChartFactory.createPieChart(
            "B2B Chart",
            dataset,
            true,
            true,
            false
    );

    JFrame frame = new JFrame("B2B Pie Chart");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(500, 300));
    frame.getContentPane().add(chartPanel);

    frame.pack();
    frame.setVisible(true);
}

}
    