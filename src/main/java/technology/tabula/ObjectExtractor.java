package technology.tabula;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class ObjectExtractor {

    private final PDDocument pdfDocument;

    public ObjectExtractor(PDDocument pdfDocument) {
        this.pdfDocument = pdfDocument;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    protected Page extractPage(Integer pageNumber) throws IOException {
        if (pageNumber > pdfDocument.getNumberOfPages() || pageNumber < 1) {
            throw new java.lang.IndexOutOfBoundsException("Page number does not exist.");
        }
        PDPage page = pdfDocument.getPage(pageNumber - 1);

        ObjectExtractorStreamEngine streamEngine = new ObjectExtractorStreamEngine(page);
        streamEngine.processPage(page);

        TextStripper textStripper = new TextStripper(pdfDocument, pageNumber);
        textStripper.process();

        Utils.sort(textStripper.textElements, Rectangle.ILL_DEFINED_ORDER);

        float width, height;
        int rotation = page.getRotation();
        if (Math.abs(rotation) == 90 || Math.abs(rotation) == 270) {
            width = page.getCropBox().getHeight();
            height = page.getCropBox().getWidth();
        } else {
            width = page.getCropBox().getWidth();
            height = page.getCropBox().getHeight();
        }

        return new Page(0, 0, width, height, rotation, pageNumber, page, pdfDocument, streamEngine, textStripper);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public PageIterator extract(Iterable<Integer> pages) {
        return new PageIterator(this, pages);
    }

    public PageIterator extract() {
        return extract(Utils.range(1, pdfDocument.getNumberOfPages() + 1));
    }

    public Page extract(int pageNumber) {
        return extract(Utils.range(pageNumber, pageNumber + 1)).next();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public void close() throws IOException {
        pdfDocument.close();
    }
    
}
