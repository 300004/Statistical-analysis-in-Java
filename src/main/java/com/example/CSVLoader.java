package com.example;


import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.NumberColumn;
import tech.tablesaw.api.StringColumn;
import java.util.HashMap;
import java.util.Set;




import tech.tablesaw.api.Table;

import tech.tablesaw.columns.Column;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.aggregate.AggregateFunction;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.example.visualization.DataVisualizer;
import com.example.Analysis.StatisticalAnalysis;
import java.util.Scanner;

public class CSVLoader {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);


        // Path to your CSV file in the resources folder
        String csvFile1 = "/Amazon.csv";
        String csvFile2 ="/Ecommerce Customers.csv";
        
        
        System.out.println("1.Visualization for Amazon Dataset");
        System.out.println("2.Statistical Analysis for an Ecommerce Dataset");
        System.out.println("Enter a choice");

        String choice = scan.nextLine();
        switch(choice)
        {
            case "1":
            processCSVFile(csvFile1);
            break;
            case "2":
            processCSVFile2(csvFile2);
            break;
            default:
            System.out.println("Invalid Choice.");


        }
        scan.close();



    }

        private static void processCSVFile(String csvFile){
            try (InputStream inputStream = CSVLoader.class.getResourceAsStream(csvFile);
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(reader)) {

                Table table = Table.read().csv(reader);
                System.out.println("Column names:");
                System.out.println(table.columnNames());
                StringColumn courierStatusColumn = table.stringColumn("Courier Status");

                // Replace blank values with "Cancelled" in the "Courier Status" column
                courierStatusColumn.set(courierStatusColumn.isMissing(), "Cancelled");


                table.removeColumns("currency","ship-country","fulfilled-by","C23");
                DoubleColumn amountColumn = (DoubleColumn) table.column("Amount");

                // Replace missing values in the "Amount" column with 0
                amountColumn.set(amountColumn.isMissing(), 0.0);
                StringColumn promocode = table.stringColumn("promotion-ids");
                promocode.set(promocode.isMissing(), "Nil");
                table = table.dropRowsWithMissingValues();
            
                System.out.println("Missing values in each column:");
                 for (Column<?> column : table.columns()) {
                System.out.println(column.name() + ": " + column.countMissing());
            }
                
                System.out.println("Unique values in Fulfilment column:");
                System.out.println(table.stringColumn("Fulfilment").unique().print());
                System.out.println(table.stringColumn("Courier Status").unique().print());
                System.out.println(table.stringColumn("ship-service-level").unique().print());

                
                
                
                String[] categories = {"Merchant", "Amazon"};
                DataVisualizer.createPieChart(table, "Fulfilment",categories);
                DataVisualizer.createHistogram(table,"Status");
                DataVisualizer.createShipPieChart(table,"ship-service-level");
                DataVisualizer.createCityHistogram(table,"ship-city");
                DataVisualizer.createB2BPieChart(table, "B2B");

 }
            catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processCSVFile2(String csvFile){
        try (InputStream inputStream = CSVLoader.class.getResourceAsStream(csvFile);
         InputStreamReader reader = new InputStreamReader(inputStream);
         BufferedReader br = new BufferedReader(reader)) {

            Table table = Table.read().csv(reader);
            System.out.println("Column names:");
            System.out.println(table.columnNames());
            StatisticalAnalysis.performAnalysis(table);

            


}
        catch (IOException e) {
             e.printStackTrace();
}
    }
}
