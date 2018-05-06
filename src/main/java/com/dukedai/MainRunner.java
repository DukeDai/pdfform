package com.dukedai;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
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
        // testFormsWith3PEncrypt();
        // testFormsWithJDKZip();
        // parsePdf();
        //drawPDF();
        renderSingleFile();
        //renderCKJFontToImage();
    }

    private static void renderCKJFontToImage() throws Exception{
        byte[] formBytes = readFormBytes();
        PDDocument pdfDocument = PDDocument.load(formBytes);
        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();


        for (PDField field : acroForm.getFields()) {
            //System.out.println(field.getFullyQualifiedName());
        }
        acroForm.setNeedAppearances(false);

        byte[] imageBytes = convertTextToImage("인천 4센터");
        renderImageField(pdfDocument, acroForm, "center", imageBytes);
        //renderTextFieldWithFont(acroForm, "center", "인천 4센터");
        renderAsciiTextField(acroForm, "shiftStart", "09:00");
        renderAsciiTextField(acroForm, "shiftEnd", "18:00");
        renderAsciiTextField(acroForm, "breakStart", "12:00");
        renderAsciiTextField(acroForm, "breakEnd", "13:00");
        renderAsciiTextField(acroForm, "hourSalary", "79,065");
        renderAsciiTextField(acroForm, "daySalary", "79,065");
        renderAsciiTextField(acroForm, "startYear", "2018");
        renderAsciiTextField(acroForm, "startMonth", "05");
        renderAsciiTextField(acroForm, "startDay", "05");
        renderAsciiTextField(acroForm, "endYear", "2018");
        renderAsciiTextField(acroForm, "endMonth", "05");
        renderAsciiTextField(acroForm, "endDay", "06");
        renderAsciiTextField(acroForm, "birthday", "2010-05-06");
        renderImageField(pdfDocument, acroForm, "signature", "signature.jpeg");
        acroForm.flatten();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        pdfDocument.save("contract.pdf");

        pdfDocument.save(baos);
        baos.flush();
        pdfDocument.close();
    }

    private static void renderSingleFile() throws Exception {
        byte[] formBytes = readFormBytes();
        PDDocument pdfDocument = PDDocument.load(formBytes);
        FileInputStream is = new FileInputStream(new File("UnBatang.ttf"));
        // To support CJK, embedSubset(whether font is embedded or not) must set as false(will be embedded into PDF)
        // Side effect: PDF will be bigger because of embedded font.
        PDType0Font font = PDType0Font.load(pdfDocument, is, false);

        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();

        String fontName = acroForm.getDefaultResources().add(font).getName();
        System.out.println(fontName);
        acroForm.refreshAppearances();

        for (PDField field : acroForm.getFields()) {
            //System.out.println(field.getFullyQualifiedName());
        }
        acroForm.setNeedAppearances(false);

        //byte[] imageBytes = convertTextToImage("");
        //renderImageField(pdfDocument, acroForm, "center", imageBytes);
        renderTextFieldWithFont(acroForm, "center", "인천 4센터");
        renderAsciiTextField(acroForm, "shiftStart", "09:00");
        renderAsciiTextField(acroForm, "shiftEnd", "18:00");
        renderAsciiTextField(acroForm, "breakStart", "12:00");
        renderAsciiTextField(acroForm, "breakEnd", "13:00");
        renderAsciiTextField(acroForm, "hourSalary", "79,065");
        renderAsciiTextField(acroForm, "daySalary", "79,065");
        renderAsciiTextField(acroForm, "startYear", "2018");
        renderAsciiTextField(acroForm, "startMonth", "05");
        renderAsciiTextField(acroForm, "startDay", "05");
        renderAsciiTextField(acroForm, "endYear", "2018");
        renderAsciiTextField(acroForm, "endMonth", "05");
        renderAsciiTextField(acroForm, "endDay", "06");
        renderAsciiTextField(acroForm, "birthday", "2010-05-06");
        renderImageField(pdfDocument, acroForm, "signature", "signature.jpeg");
        acroForm.flatten();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        pdfDocument.save("contract.pdf");

        pdfDocument.save(baos);
        baos.flush();
        pdfDocument.close();

    }

    private static void parsePdf() throws Exception {
        PDDocument doc = PDDocument.load(new File("contract.pdf"));
        PDPageTree ptree = doc.getDocumentCatalog().getPages();
        for (PDPage page : ptree) {
            for (COSName name : page.getResources().getFontNames()) {
                System.out.println(name.getName());
            }
            PDFStreamParser parser = new PDFStreamParser(page);
            parser.parse();
            List<Object> tokens = parser.getTokens();
            for (int j = 0; j < tokens.size(); j++) {
                Object next = tokens.get(j);

                //System.out.println(next.getClass());
                if (next instanceof COSString) {
                    COSString cosStr = (COSString) next;
                    //System.out.println("String " + cosStr.getString());
                }
                if (next instanceof COSName) {
                    COSName cosStr = (COSName) next;
                    //System.out.println("String " + cosStr.getName());
                }
                if (next instanceof org.apache.pdfbox.contentstream.operator.Operator) {
                    Operator op = (Operator) next;
                    //System.out.println(op.getName() + " " + op.toString());
                    if (op.getName().equals("Tj")) {

                        COSString pre = (COSString) tokens.get(j - 1);
                        //System.out.println(pre.getString());
                    }
                    if (op.getName().equals("TJ")) {
                        COSArray previous = (COSArray) tokens.get(j - 1);
                        for (int k = 0; k < previous.size(); k++) {
                            Object el = previous.get(k);
                            if (el instanceof COSString) {
                                COSString s = (COSString) el;
                                //System.out.println(s.getString());
                            }
                        }
                    }


                }
            }

            break;
        }
    }

    private static void drawPDF() throws Exception {

//        org.apache.fontbox.util.autodetect.FontFileFinder fontFinder = new org.apache.fontbox.util.autodetect.FontFileFinder();
//        List<URI> fontURIs = fontFinder.find();
//        File fontFile = null;
//        for(URI uri : fontURIs){
//            fontFile = new File(uri);
//            System.out.println(fontFile.getName());
//            if("Gungseouche.ttf".equals(fontFile.getName())){
//                break;
//            }
//        }
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDAcroForm acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);


        FileInputStream is = new FileInputStream(new File("UnBatang.ttf"));
        //FileInputStream is = new FileInputStream(new File("AppleMyungjo.ttf"));
        Encoding encoding = Encoding.getInstance(COSName.TYPE0);
        //this.getClass().getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf")
        PDType0Font font = PDType0Font.load(document, is, false);
        //PDFont font = PDTrueTypeFont.loadTTF(document, is, encoding);
        PDResources resources = new PDResources();
        String fontName = resources.add(font).getName();
        acroForm.setDefaultResources(resources);

        String defaultAppearanceString = "/" + fontName + " 10 Tf 10 g";


        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.beginText();
        contentStream.newLineAtOffset(25, 300);
        contentStream.setFont(font, 20);
        contentStream.showText("This is title.");

        contentStream.setFont(font, 20);
        contentStream.newLineAtOffset(40, 200);
        //contentStream.newLine();
        contentStream.showText("인천 4센터");
//        contentStream.setFont(font, 20);
//        contentStream.showText("직원은        년    월    일(“근로관계 개시일”)부터        년    월    일까지 회사의 지시에 따라 다음과 같이 근무하며, 별도의 정함이 없는 한 본 계약기간 만료로 근로계약은 자동 종료된다. 또한, 직원의 업무 및 근무 장소는 회사의 업무상 필요에 따라 추후 변경될 수 있다.");
//        contentStream.newLine();
//        contentStream.showText("업       무 : [현장기능직/기간제] 입출고 및 기타 관리자의 지시 업무");
//        contentStream.newLine();
//        contentStream.showText("근무 장소 : [대구] 물류센터");
//        contentStream.newLine();
//        contentStream.setFont(font, 20);
//        contentStream.showText("제2조 수습 기간");
//        contentStream.newLine();
//        contentStream.setFont(font, 20);
        //contentStream.showText("직원의 수습기간은 근로관계 개시일로부터 1개월로 한다. 단, 회사는 필요하거나 적절하다고 생각하는 경우 위 수습기간을 생략, 단축 및 연장할 수 있다. 수습기간 중 또는 수습기간 만료 시에 계속 근로가 부적당하다고 인정하는 경우 사전예고 및 기타 보상 없이 본 계약을 해지할 수 있으며, 그 경우 회사는 직원의 실제 근무 일수에 대해서만 급여를 지급한다. ");
        contentStream.endText();
        contentStream.close();


        document.save("draw.pdf");
        document.close();


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

            renderAsciiTextField(acroForm, "name", "Esther Piao");
            renderAsciiTextField(acroForm, "fromYear", "2018");
            renderAsciiTextField(acroForm, "fromMonth", "6");
            renderAsciiTextField(acroForm, "fromDay", "3");
            renderAsciiTextField(acroForm, "toYear", "2018");
            renderAsciiTextField(acroForm, "toMonth", "7");
            renderAsciiTextField(acroForm, "toDay", "5");

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

            renderAsciiTextField(acroForm, "name", "Esther Piao");
            renderAsciiTextField(acroForm, "fromYear", "2018");
            renderAsciiTextField(acroForm, "fromMonth", "6");
            renderAsciiTextField(acroForm, "fromDay", "3");
            renderAsciiTextField(acroForm, "toYear", "2018");
            renderAsciiTextField(acroForm, "toMonth", "7");
            renderAsciiTextField(acroForm, "toDay", "5");

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
            BufferedImage[] bufImages = new BufferedImage[pages];
            int totalHeight = 0;
            for (int p = 0; p < pages; p++) {
                BufferedImage image = renderer.renderImage(p);
                bufImages[p] = image;
                totalHeight += image.getHeight();
            }
            pdfDocument.close();

            BufferedImage concatImage = new BufferedImage(bufImages[0].getWidth(), totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics g = concatImage.getGraphics();
            int nextHeight = 0;
            for (BufferedImage bi : bufImages) {
                g.drawImage(bi, 0, nextHeight, null);
                nextHeight += bi.getHeight();
            }

            ImageIO.write(concatImage, "jpg", new File("concatenated.jpg"));
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
        File f = new File("contract_form.pdf");
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len = -1;
        while ((len = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    private static void renderTextFieldWithFont(PDAcroForm acroForm, String name, String value) throws IOException {
        PDField field = (PDField) acroForm.getField(name);
        PDTextField tf = (PDTextField) field;

        // must change settings derived from original form.
        List<PDAnnotationWidget> widgets = tf.getWidgets();
        PDAnnotationWidget w = widgets.get(0);
        w.getCOSObject().setString(COSName.DA, "/F3 0 Tf 0 g");

        tf.setValue(value);
    }

    private static void renderAsciiTextField(PDAcroForm acroForm, String name, String value) throws IOException {
        PDField field = (PDField) acroForm.getField(name);

        field.setValue(value);
    }

    private static void renderImageField(PDDocument pdfDocument, PDAcroForm acroForm, String name, byte[] imageBytes) throws IOException {
        PDField buttonField = acroForm.getField(name);
        // System.out.println(buttonField.getClass().getSimpleName());
        // if (buttonField instanceof PDPushButton) {
        if (buttonField instanceof PDTextField) {
            // PDPushButton pdPushButton = (PDPushButton) buttonField;
            PDTextField pdPushButton = (PDTextField) buttonField;

            List<PDAnnotationWidget> widgets = pdPushButton.getWidgets();
            if (widgets != null && widgets.size() > 0) {
                PDAnnotationWidget annotationWidget = widgets.get(0); // just need one widget


                /*
                 * BufferedImage bufferedImage = ImageIO.read(imageFile);
                 * PDImageXObject pdImageXObject = LosslessFactory.createFromImage(document, bufferedImage);
                 */
                PDImageXObject pdImageXObject = PDImageXObject.createFromByteArray(pdfDocument, imageBytes, null);
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

    private static byte[] convertTextToImage(String text) {
        /*
           Because font metrics is based on a graphics context, we need to create
           a small, temporary image so we can ascertain the width and height
           of the final image
         */
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 20);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 0, fm.getAscent());
        g2d.dispose();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            // ImageIO.write(img, "png", new File("Text.png"));
            return baos.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;

    }


}
