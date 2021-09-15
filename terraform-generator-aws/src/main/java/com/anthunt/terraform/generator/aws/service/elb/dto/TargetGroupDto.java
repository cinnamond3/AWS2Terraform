package com.anthunt.terraform.generator.aws.service.elb.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.ToString;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.*;

import java.util.List;

@Data
@Builder
@ToString
public class TargetGroupDto {
    private TargetGroup targetGroup;
    @Singular
    private List<TargetGroupAttribute> targetGroupAttributes;
}
