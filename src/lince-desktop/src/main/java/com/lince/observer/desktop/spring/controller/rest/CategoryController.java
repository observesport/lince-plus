package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.CgEnum;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryContainer;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.common.DataTableWrapper;
import com.lince.observer.data.common.EnumStructure;
import com.lince.observer.math.service.CategoryServiceImpl;
import com.lince.observer.math.service.CategoryServiceOld;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;

/**
 * lince-scientific-desktop
 * .controller.rest
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

    private final CategoryServiceImpl categoryService;

    private final CategoryServiceOld oldCategoryService;

    @Autowired
    public CategoryController(CategoryServiceImpl categoryService, CategoryServiceOld oldCategoryService) {
        this.categoryService = categoryService;
        this.oldCategoryService = oldCategoryService;
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
                nodeList.setNodes(oldCategoryService.getChildren(id));
                if (nodeList.getNodes().size() < 1 && id == 0) {
                    oldCategoryService.generateDummyCollection();
                }
                nodeList.setNodes(oldCategoryService.getChildren(id));
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
                categoryService.pushAll(items.getData());
            }
            return new ResponseEntity<>(new DataTableWrapper<>(categoryService.getCollection()), HttpStatus.OK);
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
                rtn = oldCategoryService.pushCategory(item.getName(), item.getPosition(), item.getRelated());
            } else {
                //which level is? preventing user usage, we will replace it at the same level
                Pair<Criteria, Category> parentData = oldCategoryService.findTreeEntryById(item.getParent(), null, null);
                Integer parent = item.getParent();
                if (parentData.getValue() != null) {
                    //es una categoría, generamos uno al mismo nivel
                    parent = parentData.getValue().getParent();
                }
                rtn = oldCategoryService.pushCriteria(parent, item.getName(), item.getPosition(), item.getRelated());
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
            CategoryData rtn = oldCategoryService.update(id, item);
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
            oldCategoryService.move(id, item);
            return new ResponseEntity<>(item, HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:move/" + id, e);
            return new ResponseEntity<>(new Criteria(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=*")
    public ResponseEntity<CategoryData> delete(@PathVariable Integer id) {
        try {
            oldCategoryService.delete(id, null);
            return new ResponseEntity<>(new Criteria(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("category:delete/" + id, e);
            return new ResponseEntity<>(new Criteria(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
