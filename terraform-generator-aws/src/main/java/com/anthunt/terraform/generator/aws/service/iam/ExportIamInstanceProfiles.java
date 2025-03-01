package com.anthunt.terraform.generator.aws.service.iam;

import com.anthunt.terraform.generator.aws.command.CommonArgs;
import com.anthunt.terraform.generator.aws.command.ExtraArgs;
import com.anthunt.terraform.generator.aws.service.AbstractExport;
import com.anthunt.terraform.generator.core.model.terraform.elements.TFExpression;
import com.anthunt.terraform.generator.core.model.terraform.elements.TFList;
import com.anthunt.terraform.generator.core.model.terraform.elements.TFString;
import com.anthunt.terraform.generator.core.model.terraform.imports.TFImport;
import com.anthunt.terraform.generator.core.model.terraform.imports.TFImportLine;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Maps;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.InstanceProfile;
import software.amazon.awssdk.services.iam.model.ListInstanceProfilesResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportIamInstanceProfiles extends AbstractExport<IamClient> {

    @Override
    protected Maps<Resource> export(IamClient client, CommonArgs commonArgs, ExtraArgs extraArgs) {
        List<InstanceProfile> instanceProfiles = listInstanceProfiles(client);
        return getResourceMaps(instanceProfiles);
    }

    @Override
    protected TFImport scriptImport(IamClient client, CommonArgs commonArgs, ExtraArgs extraArgs) {
        List<InstanceProfile> instanceProfiles = listInstanceProfiles(client);
        return getTFImport(instanceProfiles);
    }

    List<InstanceProfile> listInstanceProfiles(IamClient client) {
        ListInstanceProfilesResponse listInstanceProfilesResponse = client.listInstanceProfiles();
        return listInstanceProfilesResponse.instanceProfiles().stream()
                .filter(instanceProfile -> !instanceProfile.arn().startsWith("arn:aws:iam::aws:instance-profile/"))
                .collect(Collectors.toList());
    }

    Maps<Resource> getResourceMaps(List<InstanceProfile> instanceProfiles) {
        Maps.MapsBuilder<Resource> resourceMapsBuilder = Maps.builder();
        for (InstanceProfile instanceProfile : instanceProfiles) {
            resourceMapsBuilder.map(
                            Resource.builder()
                                    .api("aws_iam_instance_profile")
                                    .name(instanceProfile.instanceProfileName())
                                    .argument("name", TFString.build(instanceProfile.instanceProfileName()))
                                    .argument("role", TFList.build(instanceProfile.roles().stream()
                                            .map(role -> TFExpression.builder().isLineIndent(false)
                                                    .expression(MessageFormat.format("aws_iam_role.{0}.name", role.roleName()))
                                                    .build())
                                            .collect(Collectors.toList())))
                                    .build());
        }
        return resourceMapsBuilder.build();
    }

    private String decodeURL(String origin) {
        return URLDecoder.decode(origin, StandardCharsets.UTF_8);
    }

    TFImport getTFImport(List<InstanceProfile> instanceProfiles) {
        return TFImport.builder()
                .importLines(instanceProfiles.stream()
                        .map(instanceProfile -> TFImportLine.builder()
                                .address(MessageFormat.format("{0}.{1}",
                                        "aws_iam_instance_profile",
                                        instanceProfile.instanceProfileName()))
                                .id(instanceProfile.instanceProfileName())
                                .build()
                        ).collect(Collectors.toList()))
                .build();
    }
}
