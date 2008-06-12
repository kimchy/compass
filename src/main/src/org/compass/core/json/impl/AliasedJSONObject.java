package org.compass.core.json.impl;

import org.compass.core.json.AliasedJsonObject;

/**
 * @author kimchy
 */
public class AliasedJSONObject extends JSONObject implements AliasedJsonObject {

    private String alias;

    public AliasedJSONObject(String alias) {
        super();
        this.alias = alias;
    }

    public AliasedJSONObject(String alias, JSONTokener x) throws JSONException {
        super(x);
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}
