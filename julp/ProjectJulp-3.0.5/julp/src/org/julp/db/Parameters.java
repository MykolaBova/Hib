package org.julp.db;

import java.sql.ParameterMetaData;
import java.sql.SQLException;

public class Parameters implements ParameterMetaData, java.io.Serializable {

    private static final long serialVersionUID = -4069317946182804139L;
    /** @params - the first parameter is 1, the second is 2, ... */
    protected Object[] params;
    protected int[] parameterMode;
    protected int[] parameterType;
    protected String[] parameterTypeName;
    protected int[] precision;
    protected int[] scale;
    protected int[] nullable;
    protected boolean[] signed;
    protected String[] parameterName;
    protected int parameterCount;

    public Parameters() {
    }

    public Parameters(int parameterCount) {
        this.params = new Object[parameterCount];
        init();
    }

    private void init() {
        this.parameterCount = this.params.length;
        this.parameterMode = new int[parameterCount];
        this.parameterType = new int[parameterCount];
        this.parameterTypeName = new String[parameterCount];
        this.precision = new int[parameterCount];
        this.scale = new int[parameterCount];
        this.nullable = new int[parameterCount];
        this.signed = new boolean[parameterCount];
        this.parameterName = new String[parameterCount];
    }

    public Parameters(Object[] params) {
        setParams(params);
    }

    public String getParameterClassName(int index) throws SQLException {
        return params[index - 1].getClass().getName();
    }

    @Override
    public int getParameterCount() throws SQLException {
        return parameterCount;
    }

    @Override
    public int getParameterMode(int index) throws SQLException {
        return parameterMode[index - 1];
    }

    public void setParameterMode(int index, int parameterMode) throws SQLException {
        this.parameterMode[index - 1] = parameterMode;
    }

    @Override
    public int getParameterType(int index) throws SQLException {
        return parameterType[index - 1];
    }

    public void setParameterType(int index, int parameterType) throws SQLException {
        this.parameterType[index - 1] = parameterType;
    }

    @Override
    public String getParameterTypeName(int index) throws SQLException {
        return parameterTypeName[index - 1];
    }

    public void setParameterTypeName(int index, String parameterTypeName) throws SQLException {
        this.parameterTypeName[index - 1] = parameterTypeName;
    }

    @Override
    public int getPrecision(int index) throws SQLException {
        return precision[index - 1];
    }

    public void setPrecision(int index, int precision) throws SQLException {
        this.precision[index - 1] = precision;
    }

    @Override
    public int getScale(int index) throws SQLException {
        return scale[index - 1];
    }

    public void setScale(int index, int scale) throws SQLException {
        this.scale[index - 1] = scale;
    }

    @Override
    public int isNullable(int index) throws SQLException {
        return nullable[index - 1];
    }

    public void setNullable(int index, int nullable) throws SQLException {
        this.nullable[index - 1] = nullable;
    }

    @Override
    public boolean isSigned(int index) throws SQLException {
        return signed[index - 1];
    }

    public void setSigned(int index, boolean signed) throws SQLException {
        this.signed[index - 1] = signed;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
        init();
    }

    public Object getParameter(int index) {
        return params[index - 1];
    }

    public Object getParameter(String parameterName) throws SQLException {
        return params[getParameterIndex(parameterName) - 1];
    }

    public void setParameter(int index, Object value) {
        this.params[index - 1] = value;
    }

    public String getParameterName(int index) throws SQLException {
        return parameterName[index - 1];
    }

    public void setParameterName(int index, String parameterName) throws SQLException {
        if (parameterName == null || parameterName.trim().length() == 0) {
            throw new SQLException("Parameter name is missing");
        }
        parameterName = parameterName.trim();
        for (int i = 0; i < parameterCount; i++) {
            String name = this.parameterName[i];
            if (name != null && name.equalsIgnoreCase(parameterName)) {
                throw new SQLException("Duplicate parameter name: " + parameterName);
            }
        }
        this.parameterName[index - 1] = parameterName;
    }

    public int getParameterIndex(String parameterName) throws SQLException {
        if (parameterName == null || parameterName.trim().length() == 0) {
            return -1;
        }
        for (int i = 0; i < parameterCount; i++) {
            String name = this.parameterName[i];
            if (name != null && name.equalsIgnoreCase(parameterName)) {
                return i + 1;
            }
        }
        return -1;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return (iface != null && iface.isAssignableFrom(org.julp.db.Parameters.class));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(java.lang.Class<T> iface) {
        if (isWrapperFor(iface)) {
            return (T) this;
        }
        throw new IllegalArgumentException("Type: " + iface.getCanonicalName() + ", must be assignable from " + org.julp.db.Parameters.class);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterCount; i++) {
            sb.append("\nparam").append(i + 1).append(": ");
            sb.append("parameterName=").append(parameterName[i]);
            sb.append("&value=").append(params[i]);
            sb.append("&parameterMode=").append(parameterMode[i]);
            sb.append("&parameterType=").append(parameterType[i]);
            sb.append("&parameterTypeName=").append(parameterTypeName[i]);
            sb.append("&precision=").append(precision[i]);
            sb.append("&scale=").append(scale[i]);
            sb.append("&nullable=").append(nullable[i]);
            sb.append("&signed=").append(signed[i]);
        }
        return sb.toString();
    }
}
