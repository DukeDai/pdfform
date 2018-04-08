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
import java.util.List;

public class MainRunner {

    public static void main(String[] args) throws Exception {
        PDDocument pdfDocument = PDDocument.load(new File("form_template.pdf"));

        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();

        if (acroForm != null) {
            PDField field = (PDField) acroForm.getField("field1");
            field.setValue("new field value 1");

            field = (PDField) acroForm.getField("field2");
            field.setValue("new field value 2");

            field = (PDField) acroForm.getField("field3");
            field.setValue("new field value 3");

            PDField buttonField = acroForm.getField("field4");
            System.out.println(buttonField.getClass().getSimpleName());
            if (buttonField instanceof PDPushButton) {
                PDPushButton pdPushButton = (PDPushButton) buttonField;

                List<PDAnnotationWidget> widgets = pdPushButton.getWidgets();
                if (widgets != null && widgets.size() > 0) {
                    PDAnnotationWidget annotationWidget = widgets.get(0); // just need one widget

                    String filePath = "signature.jpeg";
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

            pdfDocument.save("fill.pdf");
            pdfDocument.close();
        }
    }

    private static PDRectangle getFieldArea(PDField field) {
        COSDictionary fieldDict = field.getCOSObject();
        COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);
        return new PDRectangle(fieldAreaArray);
    }
}
