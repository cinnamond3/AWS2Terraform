package com.anthunt.terraform.generator.aws.service.vpc;

import com.anthunt.terraform.generator.aws.command.CommonArgs;
import com.anthunt.terraform.generator.aws.command.ExtraArgs;
import com.anthunt.terraform.generator.aws.service.AbstractExport;
import com.anthunt.terraform.generator.core.model.terraform.elements.TFMap;
import com.anthunt.terraform.generator.core.model.terraform.elements.TFString;
import com.anthunt.terraform.generator.core.model.terraform.imports.TFImport;
import com.anthunt.terraform.generator.core.model.terraform.imports.TFImportLine;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Maps;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInternetGatewaysResponse;
import software.amazon.awssdk.services.ec2.model.InternetGateway;
import software.amazon.awssdk.services.ec2.model.InternetGatewayAttachment;
import software.amazon.awssdk.services.ec2.model.Tag;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportInternetGateways extends AbstractExport<Ec2Client> {

    @Override
    protected Maps<Resource> export(Ec2Client client, CommonArgs commonArgs, ExtraArgs extraArgs) {
        List<InternetGateway> internetGateways = listInternetGateways(client);
        return getResourceMaps(internetGateways);
    }

    @Override
    protected TFImport scriptImport(Ec2Client client, CommonArgs commonArgs, ExtraArgs extraArgs) {
        List<InternetGateway> internetGateways = listInternetGateways(client);
        return getTFImport(internetGateways);
    }

    protected List<InternetGateway> listInternetGateways(Ec2Client client) {
        DescribeInternetGatewaysResponse describeInternetGatewaysResponse = client.describeInternetGateways();
        return describeInternetGatewaysResponse.internetGateways();
    }

    protected Maps<Resource> getResourceMaps(List<InternetGateway> internetGateways) {
        Maps.MapsBuilder<Resource> resourceMapsBuilder = Maps.builder();
        for (InternetGateway internetGateway : internetGateways) {

            List<InternetGatewayAttachment> internetGatewayAttachments = internetGateway.attachments();

            for (InternetGatewayAttachment internetGatewayAttachment : internetGatewayAttachments) {
                resourceMapsBuilder.map(
                        Resource.builder()
                                .api("aws_internet_gateway")
                                .name(internetGateway.internetGatewayId())
                                .argument("vpc_id", TFString.build(internetGatewayAttachment.vpcId()))
                                .argument("tags", TFMap.build(
                                        internetGateway.tags().stream()
                                                .collect(Collectors.toMap(Tag::key, tag -> TFString.build(tag.value())))
                                ))
                                .build()
                );
            }
        }
        return resourceMapsBuilder.build();
    }

    TFImport getTFImport(List<InternetGateway> internetGateways) {
        return TFImport.builder()
                .importLines(internetGateways.stream()
                        .map(internetGateway -> TFImportLine.builder()
                                .address(MessageFormat.format("{0}.{1}",
                                        "aws_internet_gateway",
                                        internetGateway.internetGatewayId()))
                                .id(internetGateway.internetGatewayId())
                                .build()
                        ).collect(Collectors.toList()))
                .build();
    }
}
