package com.anthunt.terraform.generator.aws.service.iam;

import com.anthunt.terraform.generator.aws.client.AmazonClients;
import com.anthunt.terraform.generator.aws.support.DisabledOnNoAwsCredentials;
import com.anthunt.terraform.generator.aws.support.TestDataFileUtils;
import com.anthunt.terraform.generator.aws.utils.JsonUtils;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Maps;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.Role;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = {AmazonClients.class})
class ExportIamRolesTest {

    private static ExportIamRoles exportIamRoles;

    @Autowired
    private ResourceLoader resourceLoader;

    private static IamClient client;

    @BeforeAll
    public static void beforeAll() {
        exportIamRoles = new ExportIamRoles();
        AmazonClients amazonClients = AmazonClients.builder().profileName("default").region(Region.AWS_GLOBAL).build();
        client = amazonClients.getIamClient();
    }

    private List<Role> getRoleList() {
        return List.of(
                Role.builder()
                        .path("/")
                        .roleName("testRole")
                        .description("test description")
                        .arn("arn:aws:iam::100000000000:role/AmazonEKS_EFS_CSI_DriverRole")
                        .assumeRolePolicyDocument(URLEncoder.encode(TestDataFileUtils.asString(
                                        resourceLoader.getResource("testData/aws/input/IamRoleAssumeRolePolicyDocument.json")),
                                StandardCharsets.UTF_8))
                        .build()

        );
    }

    @Test
    @DisabledOnNoAwsCredentials
    public void getRoles() {

        List<Role> roles = exportIamRoles.listRoles(client);

        roles.stream()
                .map(role ->
                        Role.builder()
                                .arn(role.arn())
                                .arn(role.roleName())
                                .arn(role.description())
                                .arn(role.path())
                                .assumeRolePolicyDocument(JsonUtils.toPrettyFormat(
                                        URLDecoder.decode(role.assumeRolePolicyDocument(), StandardCharsets.UTF_8)))
                                .build())
                .forEach(role -> log.debug("role => {}", role.toString()));
    }

    @Test
    public void getResourceMaps() {
        List<Role> roles = getRoleList();
        Maps<Resource> resourceMaps = exportIamRoles.getResourceMaps(roles);
        String actual = resourceMaps.unmarshall();

        log.debug("actual => \n{}", actual);
        String expected = TestDataFileUtils.asString(
                resourceLoader.getResource("testData/aws/expected/IamRole.tf")
        );
        assertEquals(expected, actual);
    }

    @Test
    public void getTFImport() {
        String expected = TestDataFileUtils.asString(resourceLoader.getResource("testData/aws/expected/IamRole.cmd"));
        String actual = exportIamRoles.getTFImport(getRoleList()).script();

        assertEquals(expected, actual);
    }
}