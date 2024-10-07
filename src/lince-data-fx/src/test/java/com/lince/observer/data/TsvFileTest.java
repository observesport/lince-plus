package com.lince.observer.data;

import com.lince.observer.data.base.TsvFileHelper;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * com.lince.observer.data
 * Class TsvFileTest
 * 12/06/2019
 * <p>
 * Tab separator file for univocity
 * <p>
 * https://www.univocity.com/pages/univocity_parsers_tsv.html#working-with-tsv
 * https://www.univocity.com/pages/univocity_parsers_writing.html#writing
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class TsvFileTest {

    @Test
    public void readTest() throws FileNotFoundException {
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        // creates a TSV parser
        TsvParser parser = new TsvParser(settings);
        // parses all rows in one go.
        List<String[]> allRows = parser.parseAll(GenericFileUtils.getInputReader("exampleTabFile.tsv"));
        for (String[] data : allRows) {
            System.out.println(ArrayUtils.toString(data));
        }
    }

    @Test
    public void writeTest() throws IOException {
        // As with the CsvWriter, all you need is to create an instance of TsvWriter with the default TsvWriterSettings.
        TsvWriter writer = new TsvWriter(GenericFileUtils.getWriter("tabOutput.tsv"), new TsvWriterSettings());
        String[][] matrix = {{"10", "20", "OK"}, {"5", "30", "KO"}, {"20", "100", "NA"}, {"10", "60", "OK"}};
        // Write the record headers of this file
        writer.writeHeaders("Year", "Make", "Model");
        // Here we just tell the writer to write everything and close the given output Writer instance.
        writer.writeRowsAndClose(matrix);
    }

    @Test
    public void writeCommentedCSV() throws IOException {
        CsvWriterSettings settings = new CsvWriterSettings();
        // Sets the character sequence to write for the values that are null.
        settings.setNullValue("?");
        //Changes the comment character to -
        settings.getFormat().setComment('-');
        // Sets the character sequence to write for the values that are empty.
        settings.setEmptyValue("!");
        // writes empty lines as well.
        settings.setSkipEmptyLines(false);
        // Creates a writer with the above settings;
        CsvWriter writer = new CsvWriter(GenericFileUtils.getWriter("tabOutput.csv"), settings);
        // writes the file headers
        List<String> rows = new ArrayList<>(Arrays.asList("10", "20", "OK"));
        writer.writeHeaders("a", "b", "c", "d", "e");
        // Let's write the rows one by one (the first row will be skipped)
        for (int i = 1; i < rows.size(); i++) {
            // You can write comments above each row
            writer.commentRow("This is row " + i);
            // writes the row
            writer.writeRow(rows.get(i));
        }
        // we must close the writer. This also closes the java.io.Writer you used to create the CsvWriter instance
        // note no checked exceptions are thrown here. If anything bad happens you'll get an IllegalStateException wrapping the original error.
        writer.close();
    }

    @Test
    public void writeThemeFile() throws IOException {
        TsvWriter writer = new TsvWriter(GenericFileUtils.getWriter("theme6File.vvt"), new TsvWriterSettings());
        //flushes all values to the output, creating a row.
        Map<String, List<String>> aux = new HashMap<>();
        aux.put("cat1", Arrays.asList("cri1", "cri2", "cri3"));
        aux.put("cat2", Arrays.asList("cri11", "cri12", "cri13"));
        for (Map.Entry<String,List<String>> entry : aux.entrySet()) {
            writer.writeRow(entry.getKey());
            for(String cri:entry.getValue()){
                writer.writeRow(StringUtils.EMPTY,cri);
            }
        }
        writer.close();
    }

    @Test
    public void writeLinceTsvFile(){
        Map<String, List<String>> aux = new HashMap<>();
        aux.put("cat1", Arrays.asList("cri1", "cri2", "cri3"));
        aux.put("cat2", Arrays.asList("cri11", "cri12", "cri13"));
        TsvFileHelper helper = new TsvFileHelper(aux);
        helper.write2File(GenericFileUtils.getResourcePath("lince2Theme.vvt"));
    }

}
