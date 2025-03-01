package com.anthunt.terraform.generator.aws.command;

import com.anthunt.terraform.generator.aws.service.elb.ExportLoadBalancerListeners;
import com.anthunt.terraform.generator.aws.service.elb.ExportLoadBalancerTargetGroups;
import com.anthunt.terraform.generator.aws.service.elb.ExportLoadBalancers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;

import javax.validation.Valid;

@Slf4j
@ShellComponent
public class ElbCommands extends AbstractCommands {

    private ExportLoadBalancers exportLoadBalancers;

    private ExportLoadBalancerTargetGroups exportLoadBalancerTargetGroups;

    private ExportLoadBalancerListeners exportLoadBalancerListeners;

    public ElbCommands(ExportLoadBalancers exportLoadBalancers, ExportLoadBalancerTargetGroups exportLoadBalancerTargetGroups, ExportLoadBalancerListeners exportLoadBalancerListeners) {
        this.exportLoadBalancers = exportLoadBalancers;
        this.exportLoadBalancerTargetGroups = exportLoadBalancerTargetGroups;
        this.exportLoadBalancerListeners = exportLoadBalancerListeners;
    }

    @ShellMethod("Export terraform resources of LoadBalancers.")
    public void loadBalancers(@ShellOption(optOut = true) @Valid CommonArgs commonArgs) {
        exportLoadBalancers.exportTerraform(ElasticLoadBalancingV2Client.class, commonArgs);
    }

    @ShellMethod("Export terraform resources of LoadBalancerTargetGroups.")
    public void loadBalancerTargetGroups(@ShellOption(optOut = true) @Valid CommonArgs commonArgs) {
        exportLoadBalancerTargetGroups.exportTerraform(ElasticLoadBalancingV2Client.class, commonArgs);
    }

    @ShellMethod("Export terraform resources of LoadBalancerListeners.")
    public void loadBalancerListeners(@ShellOption(optOut = true) @Valid CommonArgs commonArgs) {
        exportLoadBalancerListeners.exportTerraform(ElasticLoadBalancingV2Client.class, commonArgs);
    }
}

