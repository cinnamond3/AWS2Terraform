package com.anthunt.terraform.generator.aws.command;

import com.anthunt.terraform.generator.aws.client.AmazonClients;
import com.anthunt.terraform.generator.aws.service.ec2.ExportInstances;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsResponse;

import javax.validation.Valid;

@Slf4j
@ShellComponent
public class Ec2Commands extends AbstractCommands {

    @Autowired
    private ExportInstances exportInstances;

    @ShellMethod("Export terraform resources of ec2 instances.")
    public void exportEc2Instances(@ShellOption(optOut = true) @Valid CommonArgs commonArgs) {
        exportInstances.exportTerraform(Ec2Client.class, commonArgs);
    }

}
