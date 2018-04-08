package com.dukedai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;

public class MainRunner {

    public static void main(String[] args) throws Exception{
        PDDocument pdfDocument = PDDocument.load(new File("pdf_template.pdf"));

        PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();

        if (acroForm != null)
        {
            PDField field = (PDField) acroForm.getField( "field1" );
            field.setValue("new field value 1");

            field = (PDField) acroForm.getField( "field2" );
            field.setValue("new field value 2");

            field = (PDField) acroForm.getField( "field3" );
            field.setValue("new field value 3");
        }

        pdfDocument.save("fill.pdf");
        pdfDocument.close();
    }
}
