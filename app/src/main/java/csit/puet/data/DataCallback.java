package csit.puet.data;

public interface DataCallback<T> {
    void onDataLoaded(T data);
    void onError(Throwable throwable);
}
