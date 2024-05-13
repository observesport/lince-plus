package com.deicos.lince.ai.agreement;

import org.apache.commons.lang3.ArrayUtils;
import org.dkpro.statistics.agreement.coding.ICodingAnnotationItem;
import org.dkpro.statistics.agreement.coding.ICodingAnnotationStudy;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

/**
 * com.deicos.lince.ai.agreement
 * Class LinceContingencyMatrixPrinter
 * 13/04/2019
 *
 *
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class LinceContingencyMatrixPrinter {
    private static final Logger log = LoggerFactory.getLogger(LinceContingencyMatrixPrinter.class);
    private final ICodingAnnotationStudy study;
    private Map<Object, Integer> categories;

    public LinceContingencyMatrixPrinter(final ICodingAnnotationStudy study) {
        if (study.getRaterCount() > 2)
            throw new IllegalArgumentException("Contingency tables are only applicable for two rater studies.");
        this.study = study;
        Map<Object, Integer> categories = new LinkedHashMap<Object, Integer>();
        for (Object cat : study.getCategories())
            categories.put(cat, categories.size());
        this.categories = categories;
    }

    /**
     * Only suitable for 2 observers
     *
     * Reduces input study to valid matrix
     * @return int matrix detecting same observation
     */
    public int[][] getContingencyMatrix() {
        try {
            int[][] frequencies = new int[study.getCategoryCount()][study.getCategoryCount()];
            for (ICodingAnnotationItem item : study.getItems()) {
                int cat1 = categories.get(item.getUnit(0).getCategory());
                int cat2 = categories.get(item.getUnit(1).getCategory());
                frequencies[cat1][cat2]++;
            }
            return frequencies;
        } catch (Exception e) {
            log.error("getMatrix", e);
            return null;
        }
    }

    /**
     * Adds study categories as a headline
     * @return List of categories for the study
     */
    public List<String> getContingencyMatrixHeader() {
        try {
            List<String> aux = new ArrayList<>();
            for (Object category : categories.keySet()) {
                aux.add(category.toString());
            }
            aux.add("Σ");
            return aux;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calculates the totals by row
     * @param data observation matrix for 2 observers
     * @return original data with total by row
     */
    private int[][] addTotalsByRow(int[][] data) {
        int i = 0;
        int[] colSum = new int[study.getCategoryCount()];
        for (Object category1 : categories.keySet()) {
            int rowSum = 0;
            for (int j = 0; j < categories.size(); j++) {
                rowSum += data[i][j];
                colSum[j] += data[i][j];
            }
            //añadimos rowsum a la columna
            data[i] = ArrayUtils.add(data[i], rowSum);
            i++;
        }
        data = addTotalsByColumn(data, colSum);
        return data;
    }

    /**
     * Modifies the reducing table adding a column with total by column
     * @param data   observation matrix with totals in each line at the end
     * @param colSum collection with totals by col
     * @return internal set for rendering
     */
    private int[][] addTotalsByColumn(int[][] data, int[] colSum) {
        int rowSum = 0;
        int[] totals = new int[study.getCategoryCount() + 1];
        for (int j = 0; j < categories.size(); j++) {
            rowSum += colSum[j];
            totals[j] = colSum[j];
        }
        totals[study.getCategoryCount()] = rowSum;
        //let's add the new row
        data = ArrayUtils.add(data, totals);
        return data;
    }

    /**
     * Returns a valid serialization for web rendering
     * @param addHeader if columns and rows should show totals
     * @return valid JSON array
     */
    public JSONArray getContingencyMatrixJson(boolean addHeader) {
        try {
            JSONArray jsonArray = new JSONArray();
            int[][] data = getContingencyMatrix();
            if (!addHeader) {
                jsonArray.put(data);
            } else {
                //shift first column
                List<String> header = getContingencyMatrixHeader();
                List<String> jsonHeader = new ArrayList<>(header);
                jsonHeader.add(0, "");
                //insert
                jsonArray.put(jsonHeader);
                data = addTotalsByRow(data);
                int position = 0;
                for (int[] rowData : data) {
                    //dark area: streams and array conversions. tricky.
                    IntStream intStream = Arrays.stream(rowData);
                    String[] row = intStream.mapToObj(String::valueOf).toArray(String[]::new);
                    List<String> newRow = new ArrayList<>();
                    newRow.add(header.get(position));
                    Collections.addAll(newRow, row);
                    //end dark area.
                    jsonArray.put(newRow);
                    position++;
                }
                return jsonArray;
            }
        } catch (Exception e) {
            log.error("getContingencyMatrixJson", e);
        }
        return null;
    }

}
