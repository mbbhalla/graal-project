package graal.project;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import graal.project.handler.Handler;
import lombok.val;

public class Bootstrap {
    
    private void log(String statement) {
        System.out.println(">>> " + statement);
        System.err.println(">>> " + statement);
    }
    
    private void execute(
        final String[] args) {
            
        val runtimeAPIHostPort = System.getenv("AWS_LAMBDA_RUNTIME_API");
        val endPointGet = "http://" + runtimeAPIHostPort + "/2018-06-01/runtime/invocation/next";
        val templateEndPointPostOk = "http://" + runtimeAPIHostPort + "/2018-06-01/runtime/invocation/{AwsRequestId}/response";
        val templateEndPointPostError = "http://" + runtimeAPIHostPort + "/2018-06-01/runtime/invocation/{AwsRequestId}/error";
        val endPointPostInitError = "http://" + runtimeAPIHostPort + "/2018-06-01/runtime/init/error";
        val lambdaARN = System.getenv("Lambda-Runtime-Invoked-Function-Arn");
        val handlerClassName = System.getenv("_HANDLER").split("\\.")[0];
        val handlerMethodName = System.getenv("_HANDLER").split("\\.")[1];
        
        
        this.log(runtimeAPIHostPort);
        
        Handler handler = null;
        try {
            handler = new Handler();
        } catch(Exception e) {
            try {
                Unirest.post(endPointPostInitError)
                    .header("Lambda-Runtime-Function-Error-Type", "Unhandled")
                    .body("Init failure")
                    .asJson();
            } catch(UnirestException e1) {
            }
        }
        
        while(true) {
            try {
                final HttpResponse<JsonNode> getResponse = Unirest.get(endPointGet).asJson();
                final String requestId = getResponse.getHeaders().getFirst("Lambda-Runtime-Aws-Request-Id");
                final String endPointPostOk = templateEndPointPostOk.replace("{AwsRequestId}", requestId);
                final String endPointPostError = templateEndPointPostError.replace("{AwsRequestId}", requestId);
                
                //make call to handler
                val response = handler.handleRequest(getResponse.toString(), null);
                
                final HttpResponse<JsonNode> postResponse = Unirest.post(endPointPostOk).body(response).asJson();
                
            } catch(final UnirestException e) {
            }
        }
    }
    
    public static void main(String[] args) {
    
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.execute(args);
        
    }
}
