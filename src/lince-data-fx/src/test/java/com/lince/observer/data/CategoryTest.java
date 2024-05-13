package com.lince.observer.data;

import com.lince.observer.data.bean.categories.Criteria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * com.lince.observer.data
 * Class CategoryTest
 * @author berto (alberto.soto@gmail.com). 14/12/2018
 */
public class CategoryTest {
    @Test
    public void testCategoryEquals() {
        Criteria c = new Criteria();
        c.setCode("C1");
        c.setId(0);

        Criteria d = new Criteria();
        d.setCode("C1");
        d.setId(1);

        Assertions.assertEquals(c,d);
    }

    @Test
    public void testAdd() {
        String str = "Junit is working fine";
        Assertions.assertEquals("Junit is working fine",str);
    }
}

