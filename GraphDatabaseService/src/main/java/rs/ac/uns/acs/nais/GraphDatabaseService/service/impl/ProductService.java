package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import org.springframework.stereotype.Service;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Product;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.ProductRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.CustomerRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.IProductService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.awt.*;


@Service
public class ProductService implements IProductService {

    public final ProductRepository productRepository;
    public final CustomerRepository customerRepository;


    public ProductService(ProductRepository productRepository, CustomerRepository customerRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }
    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }
    
    @Override
    public Product addNewProduct(Product product) {
        
        product.setAvailable(true);
        Product product1 = productRepository.save(product);
        
        return product1;
        
    }

    @Override
    public boolean deleteProduct(String id) {
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()){
            product.get().setAvailable(false);
            productRepository.save(product.get());
            return true;
        }
        return false;
    }
    @Override
    public boolean updateProduct(String id, String productName) {
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()){
            product.get().setName(productName);
            productRepository.save(product.get());
            return true;
        }
        return false;
    }

    @Override
    public List<Product> recommendProductsByPurchaseHistory(Long customerId) {
        return productRepository.recommendProductsByPurchaseHistory(customerId);
    }

    @Override
    public List<Product> recommendProductsByReviews(Long customerId) {
        return productRepository.recommendProductsByReviews(customerId);
    }

    @Override
    public byte[] export(List<Product> products) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        com.lowagie.text.Document document = new com.lowagie.text.Document();

        String filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss")) + ".pdf";

        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Font.BOLD);

        Paragraph title = new Paragraph("PRODUCT REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        PdfPTable reportTable = new PdfPTable(3);
        reportTable.setWidthPercentage(100);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);
        PdfPCell headerCell1 = new PdfPCell(new Paragraph("Product", headerFont));
        PdfPCell headerCell2 = new PdfPCell(new Paragraph("Availability", headerFont));
        PdfPCell headerCell3 = new PdfPCell(new Paragraph("ID", headerFont));

        headerCell1.setBackgroundColor(new Color(110, 231, 234, 255));
        headerCell2.setBackgroundColor(new Color(110, 231, 234, 255));
        headerCell3.setBackgroundColor(new Color(110, 231, 234, 255));

        reportTable.addCell(headerCell1);
        reportTable.addCell(headerCell2);
        reportTable.addCell(headerCell3);

        for (Product product : products) {
            reportTable.addCell(product.getName());
            reportTable.addCell(String.valueOf(product.isAvailable()));
            reportTable.addCell(product.getId());
        }

        document.add(reportTable);
        document.close();

        return byteArrayOutputStream.toByteArray();
    }


}
