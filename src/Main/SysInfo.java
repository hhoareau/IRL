package Main;

/**
 * Created by u016272 on 07/04/2016.
 */
public class SysInfo {
    public Long callsByMin=0L;

    public SysInfo(Long callsByMin) {
        this.callsByMin = callsByMin;
    }

    public Long getCallsByMin() {
        return callsByMin;
    }

    public void setCallsByMin(Long callsByMin) {
        this.callsByMin = callsByMin;
    }
}
