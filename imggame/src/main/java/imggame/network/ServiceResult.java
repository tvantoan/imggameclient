package imggame.network;

import imggame.models.User;

public class ServiceResult<T> {
    public final boolean success;
    public String message;
    public T data;

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ServiceResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    @Override
    public String toString() {

        String res = "Is succeed? :" + success;
        if (data instanceof User) {
            res += ((User) data).getId() + ((User) data).getUsername();
        }
        return res;

    }

}