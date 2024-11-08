package com.lince.observer.data.common;

import com.lince.observer.data.structure.BeanFormConfig;

import java.io.Serializable;
import java.util.List;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.common
 * @author berto (alberto.soto@gmail.com)in 20/01/2016.
 * Description:
 */
public interface CgCoreMessage extends Serializable {
    Long getId();
    List<BeanFormConfig> viewFormConfig();
}
