package shortener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        logger.log("Inicio");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
            .withHeaders(headers);
        //String url = URLS.get(input.getPath());
String url = getURL(input.getPath());        
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

    private String getURL(String path) {
        try (Connection connection = getConnection()) {
            createTableIfNotExists(connection);
            String url = searchURL(connection, path);
            return url;

        } catch (SQLException error) {
            error.printStackTrace();
        }

        return null;
    }

    private Connection getConnection() throws SQLException {
        String address = getEnv("DB_ADDRESS");
        String name = getEnv("DB_NAME");
        String userName = getEnv("DB_USERNAME");
        String password = getEnv("DB_PASSWORD");
        String url = String.format("jdbc:mysql://%s/%s", address, name);
        DriverManager.setLoginTimeout(60);
        return DriverManager.getConnection(url, userName, password);
    }

    private String getEnv(String value) {
        return System.getenv(value);

    }

    private String searchURL(Connection connection, String path) {
        String search = "select url from urls where path = ?";
        try (PreparedStatement statement = connection.prepareStatement(search)) {
            statement.setString(1, path);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("url");
            }

            return null;

        } catch (SQLException error) {
            error.printStackTrace();
        }
        
        return null;

    }

    private void createTableIfNotExists(Connection connection) {
        String sql = "CREATE TABLE IF NOT EXISTS urls (path varchar(255) primary key, url varchar(1024))";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException error) {
            error.printStackTrace();
            throw new RuntimeException("The table could not be created");
        }   

    }

}
