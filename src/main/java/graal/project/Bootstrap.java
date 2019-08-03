package graal.project;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import graal.project.handler.Handler;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Bootstrap {
    
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        
        val runtimeAPIHostPort = System.getenv("AWS_LAMBDA_RUNTIME_API");
        val endPointGet = "http://" + runtimeAPIHostPort + "/2018-06-01/runtime/invocation/next";
        val templateEndPointPostOk = "http://" + runtimeAPIHostPort + "/2018-06-01/runtime/invocation/{AwsRequestId}/response";
        val templateEndPointPostError = "http://" + runtimeAPIHostPort + "/2018-06-01/runtime/invocation/{AwsRequestId}/error";
        val endPointPostInitError = "http://" + runtimeAPIHostPort + "/2018-06-01/runtime/init/error";
        val lambdaARN = System.getenv("Lambda-Runtime-Invoked-Function-Arn");
        
        Handler handler = null;
        try {
            handler = new Handler();
        } catch(Exception e) {
            log.error("Error occured while init handler", e.getMessage());
            try {
                Unirest.post(endPointPostInitError)
                    .header("Lambda-Runtime-Function-Error-Type", "Unhandled")
                    .body("Init failure")
                    .asJson();
            } catch(UnirestException e1) {
                log.error("Error occured while posting init failure", e1.getMessage());
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
                
                log.info("Function call Ok", lambdaARN);
            } catch(final UnirestException e) {
                log.error("Error occured in processing loop", e.getMessage());
            }
        }
    }
}
