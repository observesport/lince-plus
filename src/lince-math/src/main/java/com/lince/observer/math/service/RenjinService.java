package com.lince.observer.math.service;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.math.RenjinDataAttribute;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.renjin.primitives.matrix.StringMatrixBuilder;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * lince-scientific-base
 * .app.service
 * Created by Alberto Soto Fernandez in 10/08/2017.
 * Description:
 * <p>
`` * {@link <a href="http://docs.renjin.org/en/latest/library/execution-context.html">Renjin Execution Context</a>}
 * <p>
 * Include packages:
 * {@link <a href="http://docs.renjin.org/en/latest/library/using-packages.html">...</a> Using Packages}
 * {@link <a href="http://packages.renjin.org/packages">...</a> Renjin Packages}
 * <p>
 * <p>
 * JSON PACKAGE TO INCLUDE:
 * =======================
 * {@link <a href="https://cran.r-project.org/web/packages/jsonlite/vignettes/json-apis.html">JSON APIs</a> }
 * {@link <a href="http://packages.renjin.org/package/org.renjin.cran/jsonlite">jsonlite Package</a> }
 * {@link <a href="https://cran.r-project.org/web/packages/jsonlite/jsonlite.pdf">jsonlite PDF</a> }
 * <p>
 * Encryption
 * {@link <a href="https://www.guardsquare.com/en/proguard">ProGuard</a> }
 */
@Service
public class RenjinService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private RenjinScriptEngineFactory factory = null;

    /**
     * This allows us to maintain one independent RenjinScriptEngine
     * per thread, allowing your server to handle concurrent requests.
     */
    private static final ThreadLocal<ScriptEngine> ENGINE = new ThreadLocal<>();

    private static final String[] RFiles = {"integration/r-lang/basic-script.r"
            , "integration/r-lang/function-script.r"};

    /**
     * Loads renjin, via Lazy init
     * First time will be slow
     *
     * @return renjin script engine
     */
    private void saveInit() {
        // create a script engine manager:
        if (this.factory == null) {
            this.factory = new RenjinScriptEngineFactory();
        }
    }

    /**
     * Valid engine and context load to use with the console
     *
     * @param loadContext if context and all data mus be exposed or not
     * @return Valid ScriptEngine for renjin exposed via threads
     */
    private ScriptEngine getScriptEngine(boolean loadContext, List<RegisterItem> registerItems, List<CategoryData> categories, CategoryService categoryService) {
        ScriptEngine engine = ENGINE.get();
        if (engine == null) {
            // Create a new ScriptEngine for this thread if one does not exist.
            saveInit();
            engine = factory.getScriptEngine();
            ENGINE.set(engine);
        }
        //context load
        if (loadContext) {
            //Vector data = new Vector() {};
            //engine.put("x", 4);
            //engine.put("y", new double[]{1d, 2d, 3d, 4d});
            //engine.put("z", new DoubleArrayVector(1, 2, 3, 4, 5));
            HashMap ctx = new HashMap();
            ctx.put("hi", "code");
            ctx.put("x", 4);
            ctx.put("register", registerItems);
            engine.put(RenjinDataAttribute.DATA_MATRIX.getItemLabel(), getExposedData(true, registerItems, categories, categoryService));
            engine.put(RenjinDataAttribute.DATA_MATRIX_BY_CATEGORY.getItemLabel(), getExposedData(false, registerItems, categories, categoryService));
            engine.put(RenjinDataAttribute.CONTEXT.getItemLabel(), ctx);
        }
        return engine;
    }


    /**
     * Casts analysis data to R Data as a Matrix
     *
     * @param concatCategories all categories are join and separated by coma or not
     * @return Lince Data Matrix
     */
    public StringVector getExposedData(boolean concatCategories, List<RegisterItem> currentData, List<CategoryData> categories, CategoryService categoryService) {
        try {
//            List<RegisterItem> currentData = analysisService.getOrderedRegister();
//            List<CategoryData> categories = categoryService.getCollection();
            //videotime, name, frames, categories(concat code with ",")
            StringMatrixBuilder data = null;
            List<String> colNames = new ArrayList<>();
            colNames.add("VideoTime");
            colNames.add("SceneName");
            colNames.add("StartFrame");
            String[] categoryNames = null;
            if (concatCategories) {
                colNames.add("Categories");
                data = new StringMatrixBuilder(currentData.size(), 4);
            } else {
                data = new StringMatrixBuilder(currentData.size(), 3 + categories.size());
                for (CategoryData cats : categories) {
                    colNames.add(cats.getCode());
                    //safe init para la columna
                    //for (int i = 0; i < )
                    //    colNames.size() - 1
                }
            }
            data.setColNames(colNames);
            int currentRow = 0;
            for (RegisterItem reg : currentData) {
                data.setValue(currentRow, 0, reg.getVideoTimeMillis());
                data.setValue(currentRow, 1, reg.getName());
                data.setValue(currentRow, 2, reg.getFrames());
                if (concatCategories) {
                    //works
                    String categoriesValue = StringUtils.EMPTY;
                    int currentCat = 0;
                    for (Category cat : reg.getRegister()) {
                        categoriesValue += cat.getCode();
                        currentCat++;
                        if (reg.getRegister().size() > currentCat) {
                            categoriesValue += ", ";
                        }
                    }
                    data.setValue(currentRow, 3, categoriesValue);
                } else {
                    for (Category cat : reg.getRegister()) {
                        //para cada serie asociada, que es una categoria por nombre, miro si tengo o no un registro asociado
                        Pair<Criteria, Category> relatedData = categoryService.findToolEntryByIdentifier(null, cat.getCode(), null);
                        //cada escena tiene que estar añadida por orden
                        if (relatedData != null && relatedData.getKey() != null) {
                            for (int i = 0; i < colNames.size(); i++) {
                                if (StringUtils.contains(colNames.get(i), relatedData.getKey().getCode())) {
                                    //es el indice correcto añadimos el valor
                                    if (relatedData.getKey().isInformationNode()) {
                                        data.setValue(currentRow, i, cat.getNodeInformation());
                                    } else {
                                        data.setValue(currentRow, i, cat.getCode());
                                    }
                                }
                            }
                        }
                    }
                }
                currentRow++;
            }
            /* Basic example usage (don't delete, no documentation available)
             StringMatrixBuilder data = new StringMatrixBuilder(1, 2);
            //enumeration starts with 0, R at context changes it to 1
            data.setValue(0, 0, "yuhu");
            data.setValue(0, 1, 3);
            colNames.add("name");
            */
            return data.build();
        } catch (Exception e) {
            log.error("Trying to convert lince data to R Vector", e);
        }
        return null;
    }

    /**
     * Tipo de elemento perfecto para retos
     *
     * @return
     */
    public Vector getCountExposedData() {
        try {

        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Loads a file from resource path
     *
     * @param resourcePath located resource file
     * @return FileReader for valid usage with renjin
     */
    private FileReader getFileReader(String resourcePath) {
        try {
            if (StringUtils.isNotEmpty(resourcePath)) {
                ClassLoader classLoader = getClass().getClassLoader();
                File file = new File(classLoader.getResource(resourcePath).getFile());
                FileReader reader = new FileReader(file);
                return reader;
            }
        } catch (Exception e) {
            log.error("Filereader", e);
        }
        return null;
    }

//    /**
//     * Executes a simple script on console
//     */
//    public void tryRService() {
//        try {
//            ScriptEngine engine = getScriptEngine(false, null, null, null);
//            engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
//            engine.eval("print(df)");
//            engine.eval("print(lm(y ~ x, df))");
//        } catch (Exception e) {
//            log.error("RenjinService", e);
//        }
//    }

//    /**
//     * Loads a simple file with an r script
//     */
//    public void tryRFile() {
//        try {
//            ScriptEngine engine = getScriptEngine(false, null, null, null);
//            engine.eval(getFileReader("integration/r-lang/function-script.r"));
//        } catch (Exception e) {
//            log.error("RenjinService", e);
//        }
//    }
//
//
//    public void tryExecuteFileByLine() {
//        try {
//            ScriptEngine engine = getScriptEngine(false, null, null, null);
//            //ClassLoader classLoader = getClass().getClassLoader();;
//            //String pat = classLoader.getResource("integration/r-lang/basic-script.r").getPath();
//            //Stream<String> stream = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("integration/r-lang/basic-script.r"))).lines();
//            Path path = Paths.get(this.getClass().getClassLoader().getResource("integration/r-lang/basic-script.r").toURI());
//            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
//                /* Replace server uri
//                if (line.contains("1313131")) {
//                    newLines.add(line.replace("1313131", "" + System.currentTimeMillis()));
//                }
//                */
//                engine.eval(line);
//            }
//        } catch (Exception e) {
//            log.error("RenjinService", e);
//        }
//    }


    /**
     * Executes Renjin script using the given data and input commands.
     *
     * @param currentData the list of RegisterItem objects representing the current data
     * @param categories  the list of CategoryData objects representing the categories
     * @param input       the array of input commands to be executed in Renjin
     * @return the output of the executed script as a String
     * @throws ScriptException if there is an error in the Renjin script
     */
    public String executeRenjin(List<RegisterItem> currentData, List<CategoryData> categories, CategoryService categoryService, String... input) throws ScriptException {
        String outputTxt;
        //try {
        StringWriter output = new StringWriter();
        StringWriter err = new StringWriter();
        ScriptEngine engine = getScriptEngine(true, currentData, categories, categoryService);
        engine.getContext().setWriter(output);
        engine.getContext().setErrorWriter(err);
        engine.eval("import(java.util.HashMap)");
        engine.eval("import(com.lince.observer.data.bean.RegisterItem)");
        engine.eval("library(e1071)");
        engine.eval("library('org.renjin.cran:KappaGUI')");
        engine.eval("library('org.renjin.cran:irr')");
        //engine.eval("data(iris)");
        //engine.eval("svmfit <- svm(Species~., data=iris)");
        //((StringArrayVector)engine.eval("svmfit$levels")).values
        for (String line : input) {
            engine.eval(line);
        }
        outputTxt = output.toString();
        return outputTxt;
    }

}
