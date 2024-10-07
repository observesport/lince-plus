package com.lince.observer.data.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Class BaseRestControllerWrapper
 * @author berto (alberto.soto@gmail.com). 17/04/2018
 * Helper for returning valid REST data
 */
public abstract class BaseRestControllerWrapper {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Helper function for returning internal items
     *
     * @param collection
     * @return
     */
    protected <TO> ResponseEntity<List<TO>> getResponseItemList(List<TO> collection) {
        try {
            if (collection.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(collection, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper function for returning internal items
     *
     * @param item
     * @return
     */
    protected <TO> ResponseEntity<TO> getResponseItem(TO item) {
        try {
            if (item == null) {
                log.info("Element not found");
                return new ResponseEntity<TO>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<TO>(item, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DARK ZONE AREA
     * OJO: Paja mental al canto
     * Ejecuta en functional la funcion adecuada y devuelve su serialización adecuada para REST
     * Existe control de errores
     *
     * @param initialData
     * @param f
     * @return
     */
    protected <TO, R> ResponseEntity<List<TO>> executeResponseItem(R initialData
            , Function<R, TO> f) {
        try {
            //Se tiene que devolver lista para que sea serializable
            List<TO> rtn = new ArrayList<>();
            TO item = f.apply(initialData);
            rtn.add(item);
            return getResponseItem(rtn);
        } catch (Exception e) {
            log.error("Functional rest code error", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected ResponseEntity getNotImplementedResponse(){
        return new ResponseEntity(new ArrayList<>(), HttpStatus.NOT_IMPLEMENTED);
    }

}
