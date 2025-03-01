package com.anthunt.terraform.generator.aws.service.ecr;

import com.anthunt.terraform.generator.aws.client.AmazonClients;
import com.anthunt.terraform.generator.aws.support.DisabledOnNoAwsCredentials;
import com.anthunt.terraform.generator.aws.support.TestDataFileUtils;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Maps;
import com.anthunt.terraform.generator.core.model.terraform.nodes.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.EncryptionConfiguration;
import software.amazon.awssdk.services.ecr.model.ImageScanningConfiguration;
import software.amazon.awssdk.services.ecr.model.Repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = {AmazonClients.class})
class ExportEcrRepositoryTest {

    @Autowired
    private ResourceLoader resourceLoader;

    private static ExportEcrRepository exportEcrRepository;

    private static EcrClient client;

    @BeforeAll
    public static void beforeAll() {
        exportEcrRepository = new ExportEcrRepository();
        AmazonClients amazonClients = AmazonClients.builder().profileName("default").region(Region.AP_NORTHEAST_2).build();
        client = amazonClients.getEcrClient();
    }

    private List<Repository> getRepositories() {
        List<Repository> repositories = List.of(
                Repository.builder()
                        .repositoryName("envoyproxy/envoy")
                        .encryptionConfiguration(EncryptionConfiguration.builder()
                                .encryptionType("AES256")
                                .kmsKey(null)
                                .build())
                        .imageTagMutability("MUTABLE")
                        .imageScanningConfiguration(ImageScanningConfiguration.builder()
                                .scanOnPush(false)
                                .build())
                        .build(),
                Repository.builder()
                        .repositoryName("openjdk")
                        .encryptionConfiguration(EncryptionConfiguration.builder()
                                .encryptionType("AES256")
                                .kmsKey(null)
                                .build())
                        .imageTagMutability("MUTABLE")
                        .imageScanningConfiguration(ImageScanningConfiguration.builder()
                                .scanOnPush(false)
                                .build())
                        .build()
        );
        return repositories;
    }

    @Test
    @DisabledOnNoAwsCredentials
    public void export() {
        Maps<Resource> export = exportEcrRepository.export(client, null, null);
        log.debug("export => \n{}", export.unmarshall());
    }

    @Test
    public void getResourceMaps() {
        List<Repository> repositories = getRepositories();

        Maps<Resource> resourceMaps = exportEcrRepository.getResourceMaps(repositories);
        String actual = resourceMaps.unmarshall();

        log.debug("actual => \n{}", actual);
        String expected = TestDataFileUtils.asString(
                resourceLoader.getResource("testData/aws/expected/EcrRepository.tf")
        );
        assertEquals(expected, actual);
    }

    @Test
    public void getTFImport() {
        String expected = TestDataFileUtils.asString(resourceLoader.getResource("testData/aws/expected/EcrRepository.cmd"));
        String actual = exportEcrRepository.getTFImport(getRepositories()).script();

        assertEquals(expected, actual);
    }
}