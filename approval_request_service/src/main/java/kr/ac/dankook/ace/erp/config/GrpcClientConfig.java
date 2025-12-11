package kr.ac.dankook.ace.erp.config;

import kr.ac.dankook.ace.erp.proto.ApprovalGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ApprovalGrpc.ApprovalBlockingStub approvalClient(GrpcChannelFactory channels) {
        return ApprovalGrpc.newBlockingStub(channels.createChannel("default"));
    }
}
