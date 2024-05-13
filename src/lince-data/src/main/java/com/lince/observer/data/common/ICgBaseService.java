package com.lince.observer.data.common;

import java.util.List;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.base
 * @author berto (alberto.soto@gmail.com)in 20/01/2016.
 * Description:
 */
public interface ICgBaseService<TO> {
    List<TO> findAll();
    TO save(TO item);
    TO update(TO item);
    TO findOne(Long id);
    boolean doesExist(TO item);
    void delete(Long id);
}
