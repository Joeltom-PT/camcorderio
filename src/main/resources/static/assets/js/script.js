function exportYearly() {
    const table = document.getElementById('yearly');
    const pdfContent = {
        content: [
            {
                text: 'Yearly Sales Report',
                style: 'header'
            },
            {
                table: {
                    body: []
                }
            }
        ],
        styles: {
            header: {
                fontSize: 18,
                bold: true,
                margin: [0, 0, 0, 10] // Adjust margin as needed
            },
            table: {
                margin: [0, 10, 0, 0] // Adjust margin as needed
            }
        }
    };

    // Convert HTML table rows to pdfmake table format
    for (let i = 0; i < table.rows.length; i++) {
        const row = table.rows[i].cells;
        const rowData = [];
        for (let j = 0; j < row.length; j++) {
            rowData.push(row[j].innerText);
        }
        pdfContent.content[1].table.body.push(rowData);
    }

    // Generate PDF
    pdfMake.createPdf(pdfContent).download('YearlySalesReport.pdf');
}

function exportDaily() {
    const table = document.getElementById('daily');
    const pdfContent = {
        content: [
            {
                text: 'Daily Sales Report',
                style: 'header'
            },
            {
                table: {
                    body: []
                }
            }
        ],
        styles: {
            header: {
                fontSize: 18,
                bold: true,
                margin: [0, 0, 0, 10] // Adjust margin as needed
            },
            table: {
                margin: [0, 10, 0, 0] // Adjust margin as needed
            }
        }
    };

    // Convert HTML table rows to pdfmake table format
    for (let i = 0; i < table.rows.length; i++) {
        const row = table.rows[i].cells;
        const rowData = [];
        for (let j = 0; j < row.length; j++) {
            rowData.push(row[j].innerText);
        }
        pdfContent.content[1].table.body.push(rowData);
    }

    // Generate PDF
    pdfMake.createPdf(pdfContent).download('DailySalesReport.pdf');
}

function exportMonthly() {
    const table = document.getElementById('monthly');
    const pdfContent = {
        content: [
            {
                text: 'Monthly Sales Report',
                style: 'header'
            },
            {
                table: {
                    body: []
                }
            }
        ],
        styles: {
            header: {
                fontSize: 18,
                bold: true,
                margin: [0, 0, 0, 10] // Adjust margin as needed
            },
            table: {
                margin: [0, 10, 0, 0] // Adjust margin as needed
            }
        }
    };

    // Convert HTML table rows to pdfmake table format
    for (let i = 0; i < table.rows.length; i++) {
        const row = table.rows[i].cells;
        const rowData = [];
        for (let j = 0; j < row.length; j++) {
            rowData.push(row[j].innerText);
        }
        pdfContent.content[1].table.body.push(rowData);
    }

    // Generate PDF
    pdfMake.createPdf(pdfContent).download('MonthlySalesReport.pdf');
}

function exportWeekly() {
    const table = document.getElementById('weekly');
    const pdfContent = {
        content: [
            {
                text: 'Weekly Sales Report',
                style: 'header'
            },
            {
                table: {
                    body: []
                }
            }
        ],
        styles: {
            header: {
                fontSize: 18,
                bold: true,
                margin: [0, 0, 0, 10] // Adjust margin as needed
            },
            table: {
                margin: [0, 10, 0, 0] // Adjust margin as needed
            }
        }
    };

    // Convert HTML table rows to pdfmake table format
    for (let i = 0; i < table.rows.length; i++) {
        const row = table.rows[i].cells;
        const rowData = [];
        for (let j = 0; j < row.length; j++) {
            rowData.push(row[j].innerText);
        }
        pdfContent.content[1].table.body.push(rowData);
    }

    // Generate PDF
    pdfMake.createPdf(pdfContent).download('WeeklySalesReport.pdf');
}
