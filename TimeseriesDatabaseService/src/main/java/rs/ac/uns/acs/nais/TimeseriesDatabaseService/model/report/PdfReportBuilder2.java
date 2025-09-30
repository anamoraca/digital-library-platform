package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;



public class PdfReportBuilder2 {

    public static byte[] buildReport(ReportPayload2 p) {
        try (var baos = new ByteArrayOutputStream()) {
            var doc = new Document(PageSize.A4, 36, 36, 48, 48);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            var titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            var h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            var mono = FontFactory.getFont(FontFactory.COURIER, 9);

            // Header
            doc.add(new Paragraph("E-Library Analytics Report", titleFont));
            doc.add(new Paragraph("Range: " + p.range() + " | Interval: " + p.interval()));
            doc.add(new Paragraph("Generated: " + fmtInstant(p.generatedAt())));
            doc.add(Chunk.NEWLINE);

            // Simple #1: Top by open_count
            doc.add(new Paragraph("Simple Section #1 – Top Books (open_count)", h2));
            doc.add(new Paragraph("Limit: " + p.limit(), mono));
            doc.add(topTable(p.topOpen()));
            doc.add(Chunk.NEWLINE);

            // Simple #2: Top by read_time
            doc.add(new Paragraph("Simple Section #2 – Top Books (read_time)", h2));
            doc.add(new Paragraph("Limit: " + p.limit(), mono));
            doc.add(topTable(p.topRead()));
            doc.add(Chunk.NEWLINE);

            // Complex: trends + analytics for a selected book
            doc.add(new Paragraph("Complex Section – Load & Reading Tempo", h2));
            doc.add(new Paragraph("Book: " + p.complexBookId(), mono));
            doc.add(new Paragraph(String.format(
                    "AVG load_ms: %.1f | P95 load_ms: %.1f | AVG progress rate (pages/%s): %.2f",
                    p.avgLoad(), p.p95Load(), p.interval(), p.avgProgressRate()
            )));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Load Time Trend (sample)", mono));
            doc.add(tsTable(p.loadTrend()));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Progress Rate Trend (sample)", mono));
            doc.add(tsTable(p.progressTrend()));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private static PdfPTable topTable(List<rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.TopBookMetric> rows) {
        var t = new PdfPTable(new float[]{2f, 1f});
        t.setWidthPercentage(100);
        addHeader(t, "book_id", "value");
        if (rows != null) {
            for (var r : rows) addRow(t, nz(r.bookId()), String.valueOf(r.value()));
        }
        return t;
    }

    private static PdfPTable tsTable(List<TimeSeriesPoint<Double>> ts) {
        var t = new PdfPTable(new float[]{2.5f, 1.2f});
        t.setWidthPercentage(100);
        addHeader(t, "time", "value");
        if (ts != null) {
            int max = Math.min(ts.size(), 25);
            for (int i=0; i<max; i++) {
                var p = ts.get(i);
                addRow(t, fmtInstant(p.time()), p.value()==null ? "-" : String.format("%.3f", p.value()));
            }
        }
        return t;
    }

    private static void addHeader(PdfPTable t, String... cells) {
        for (var c : cells) {
            var cell = new PdfPCell(new Phrase(c, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
            cell.setGrayFill(0.92f);
            t.addCell(cell);
        }
    }
    private static void addRow(PdfPTable t, String... cells) {
        for (var c : cells)
            t.addCell(new PdfPCell(new Phrase(c, FontFactory.getFont(FontFactory.HELVETICA, 9))));
    }

    private static String fmtInstant(java.time.Instant i) {
        if (i == null) return "-";
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
                .withZone(ZoneId.of("UTC"));
        return fmt.format(i);
    }
    private static String nz(String s) { return s == null ? "-" : s; }
}
