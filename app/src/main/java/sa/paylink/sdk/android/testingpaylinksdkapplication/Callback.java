package sa.paylink.sdk.android.testingpaylinksdkapplication;

public interface Callback<T, E> {
    void onSuccess(T response);

    void onError(E error);
}