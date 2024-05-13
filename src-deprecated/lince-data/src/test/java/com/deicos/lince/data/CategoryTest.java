package com.deicos.lince.data;

import com.deicos.lince.data.bean.categories.Criteria;
import org.junit.Assert;
import org.junit.Test;

/**
 * com.deicos.lince.data
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

        Assert.assertEquals(c,d);
    }

    @Test
    public void testAdd() {
        String str = "Junit is working fine";
        Assert.assertEquals("Junit is working fine",str);
    }
}

