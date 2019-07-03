package com.deicos.lince.app.spring.controller.rest;

import com.deicos.lince.app.base.common.CgEnum;
import com.deicos.lince.app.base.common.DataTableWrapper;
import com.deicos.lince.app.base.common.EnumStructure;
import com.deicos.lince.data.bean.categories.Category;
import com.deicos.lince.data.bean.categories.CategoryContainer;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.math.service.CategoryService;
import com.deicos.lince.math.service.CategoryServiceOld;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * lince-scientific-desktop
 * com.deicos.lince.controller.rest
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 * <p>
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = CategoryController.RQ_MAPPING_NAME)
public class CategoryController /*extends CgEngineBaseController<Book, BookServiceImpl>*/ {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    static final String RQ_MAPPING_NAME = "/categories";

    private final CategoryService newCategoryService;

    private final CategoryServiceOld categoryService;

    @Autowired
    public CategoryController(CategoryService newCategoryService, CategoryServiceOld categoryService) {
        this.newCategoryService = newCategoryService;
        this.categoryService = categoryService;
    }

    protected String getActionName() {
        return RQ_MAPPING_NAME;
    }

    private ResponseEntity<HashMap<String, String>> getItemCollectionForEnum(CgEnum enumType) {
        EnumStructure cont = new EnumStructure();
        cont.setEnumData(enumType.getClass().getEnumConstants());
        return new ResponseEntity<>(cont.getDataValues(), HttpStatus.OK);
    }

    /**
     * Obtiene todos los nodos del nivel ID
     *
     * @param txtId
     * @return
     */
    @RequestMapping(value = "/get/{txtId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryContainer> getCategories(@PathVariable String txtId) {
        CategoryContainer nodeList = new CategoryContainer();
        try {
            if (StringUtils.isNotEmpty(txtId)) {
                Integer id = Integer.valueOf(txtId);
                nodeList.setNodes(categoryService.getChildren(id));
                if (nodeList.getNodes().size() < 1 && id == 0) {
                    categoryService.generateDummyCollection();
                }
                nodeList.setNodes(categoryService.getChildren(id));
            }
            return new ResponseEntity<>(nodeList, HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:get/" + txtId, e);
            return new ResponseEntity<>(new CategoryContainer(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/saveAll/{gen}", method = RequestMethod.POST)
    public ResponseEntity<DataTableWrapper<CategoryData>> saveAll(HttpServletRequest request
            , @PathVariable("gen") Boolean doGen
            , @RequestBody DataTableWrapper<Criteria> items) {
        try {
            if (!items.getData().isEmpty()) {
                newCategoryService.pushAll(items.getData());
            }
            return new ResponseEntity<>(new DataTableWrapper<>(newCategoryService.getCollection()), HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:save", e);
            return new ResponseEntity<>(new DataTableWrapper<CategoryData>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/save", method = RequestMethod.POST/*, produces = MediaType.APPLICATION_JSON_VALUE, headers="Accept=*"*/)
    //@ResponseBody
    public ResponseEntity<CategoryData> save(HttpServletRequest request, @RequestBody CategoryData item) {
        try {
            CategoryData rtn;
            if (item.getLevel() == 0 && item.getParent() == 0) {
                rtn = categoryService.pushCategory(item.getName(), item.getPosition(), item.getRelated());
            } else {
                //which level is? preventing user usage, we will replace it at the same level
                Pair<Criteria, Category> parentData = categoryService.findTreeEntryById(item.getParent(), null, null);
                Integer parent = item.getParent();
                if (parentData.getValue() != null) {
                    //es una categoría, generamos uno al mismo nivel
                    parent = parentData.getValue().getParent();
                }
                rtn = categoryService.pushCriteria(parent, item.getName(), item.getPosition(), item.getRelated());
            }
            return new ResponseEntity<>(rtn, HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:save", e);
            return new ResponseEntity<>(new Criteria(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    //@ResponseBody
    public ResponseEntity<CategoryData> update(@PathVariable Integer id, @RequestBody CategoryData item) {
        try {
            CategoryData rtn = categoryService.update(id, item);
            return new ResponseEntity<>(rtn, HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:update/" + id, e);
            return new ResponseEntity<>(new Criteria(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/move/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    //@ResponseBody
    public ResponseEntity<CategoryData> move(@PathVariable Integer id, @RequestBody CategoryData item) {
        try {
            categoryService.move(id, item);
            return new ResponseEntity<>(item, HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:move/" + id, e);
            return new ResponseEntity<>(new Criteria(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<CategoryData> delete(@PathVariable Integer id) {
        try {
            categoryService.delete(id, null);
            return new ResponseEntity<>(new Criteria(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:delete/" + id, e);
            return new ResponseEntity<>(new Criteria(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
