package elmot.javabrick.ev3;

import java.util.Arrays;

/**
 * @author elmot
 */
public class Response {
    private final int statusCode;
    private final Object[] outParameters;

    public Response(int outParametersCount, int statusCode) {
        this.outParameters = outParametersCount == 0 ? null : new Object[outParametersCount];
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    void setOutParameter(int index, Object value) {
        outParameters[index] = value;
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", outParameters=" + Arrays.toString(outParameters) +
                '}';
    }

    public int getInt(int index) {
        return ((Number) outParameters[index]).intValue();
    }

    public float getFloat(int index) {
        return ((Number) outParameters[index]).floatValue();
    }

    public byte getByte(int index) {
        return (Byte) outParameters[index];
    }
    
    public String getString(int index) {
        return (String) outParameters[index];
    }
}
