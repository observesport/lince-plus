package com.deicos.lince.data.bean.categories;

/**
 * com.deicos.lince.data.bean.categories
 * Class NodeInfo
 * 01/11/2019
 *
 * @author berto (alberto.soto@gmail.com)
 *
 * Interface to modify criteria and category behavior allowing to set additional info
 *
 * Use: save information on an scene
 */
public interface INodeInfo {
    public boolean isInformationNode();
    public void setInformationNode(boolean nodeInfo);
    public void setNodeInformation(String nodeInfo);
    public String getNodeInformation();
}
