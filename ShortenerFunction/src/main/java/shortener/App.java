package shortener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Map<String, String> URLS = new HashMap<>() {{
        put("/repo", "https://github.com/rospiel/study-serverless-linux-tips");
        put("/gitpod", "https://gitpod.io/#github.com/rospiel/study-serverless-linux-tips");
    }};
    
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        LambdaLogger logger = context.getLogger();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
            .withHeaders(headers);
        String url = URLS.get(input.getPath());
        
        if (Objects.nonNull(url)) {
            headers.put("Location", url);
            logger.log(String.format("%s => %s %s", input.getPath(), 302, url));
            return response
                .withStatusCode(302)
                .withBody(new String("Redirecting to ".concat(url)));

        } 
        
        return response
                .withStatusCode(404)
                .withBody("");
    }
}
