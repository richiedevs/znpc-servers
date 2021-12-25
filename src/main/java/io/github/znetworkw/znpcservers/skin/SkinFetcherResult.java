package io.github.znetworkw.znpcservers.skin;

/**
 * Interface used for the {@link SkinFetcher#doReadSkin} method.
 * use this interface for getting the texture values after a skin fetching is finish.
 */
public interface SkinFetcherResult {
    /**
     * Called when a skin is fetched.
     *
     * @param values The skin values.
     * @param throwable The throwable cause.
     */
    void onDone(String[] values, Throwable throwable);
}
