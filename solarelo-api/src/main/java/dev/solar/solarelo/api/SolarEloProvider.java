package dev.solar.solarelo.api;

public final class SolarEloProvider {

    private static SolarEloAPI api = null;

    private SolarEloProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static SolarEloAPI getAPI() {
        return api;
    }

    public static void register(SolarEloAPI instance) {
        api = instance;
    }

    public static void unregister() {
        api = null;
    }
}
