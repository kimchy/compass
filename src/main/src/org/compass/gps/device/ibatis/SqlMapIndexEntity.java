package org.compass.gps.device.ibatis;

import org.compass.gps.device.support.parallel.GenericIndexEntity;

/**
 * @author kimchy
 */
public class SqlMapIndexEntity extends GenericIndexEntity {

    private String statementId;

    private Object param;

    public SqlMapIndexEntity(String name, String[] subIndexes, String statementId, Object param) {
        super(name, subIndexes);
        this.statementId = statementId;
        this.param = param;
    }

    public String getStatementId() {
        return statementId;
    }

    public Object getParam() {
        return param;
    }
}
