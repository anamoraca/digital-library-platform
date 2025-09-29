package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;


import rs.ac.uns.acs.nais.GraphDatabaseService.repository.ReportRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Book;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final ReportRepository reportRepo;
    private final TemplateEngine templateEngine;

    public ReportService(ReportRepository reportRepo, TemplateEngine templateEngine) {
        this.reportRepo = reportRepo;
        this.templateEngine = templateEngine;
    }

    public byte[] generateOverviewPdf(String userId, String genre, Integer yearFrom, Integer yearTo,
                                      Long minReads, Long limit) {

        // Proste sekcije
        List<Map<String,Object>> booksByGenre =
                reportRepo.listBooksByGenre(genre, yearFrom, yearTo, limit != null ? limit : 50L);
        List<Map<String,Object>> activeUsers =
                reportRepo.usersByReadCount(minReads != null ? minReads : 1L, limit != null ? limit : 50L);

        // Slozena sekcija
        List<Map<String,Object>> complex =
                reportRepo.complexHybrid(userId, limit != null ? limit : 20L);

        // Thymeleaf model
        Context ctx = new Context();
        ctx.setVariable("userId", userId);
        ctx.setVariable("genre", genre);
        ctx.setVariable("yearFrom", yearFrom);
        ctx.setVariable("yearTo", yearTo);
        ctx.setVariable("minReads", minReads);
        ctx.setVariable("booksByGenre", booksByGenre);
        ctx.setVariable("activeUsers", activeUsers);
        ctx.setVariable("complex", complex);

        String html = templateEngine.process("report", ctx);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(bos);
            builder.run();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
