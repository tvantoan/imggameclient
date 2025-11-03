package imggame.network;

public class ServiceResult {
    public final boolean success;
    public final String message;

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}