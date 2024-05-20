package com.example.Analysis;
import tech.tablesaw.api.Table;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.TDistribution;



import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class StatisticalAnalysis {

        public static void performAnalysis(Table table) {
            String[] columns = {"Avg. Session Length", "Time on App", "Time on Website", "Length of Membership", "Yearly Amount Spent"};
    
            Table statisticsTable = calculateStatistics(table, columns);
            System.out.println(statisticsTable.print());
            System.out.println("\n====================\n");
            Table correlationTable = calculateCorrelations(table, columns);
            System.out.println(correlationTable.print());
            createBoxPlot(table, columns);
            createScatterPlots(table,columns,"Avg. Session Length");
            createScatterPlots(table,columns,"Time on App");
            System.out.println("\n====================\n");

            performHypothesisTest(table.doubleColumn("Yearly Amount Spent"), 500.0, 0.05);
            System.out.println("\n====================\n");

            performHypothesisTest(table.doubleColumn("length of Membership"),60.0,.05);
            System.out.println("\n====================\n");

            performTwoSampleHypothesisTest(table.doubleColumn("Avg. Session Length"), table.doubleColumn("Time on App"), 0.05);
            System.out.println("\n====================\n");
            performTwoSampleHypothesisTest(table.doubleColumn("Time on App"),table.doubleColumn("Time on Website"),0.05);
            System.out.println("\n====================\n");
            performVarianceTest(table.doubleColumn("Yearly Amount Spent"), 20000.0, 0.05);





        }
    
        private static double getMode(DoubleColumn column) {
            double[] values = column.asDoubleArray();
            Map<Double, Integer> frequencyMap = new HashMap<>();
    
            for (double value : values) {
                frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
            }
    
            double mode = Double.NaN;
            int maxCount = 0;
    
            for (Map.Entry<Double, Integer> entry : frequencyMap.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mode = entry.getKey();
                }
            }
    
            return mode;
        }
    
        public static Table calculateStatistics(Table table, String[] columns) {
            StringColumn columnNames = StringColumn.create("Column Name");
        DoubleColumn means = DoubleColumn.create("Mean");
        DoubleColumn medians = DoubleColumn.create("Median");
        DoubleColumn modes = DoubleColumn.create("Mode");
        DoubleColumn stdDevs = DoubleColumn.create("Standard Deviation");
        DoubleColumn q1s = DoubleColumn.create("Q1");
        DoubleColumn q3s = DoubleColumn.create("Q3");
        DoubleColumn skewnesses = DoubleColumn.create("Skewness");
        DoubleColumn kurtoses = DoubleColumn.create("Kurtosis");

        for (String columnName : columns) {
            if (table.columnNames().contains(columnName)) {
                DoubleColumn column = table.doubleColumn(columnName);

                DescriptiveStatistics stats = new DescriptiveStatistics();
                for (double value : column) {
                    stats.addValue(value);
                }

                columnNames.append(columnName);
                means.append(stats.getMean());
                medians.append(stats.getPercentile(50));
                modes.append(getMode(column));
                stdDevs.append(stats.getStandardDeviation());
                q1s.append(stats.getPercentile(25));
                q3s.append(stats.getPercentile(75));
                Skewness skewness = new Skewness();
                Kurtosis kurtosis = new Kurtosis();
                skewnesses.append(skewness.evaluate(stats.getValues()));
                kurtoses.append(kurtosis.evaluate(stats.getValues()));
            }
        }

        Table statisticsTable = Table.create("Statistics Table", columnNames, means, medians, modes, stdDevs,q1s, q3s,kurtoses,skewnesses);
        return statisticsTable;
    }
    
        public static Table calculateCorrelations(Table table, String[] columns) {
            int n = columns.length;
            DoubleColumn[] correlationColumns = new DoubleColumn[n];
            for (int i = 0; i < n; i++) {
                correlationColumns[i] = DoubleColumn.create(columns[i], n);
            }
    
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == j) {
                        correlationColumns[i].set(j, 1.0);
                    } else {
                        DoubleColumn col1 = table.doubleColumn(columns[i]);
                        DoubleColumn col2 = table.doubleColumn(columns[j]);
                        double correlation = calculatePearsonCorrelation(col1, col2);
                        correlationColumns[i].set(j, correlation);
                    }
                }
            }
    
            Table correlationTable = Table.create("Correlation Matrix");
            for (int i = 0; i < n; i++) {
                correlationTable.addColumns(correlationColumns[i]);
            }
    
            return correlationTable;
        }
    
        private static double calculatePearsonCorrelation(DoubleColumn col1, DoubleColumn col2) {
            int n = col1.size();
            double sum1 = col1.sum();
            double sum2 = col2.sum();
            double sum1Sq = col1.multiply(col1).sum();
            double sum2Sq = col2.multiply(col2).sum();
            double pSum = col1.multiply(col2).sum();
    
            double num = pSum - (sum1 * sum2 / n);
            double den = Math.sqrt((sum1Sq - (sum1 * sum1 / n)) * (sum2Sq - (sum2 * sum2 / n)));
    
            return (den == 0) ? 0 : num / den;
        }

        public static void createBoxPlot(Table table, String[] columns) {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        for (String column : columns) {
            DoubleColumn doubleColumn = table.doubleColumn(column);
            dataset.add(doubleColumn.asList(), "Values", column);
        }

        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                "Box Plot",
                "Category",
                "Value",
                dataset,
                false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setOrientation(PlotOrientation.VERTICAL);

        ApplicationFrame frame = new ApplicationFrame("Box Plot Example");
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        frame.setContentPane(chartPanel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }
    public static void createScatterPlots(Table table, String[] columns, String targetColumn) {
        for (String column : columns) {
            if (!column.equals(targetColumn)) {
                DoubleColumn xColumn = table.doubleColumn(column);
                DoubleColumn yColumn = table.doubleColumn(targetColumn);

                XYSeries series = new XYSeries(column);
                for (int i = 0; i < xColumn.size(); i++) {
                    series.add(xColumn.get(i), yColumn.get(i));
                }

                XYSeriesCollection dataset = new XYSeriesCollection(series);
                JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                        "Scatter Plot: " + column + " vs " + targetColumn,
                        column,
                        targetColumn,
                        dataset,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false);

                ApplicationFrame frame = new ApplicationFrame("Scatter Plot: " + column + " vs " + targetColumn);
                ChartPanel chartPanel = new ChartPanel(scatterPlot);
                chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
                frame.setContentPane(chartPanel);
                frame.pack();
                RefineryUtilities.centerFrameOnScreen(frame);
                frame.setVisible(true);
            }
        }
    }
                //Hypothesis Testing
                //HO: mu = mu0;
                //H1: mu != mu0;

                public static void performHypothesisTest(DoubleColumn column, double hypothesizedMean, double alpha) {
                    // Calculate sample statistics
                    DescriptiveStatistics stats = new DescriptiveStatistics();
                    for (double value : column) {
                        stats.addValue(value);
                    }
                    double sampleMean = stats.getMean();
                    double sampleStdDev = stats.getStandardDeviation();
                    int sampleSize = (int) stats.getN();
            
                    // Calculate the t-statistic
                    double tStatistic = (sampleMean - hypothesizedMean) / (sampleStdDev / Math.sqrt(sampleSize));
            
                    // Determine the critical t-value
                    TDistribution tDistribution = new TDistribution(sampleSize - 1);
                    double criticalValue = tDistribution.inverseCumulativeProbability(1 - alpha / 2);
            
                    // Print results
                    System.out.println("Hypothesis Test for column: " + column.name());
                    System.out.println("Sample Mean: " + sampleMean);
                    System.out.println("Hypothesized Mean: " + hypothesizedMean);
                    System.out.println("Sample Standard Deviation: " + sampleStdDev);
                    System.out.println("Sample Size: " + sampleSize);
                    System.out.println("T-Statistic: " + tStatistic);
                    System.out.println("Critical Value: " + criticalValue);
            
                    // Make a decision
                    if (Math.abs(tStatistic) > criticalValue) {
                        System.out.println("Reject the null hypothesis. There is a significant difference.");
                    } else {
                        
                        System.out.println("Fail to reject the null hypothesis. There is no significant difference.");
                    }
                }

                public static void performTwoSampleHypothesisTest(DoubleColumn column1, DoubleColumn column2, double alpha) {
                    // Calculate sample statistics for both columns
                    DescriptiveStatistics stats1 = new DescriptiveStatistics();
                    DescriptiveStatistics stats2 = new DescriptiveStatistics();
                    for (double value : column1) {
                        stats1.addValue(value);
                    }
                    for (double value : column2) {
                        stats2.addValue(value);
                    }
            
                    double mean1 = stats1.getMean();
                    double mean2 = stats2.getMean();
                    double stdDev1 = stats1.getStandardDeviation();
                    double stdDev2 = stats2.getStandardDeviation();
                    int n1 = (int) stats1.getN();
                    int n2 = (int) stats2.getN();
            
                    // Calculate the pooled standard deviation
                    double pooledStdDev = Math.sqrt(((stdDev1 * stdDev1) / n1) + ((stdDev2 * stdDev2) / n2));
            
                    // Calculate the t-statistic
                    double tStatistic = (mean1 - mean2) / pooledStdDev;
            
                    // Degrees of freedom
                    int degreesOfFreedom = n1 + n2 - 2;
            
                    // Determine the critical t-value
                    TDistribution tDistribution = new TDistribution(degreesOfFreedom);
                    double criticalValue = tDistribution.inverseCumulativeProbability(1 - alpha / 2);
            
                    // Print results
                    System.out.println("Two-Sample Hypothesis Test:");
                    System.out.println("Mean of Column 1: " + mean1);
                    System.out.println("Mean of Column 2: " + mean2);
                    System.out.println("Standard Deviation of Column 1: " + stdDev1);
                    System.out.println("Standard Deviation of Column 2: " + stdDev2);
                    System.out.println("Sample Size of Column 1: " + n1);
                    System.out.println("Sample Size of Column 2: " + n2);
                    System.out.println("T-Statistic: " + tStatistic);
                    System.out.println("Critical Value: " + criticalValue);
            
                    // Make a decision
                    if (Math.abs(tStatistic) > criticalValue) {
                        System.out.println("Reject the null hypothesis. The means are significantly different.");
                    } else {
                        System.out.println("Fail to reject the null hypothesis. The means are not significantly different.");
                    }
                }

        public static void performVarianceTest(DoubleColumn column, double hypothesizedVariance, double alpha) {
        // Calculate sample statistics
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double value : column) {
            stats.addValue(value);
        }
        double sampleVariance = stats.getVariance();
        int sampleSize = (int) stats.getN();

        // Calculate the chi-square statistic
        double chiSquareStatistic = (sampleSize - 1) * sampleVariance / hypothesizedVariance;

        // Determine the critical chi-square values
        ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(sampleSize - 1);
        double criticalValueLower = chiSquaredDistribution.inverseCumulativeProbability(alpha / 2);
        double criticalValueUpper = chiSquaredDistribution.inverseCumulativeProbability(1 - alpha / 2);

        // Print results
        System.out.println("Variance Test for column: " + column.name());
        System.out.println("Sample Variance: " + sampleVariance);
        System.out.println("Sample Size: " + sampleSize);
        System.out.println("Chi-Square Statistic: " + chiSquareStatistic);
        System.out.println("Critical Value Lower: " + criticalValueLower);
        System.out.println("Critical Value Upper: " + criticalValueUpper);

        // Make a decision
        if (chiSquareStatistic < criticalValueLower || chiSquareStatistic > criticalValueUpper) {
            System.out.println("Reject the null hypothesis. The variance is significantly different from the hypothesized variance.");
        } else {
            System.out.println("Fail to reject the null hypothesis. The variance is not significantly different from the hypothesized variance.");
        }
    }
            }
                
            
        




        
    
    


    