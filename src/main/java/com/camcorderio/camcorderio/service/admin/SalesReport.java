package com.camcorderio.camcorderio.service.admin;

import com.camcorderio.camcorderio.model.admin.*;

import java.time.LocalDate;
import java.util.List;

public interface SalesReport {



    List<Integer> getMonthlyValues();

    List<Integer> getYearlyValues();

    List<Integer> getWeeklyValues();

    List<Integer> getDailyValues();

    List<DailySales> getDailySales();

    List<WeeklySales> getWeeklySales();

    List<MonthlySales> getMonthlySales();

    List<YearlySales> getYearlySales();

    byte[] generateSalesReportPDF(String start, String end);
}
