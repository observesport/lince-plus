package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.controller.RenjinController;
import com.lince.observer.math.service.RenjinService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
/**
 * lince-scientific-base
 * .app.spring.controller.rest
 * Created by Alberto Soto Fernandez in 10/08/2017.
 * Description:
 */
@RestController
@RequestMapping(value = RenjinController.RQ_MAPPING_NAME)
public class RenjinControllerImpl implements RenjinController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final RenjinService renjinService;

    public RenjinControllerImpl(RenjinService renjinService) {
        this.renjinService = renjinService;
    }

    @Override
    @RequestMapping(value = "/execute", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> execute(HttpServletRequest request, @RequestBody String code) {
        try {
            if (StringUtils.startsWith(code,"\"")){
                code = StringUtils.removeStart(code,"\"");
                code = StringUtils.removeEnd(code,"\"");
                code = StringUtils.replace(code,"}","};"); //OJO!
            }
            //System.getProperty("line.separator") String.split(System.getProperty("line.separator"));
            String[] codes = StringUtils.splitByWholeSeparator(code,"\\n");
            int index=-1;
            for (String line:codes){
                index++;
                if (StringUtils.contains(line,"#")){
                    codes[index] = StringUtils.substringBefore(line,"#");
                }
            }
            //renjinService.tryRFile();
            String output = renjinService.executeRenjin(StringUtils.join(codes));
            return new ResponseEntity<>(output, HttpStatus.OK);
        } catch (Exception e) {
            log.error("renjin:execute/", e);
            //Queremos que la traza salga a usuario para corregir el script
            //si no: HttpStatus.INTERNAL_SERVER_ERROR
            return new ResponseEntity<>(e.toString(), HttpStatus.OK);
        }
    }


}
