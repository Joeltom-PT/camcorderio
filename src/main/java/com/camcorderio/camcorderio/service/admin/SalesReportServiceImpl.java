package com.camcorderio.camcorderio.service.admin;

import com.camcorderio.camcorderio.entity.user.Orders;
import com.camcorderio.camcorderio.exceptions.FutureDateException;
import com.camcorderio.camcorderio.exceptions.NoOrdersFoundException;
import com.camcorderio.camcorderio.model.admin.*;
import com.camcorderio.camcorderio.repository.OrderItemRepository;
import com.camcorderio.camcorderio.repository.OrderRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SalesReportServiceImpl implements SalesReport {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public SalesReportServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }



    @Override
    public List<Integer> getMonthlyValues() {
        List<Integer> monthlyValues = new ArrayList<>();


        LocalDate currentDate = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate startDate = currentDate.minusMonths(i).withDayOfMonth(1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);


            Integer totalQuantity = orderItemRepository.getTotalQuantityByMonth(startDate, endDate);


            monthlyValues.add(totalQuantity != null ? totalQuantity : 0);
        }

        return monthlyValues;
    }



    @Override
    public List<Integer> getYearlyValues() {
        List<Integer> yearlyValues = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();

        for (int i = 2; i >= 0; i--) {
            int targetYear = currentYear - i;

            Integer totalQuantity = orderItemRepository.getTotalQuantityByYear(targetYear);

            yearlyValues.add(totalQuantity != null ? totalQuantity : 0);
        }

        return yearlyValues;
    }

    @Override
    public List<Integer> getWeeklyValues() {
        List<Integer> weeklyValues = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();

        for (int i = 3; i >= 0; i--) {
            LocalDate startDate = currentDate.minusWeeks(i).with(DayOfWeek.MONDAY);
            LocalDate endDate = startDate.plusWeeks(1).minusDays(1);

            Integer totalQuantity = orderItemRepository.getTotalQuantityByDateRange(startDate, endDate);

            weeklyValues.add(totalQuantity != null ? totalQuantity : 0);
        }

        return weeklyValues;
    }


    @Override
    public List<Integer> getDailyValues() {
        List<Integer> dailyValues = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < 8; i++) {
            LocalDate startDate = currentDate.minusDays(i);
            LocalDate endDate = startDate;

            Integer totalQuantity = orderItemRepository.getTotalQuantityByDateRange(startDate, endDate);

            dailyValues.add(totalQuantity != null ? totalQuantity : 0);
        }

        return dailyValues;
    }

    @Override
    public List<DailySales> getDailySales() {
        List<DailySales> dailySales = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();

        for (int i = -3; i <= 3; i++) {
            LocalDate date = currentDate.plusDays(i);

            Integer totalAmount = orderItemRepository.getTotalAmountByDate(date);
            Integer noOfProductSold = orderItemRepository.getTotalQuantityByDate(date);
            Integer noOfOrders = orderRepository.getTotalOrdersByDate(date);

            DailySales dailySalesData = new DailySales();
            dailySalesData.setDay(date.toString());
            dailySalesData.setTotalAmount(totalAmount != null ? totalAmount : 0);
            dailySalesData.setNoOfProductSold(noOfProductSold != null ? noOfProductSold : 0);
            dailySalesData.setNoOfOrders(noOfOrders != null ? noOfOrders : 0);

            dailySales.add(dailySalesData);
        }

        return dailySales;
    }

    @Override
    public List<WeeklySales> getWeeklySales() {
        List<WeeklySales> weeklySalesList = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < 4; i++) {
            LocalDate startDate = currentDate.minusWeeks(i).with(DayOfWeek.MONDAY);
            LocalDate endDate = startDate.plusWeeks(1).minusDays(1);


            Integer totalAmount = orderItemRepository.getTotalAmountByDateRange(startDate, endDate);
            Integer noOfProductSold = orderItemRepository.getTotalQuantityByDateRange(startDate, endDate);
            Integer noOfOrders = orderRepository.getTotalOrdersByDateRange(startDate, endDate);


            WeeklySales weeklySales = new WeeklySales();


            String weekRepresentation;
            if (i == 0) {
                weekRepresentation = "Current Week";
            } else {
                weekRepresentation = "Week -" + i;
            }

            weeklySales.setWeeks(weekRepresentation);
            weeklySales.setTotalAmount(totalAmount != null ? totalAmount : 0);
            weeklySales.setNoOfProductSold(noOfProductSold != null ? noOfProductSold : 0);
            weeklySales.setNoOfOrders(noOfOrders != null ? noOfOrders : 0);


            weeklySalesList.add(weeklySales);
        }

        return weeklySalesList;
    }


    @Override
    public List<MonthlySales> getMonthlySales() {
        List<MonthlySales> monthlySalesList = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate startDate = currentDate.minusMonths(i).withDayOfMonth(1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);

            Integer totalAmount = orderItemRepository.getTotalAmountByDateRange(startDate, endDate);
            Integer noOfProductSold = orderItemRepository.getTotalQuantityByDateRange(startDate, endDate);
            Integer noOfOrders = orderRepository.getTotalOrdersByDateRange(startDate, endDate);

            MonthlySales monthlySales = new MonthlySales();

            String monthRepresentation = startDate.getMonth().name() + " " + startDate.getYear();

            monthlySales.setMonth(monthRepresentation);
            monthlySales.setTotalAmount(totalAmount != null ? totalAmount : 0);
            monthlySales.setNoOfProductSold(noOfProductSold != null ? noOfProductSold : 0);
            monthlySales.setNoOfOrders(noOfOrders != null ? noOfOrders : 0);

            monthlySalesList.add(monthlySales);
        }

        return monthlySalesList;
    }

    @Override
    public List<YearlySales> getYearlySales() {
        List<YearlySales> yearlySalesList = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < 3; i++) {
            int targetYear = currentDate.getYear() - i;
            LocalDate startDate = LocalDate.of(targetYear, Month.JANUARY, 1);
            LocalDate endDate = startDate.plusYears(1).minusDays(1);

            Integer totalAmount = orderItemRepository.getTotalAmountByDateRange(startDate, endDate);
            Integer noOfProductSold = orderItemRepository.getTotalQuantityByDateRange(startDate, endDate);
            Integer noOfOrders = orderRepository.getTotalOrdersByDateRange(startDate, endDate);

            YearlySales yearlySales = new YearlySales();

            yearlySales.setYear(String.valueOf(targetYear));
            yearlySales.setTotalAmount(totalAmount != null ? totalAmount : 0);
            yearlySales.setNoOfProductSold(noOfProductSold != null ? noOfProductSold : 0);
            yearlySales.setNoOfOrders(noOfOrders != null ? noOfOrders : 0);

            yearlySalesList.add(yearlySales);
        }

        return yearlySalesList;
    }


    @Override
    public byte[] generateSalesReportPDF(String startDate, String endDate) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate parsedStartDate = LocalDate.parse(startDate, dateFormatter);
            LocalDate parsedEndDate = LocalDate.parse(endDate, dateFormatter);
            LocalDate today = LocalDate.now();
            List<Orders> orders = orderRepository.findByOrderDateBetween(parsedStartDate, parsedEndDate);

            if (parsedStartDate.isAfter(today) || parsedEndDate.isAfter(today)) {

                throw new FutureDateException();
            }

            if (orders == null || orders.isEmpty()) {
                throw new NoOrdersFoundException();
            }


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            PdfPCell cell;

            // Title row
            cell = new PdfPCell(new Phrase("Sales Report " + startDate + " to " + endDate, headerFont));
            cell.setColspan(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            // Header row
            cell = new PdfPCell(new Phrase("No", headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Order ID", headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Order Date", headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Payment Method", headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Total Amount", headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            // Data rows
            int counter = 1;
            for (Orders order : orders) {
                // Counter cell
                cell = new PdfPCell(new Phrase(String.valueOf(counter)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                // Order ID cell
                cell = new PdfPCell(new Phrase(String.valueOf(order.getOrderId())));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                // Order Date cell
                cell = new PdfPCell(new Phrase(order.getOrderDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                // Payment Method cell
                cell = new PdfPCell(new Phrase(order.getPayments().getPaymentMethod().name()));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                // Total Amount cell
                cell = new PdfPCell(new Phrase(String.valueOf(order.getTotalAmount())));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                counter++;
            }

            document.add(table);
            document.close();
            return byteArrayOutputStream.toByteArray();
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }


}