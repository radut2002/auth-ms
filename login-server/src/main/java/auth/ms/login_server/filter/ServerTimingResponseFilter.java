package auth.ms.login_server.filter;

import javax.ws.rs.ext.Provider;

import auth.ms.server_timings.filter.AbstractServerTimingResponseFilter;

@Provider
public class ServerTimingResponseFilter extends AbstractServerTimingResponseFilter {

    private static final String MICROSERVICE_TIMING_ID = "l";
    private static final String SERVER_TIMING_KEY = MICROSERVICE_TIMING_ID + ";dur=";

    @Override
    protected String serverTimingKey() {
        return SERVER_TIMING_KEY;
    }
}
