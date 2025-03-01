package com.anthunt.terraform.generator.aws.service.iam;

import com.anthunt.terraform.generator.aws.command.CommonArgs;
import com.anthunt.terraform.generator.aws.command.ExtraArgs;
import com.anthunt.terraform.generator.aws.service.AbstractExport;
import com.anthunt.terraform.generator.aws.service.iam.model.AWSRolePolicyAttachment;
import com.anthunt.terraform.generator.core.model.terraform.elements.TFExpression;
import com.anthunt.terraform.generator.core.model.terraform.imports.TFImport;
import com.anthunt.terraform.generator.core.model.terraform.imports.TFImportLine;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Maps;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListRolesResponse;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportIamRolePolicyAttachment extends AbstractExport<IamClient> {

    @Override
    protected Maps<Resource> export(IamClient client, CommonArgs commonArgs, ExtraArgs extraArgs) {
        List<AWSRolePolicyAttachment> awsRolePolicyAttachments = listAwsRolePolicyAttachments(client);
        return getResourceMaps(awsRolePolicyAttachments);
    }

    @Override
    protected TFImport scriptImport(IamClient client, CommonArgs commonArgs, ExtraArgs extraArgs) {
        List<AWSRolePolicyAttachment> awsRolePolicyAttachments = listAwsRolePolicyAttachments(client);
        return getTFImport(awsRolePolicyAttachments);
    }

    List<AWSRolePolicyAttachment> listAwsRolePolicyAttachments(IamClient client) {
        ListRolesResponse listPoliciesResponse = client.listRoles();
        return listPoliciesResponse.roles().stream()
                .filter(role -> !role.arn().startsWith("arn:aws:iam::aws:role/"))
                .peek(role -> log.debug("roleName => {}", role.roleName()))
                .flatMap(
                        role -> client.listAttachedRolePolicies(ListAttachedRolePoliciesRequest.builder()
                                        .roleName(role.roleName())
                                        .build())
                                .attachedPolicies()
                                .stream()
                                .map(attachedPolicy -> AWSRolePolicyAttachment.builder()
                                        .roleName(role.roleName())
                                        .policyName(attachedPolicy.policyName())
                                        .build()
                                )
                                .peek(rolePolicyAttachment -> log.debug("rolePolicyAttachment => {}", rolePolicyAttachment))
                )
                .collect(Collectors.toList());
    }

    Maps<Resource> getResourceMaps(List<AWSRolePolicyAttachment> awsRolePolicyAttachments) {
        Maps.MapsBuilder<Resource> resourceMapsBuilder = Maps.builder();
        for (AWSRolePolicyAttachment awsRolePolicyAttachment : awsRolePolicyAttachments) {
            resourceMapsBuilder.map(
                    Resource.builder()
                            .api("aws_iam_role_policy_attachment")
                            .name(getResourceName(awsRolePolicyAttachment.getRoleName(), awsRolePolicyAttachment.getPolicyName()))
                            .argument("role", TFExpression.build(
                                    MessageFormat.format("aws_iam_role.{0}.name", awsRolePolicyAttachment.getRoleName())))
                            .argument("policy_arn", TFExpression.build(
                                    MessageFormat.format("aws_iam_policy.{0}.arn", awsRolePolicyAttachment.getPolicyName())))
                            .build()
            );
        }
        return resourceMapsBuilder.build();
    }

    private String getResourceName(String roleName, String policyName) {
        return MessageFormat.format("{0}-attach-{1}", roleName, policyName);
    }

    TFImport getTFImport(List<AWSRolePolicyAttachment> awsRolePolicyAttachments) {
        return TFImport.builder()
                .importLines(awsRolePolicyAttachments.stream()
                        .map(awsRolePolicyAttachment -> TFImportLine.builder()
                                .address(MessageFormat.format("{0}.{1}",
                                        "aws_iam_role_policy_attachment",
                                        getResourceName(awsRolePolicyAttachment.getRoleName(),
                                                awsRolePolicyAttachment.getPolicyName())
                                ))
                                .id(MessageFormat.format("{0}/{1}",
                                        awsRolePolicyAttachment.getRoleName(),
                                        awsRolePolicyAttachment.getPolicyName()))
                                .build()
                        ).collect(Collectors.toList()))
                .build();
    }
}
