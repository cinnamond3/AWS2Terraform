package com.anthunt.terraform.generator.aws.service.efs.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import software.amazon.awssdk.services.efs.model.FileSystemDescription;
import software.amazon.awssdk.services.efs.model.MountTargetDescription;

import java.util.List;

@Data
@Builder
@ToString
public class EfsDto {
    private FileSystemDescription fileSystemDescription;
    private String backupPolicyStatus;
    private String fileSystemPolicy;
    private List<MountTargetDescription> mountTargets;

}
