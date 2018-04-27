package com.dukedai;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.List;
import java.util.zip.ZipEntry;

public class MainRunner {

    public static void main(String[] args) throws Exception {
        //renderAdvance();

        //renderSimple();
        // testEscapeNoZip();
        testFormsWith3PEncrypt();
        // testFormsWithJDKZip();
    }

    private static void testFormsWithJDKZip() throws Exception {
        MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
        mxBean.gc();
        long usedBefore = mxBean.getHeapMemoryUsage().getUsed();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(100_000_000);
        // java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new FileOutputStream("formsjdk.zip"));
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(buffer);
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
        parameters.setPassword("secret");
        long start = System.currentTimeMillis();

        byte[] formBytes = readFormBytes();
        for (int i = 0; i < 2000; i++) {
            PDDocument pdfDocument = PDDocument.load(formBytes);
            PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();
//            for (PDField field : acroForm.getFields()) {
//                System.out.println(field.getFullyQualifiedName());
//            }

            renderTextField(acroForm, "name", "Esther Piao");
            renderTextField(acroForm, "fromYear", "2018");
            renderTextField(acroForm, "fromMonth", "6");
            renderTextField(acroForm, "fromDay", "3");
            renderTextField(acroForm, "toYear", "2018");
            renderTextField(acroForm, "toMonth", "7");
            renderTextField(acroForm, "toDay", "5");

            renderImageField(pdfDocument, acroForm, "signature", "signature.jpeg");
            acroForm.flatten();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // pdfDocument.save("contract.pdf");
            pdfDocument.save(baos);
            baos.flush();
            zos.putNextEntry(new ZipEntry("name" + i));
            zos.write(baos.toByteArray());
            zos.closeEntry();
            pdfDocument.close();
        }
        zos.finish();
        zos.close();
        mxBean.gc();
        long usedAfter = mxBean.getHeapMemoryUsage().getUsed();
        long end = System.currentTimeMillis();
        System.out.println("time ms: " + (end - start));
        System.out.println("memory bytes: " + (usedAfter - usedBefore));

    }


    private static void testFormsWith3PEncrypt() throws Exception {
        MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
        mxBean.gc();
        long usedBefore = mxBean.getHeapMemoryUsage().getUsed();
        // net.lingala.zip4j.io.ZipOutputStream zos = new net.lingala.zip4j.io.ZipOutputStream(new FileOutputStream("forms3p.zip"));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(100_000_000);
        net.lingala.zip4j.io.ZipOutputStream zos = new net.lingala.zip4j.io.ZipOutputStream(buffer);

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
        parameters.setSourceExternalStream(true);
        parameters.setPassword("secret");
        long start = System.currentTimeMillis();

        byte[] formBytes = readFormBytes();
        for (int i = 0; i < 1; i++) {
            PDDocument pdfDocument = PDDocument.load(formBytes);
            PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();
//            for (PDField field : acroForm.getFields()) {
//                System.out.println(field.getFullyQualifiedName());
//            }

            renderTextField(acroForm, "name", "Esther Piao");
            renderTextField(acroForm, "fromYear", "2018");
            renderTextField(acroForm, "fromMonth", "6");
            renderTextField(acroForm, "fromDay", "3");
            renderTextField(acroForm, "toYear", "2018");
            renderTextField(acroForm, "toMonth", "7");
            renderTextField(acroForm, "toDay", "5");

            renderImageField(pdfDocument, acroForm, "signature", "signature.jpeg");
            acroForm.flatten();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // pdfDocument.save("contract.pdf");
            pdfDocument.save(baos);
            baos.flush();
            String fileName = "name" + i;
            parameters.setFileNameInZip(fileName);
            zos.putNextEntry(new File("name" + i), parameters);
            zos.write(baos.toByteArray());
            zos.closeEntry();

            int pages = pdfDocument.getNumberOfPages();

            PDFRenderer renderer = new PDFRenderer(pdfDocument);
            for (int p = 0; p < pages; p++) {
                BufferedImage image = renderer.renderImage(p);
                File imageFile = new File("pdf_image_" + p + ".jpg");
                ImageIO.write(image, "jpg", imageFile);
            }
            pdfDocument.close();
        }
        zos.finish();
        zos.close();
        mxBean.gc();
        long usedAfter = mxBean.getHeapMemoryUsage().getUsed();
        long end = System.currentTimeMillis();
        System.out.println("time ms: " + (end - start));
        System.out.println("memory bytes: " + (usedAfter - usedBefore));

    }

    static byte[] readFormBytes() throws Exception {
        File f = new File("pdfescape_form.pdf");
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len = -1;
        while ((len = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    private static void renderAdvance() throws IOException {
        PDDocument pdfDocument = PDDocument.load(new File("employee.pdf"));

        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        renderTextField(acroForm, "date", "2018-04-10");
        renderTextField(acroForm, "name", "Trump");
        renderTextField(acroForm, "toYear", "2018-04-10");
        renderTextField(acroForm, "fromYear", "2011-04-10");
        renderTextField(acroForm, "EmployerName", "USA");
        renderImageField(pdfDocument, acroForm, "imageButton", "signature.jpeg");
        //acroForm.setNeedAppearances(false);
        acroForm.flatten();
        pdfDocument.save("employee_fill.pdf");
        pdfDocument.close();
    }


    private static void renderSimple() throws IOException {
        PDDocument pdfDocument = PDDocument.load(new File("form_template.pdf"));

        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        if (acroForm != null) {
            renderTextField(acroForm, "field1", "new field value 1");
            renderTextField(acroForm, "field2", "new field value 2");
            renderTextField(acroForm, "field3", "new field value 3");

            renderImageField(pdfDocument, acroForm, "field4", "signature.jpeg");
            acroForm.flatten();
            pdfDocument.save("form_fill.pdf");
            pdfDocument.close();
        }
    }

    private static void renderTextField(PDAcroForm acroForm, String name, String value) throws IOException {
        PDField field = (PDField) acroForm.getField(name);
        field.setValue(value);
    }

    private static void renderImageField(PDDocument pdfDocument, PDAcroForm acroForm, String name, String value) throws IOException {
        PDField buttonField = acroForm.getField(name);
        // System.out.println(buttonField.getClass().getSimpleName());
        // if (buttonField instanceof PDPushButton) {
        if (buttonField instanceof PDTextField) {
            // PDPushButton pdPushButton = (PDPushButton) buttonField;
            PDTextField pdPushButton = (PDTextField) buttonField;

            List<PDAnnotationWidget> widgets = pdPushButton.getWidgets();
            if (widgets != null && widgets.size() > 0) {
                PDAnnotationWidget annotationWidget = widgets.get(0); // just need one widget

                String filePath = value;
                File imageFile = new File(filePath);

                if (imageFile.exists()) {
                    /*
                     * BufferedImage bufferedImage = ImageIO.read(imageFile);
                     * PDImageXObject pdImageXObject = LosslessFactory.createFromImage(document, bufferedImage);
                     */
                    PDImageXObject pdImageXObject = PDImageXObject.createFromFile(filePath, pdfDocument);
                    float imageScaleRatio = (float) pdImageXObject.getHeight() / (float) pdImageXObject.getWidth();

                    PDRectangle buttonPosition = getFieldArea(pdPushButton);
                    float height = buttonPosition.getHeight();
                    float width = height / imageScaleRatio;
                    float x = buttonPosition.getLowerLeftX();
                    float y = buttonPosition.getLowerLeftY();

                    PDAppearanceStream pdAppearanceStream = new PDAppearanceStream(pdfDocument);
                    pdAppearanceStream.setResources(new PDResources());
                    try (PDPageContentStream pdPageContentStream = new PDPageContentStream(pdfDocument, pdAppearanceStream)) {
                        pdPageContentStream.drawImage(pdImageXObject, x, y, width, height);
                    }
                    pdAppearanceStream.setBBox(new PDRectangle(x, y, width, height));

                    PDAppearanceDictionary pdAppearanceDictionary = annotationWidget.getAppearance();
                    if (pdAppearanceDictionary == null) {
                        pdAppearanceDictionary = new PDAppearanceDictionary();
                        annotationWidget.setAppearance(pdAppearanceDictionary);
                    }

                    pdAppearanceDictionary.setNormalAppearance(pdAppearanceStream);
                    // System.out.println("Image '" + filePath + "' inserted");

                } else {
                    // System.err.println("File " + filePath + " not found");
                }
            }
        }
    }

    private static PDRectangle getFieldArea(PDField field) {
        COSDictionary fieldDict = field.getCOSObject();
        COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);
        return new PDRectangle(fieldAreaArray);
    }


}
