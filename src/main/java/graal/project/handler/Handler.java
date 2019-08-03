package graal.project.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<String, String> {

    @Override
    public String handleRequest(
        final String input, 
        final Context context) {

        return "Hello " + input;
    }
}
