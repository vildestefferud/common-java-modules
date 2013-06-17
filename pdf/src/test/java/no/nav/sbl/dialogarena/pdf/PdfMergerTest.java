package no.nav.sbl.dialogarena.pdf;

import no.nav.sbl.dialogarena.detect.IsPdf;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.pdf.PdfTestUtils.getBytesFromFile;
import static no.nav.sbl.dialogarena.pdf.PdfTestUtils.writeBytesToFile;
import static no.nav.sbl.dialogarena.test.match.Matchers.match;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PdfMergerTest {

    List<byte[]> dokumenter;

    @Before
    public void setup() throws IOException {
        dokumenter = new ArrayList<>();
        byte[] side1 = getBytesFromFile("/PdfMergerFiles/skjema1_side1_fra_png.pdf");
        byte[] side2 = getBytesFromFile("/PdfMergerFiles/skjema1_side2_fra_jpg.pdf");
        byte[] side3 = getBytesFromFile("/PdfMergerFiles/skjema1_side3.pdf");
        byte[] side45 = getBytesFromFile("/PdfMergerFiles/skjema1_side4-5.pdf");
        dokumenter.add(side1);
        dokumenter.add(side2);
        dokumenter.add(side3);
        dokumenter.add(side45);
    }

    @Test
    public void resultatDokumentSkalHaRiktigAntallSider() throws IOException {
        int inputPages = 0;
        for (byte[] dokument : dokumenter) {
            inputPages += PdfFunctions.PDF_SIDEANTALL.transform(dokument);
        }
        byte[] mergedBytes = new PdfMerger().transform(dokumenter);
        assertThat(mergedBytes, match(new IsPdf()));
        int outputPages = PdfFunctions.PDF_SIDEANTALL.transform(mergedBytes);
        assertThat(inputPages, is(outputPages));

        writeBytesToFile(mergedBytes, "/PdfMergerFiles", "/skjema_sammenslått.pdf");
    }

    @Test(expected = IllegalArgumentException.class)
    public void skalKasteExceptionForUlovligeFiler() throws IOException {
        List<byte[]> filer = new ArrayList<>();

        byte[] pdf = getBytesFromFile("/PdfMergerFiles/skjema1_side3.pdf");
        byte[] txt = getBytesFromFile("/PdfToImageFiles/illegal-file.txt");
        filer.add(pdf);
        filer.add(txt);

        new PdfMerger().transform(filer);
    }
}
