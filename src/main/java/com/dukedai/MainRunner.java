package com.dukedai;

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
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainRunner {

    public static void main(String[] args) throws Exception {
        renderAdvance();

        renderSimple();
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
        System.out.println(buttonField.getClass().getSimpleName());
        if (buttonField instanceof PDPushButton) {
            PDPushButton pdPushButton = (PDPushButton) buttonField;

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
                    System.out.println("Image '" + filePath + "' inserted");

                } else {
                    System.err.println("File " + filePath + " not found");
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
