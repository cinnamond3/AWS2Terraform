package com.anthunt.terraform.generator.aws.service.ec2.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import software.amazon.awssdk.services.ec2.model.Instance;

@Data
@ToString
@Builder
public class AWSInstance {
    private Instance instance;
    private Boolean disableApiTermination;
    private String shutdownBehavior;
    private String userData;
}
