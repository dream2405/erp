package kr.ac.dankook.ace.approval_processing_service.config;

import kr.ac.dankook.ace.erp.proto.ApprovalGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

    @Bean
    public ApprovalGrpc.ApprovalBlockingStub approvalStub(GrpcChannelFactory channels) {
        return ApprovalGrpc.newBlockingStub(channels.createChannel("default"));
    }
}
