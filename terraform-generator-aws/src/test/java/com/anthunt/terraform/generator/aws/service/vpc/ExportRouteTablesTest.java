package com.anthunt.terraform.generator.aws.service.vpc;

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
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = {AmazonClients.class})
class ExportRouteTablesTest {

    private static ExportRouteTables exportRouteTables;

    @Autowired
    private ResourceLoader resourceLoader;

    private static Ec2Client client;

    @BeforeAll
    public static void beforeAll() {
        exportRouteTables = new ExportRouteTables();
        AmazonClients amazonClients = AmazonClients.builder().profileName("default").region(Region.AP_NORTHEAST_2).build();
        client = amazonClients.getEc2Client();
    }

    private List<RouteTable> getRouteTables() {
        //noinspection unchecked
        return List.of(
                RouteTable.builder()
                        .vpcId("vpc-7931b212")
                        .routeTableId("rtb-d6b5fdbd")
                        .routes(Route.builder()
                                        .destinationCidrBlock("172.31.0.0/16")
                                        .gatewayId("local")
                                        .build(),
                                Route.builder()
                                        .destinationCidrBlock("0.0.0.0/0")
                                        .gatewayId("igw-8ecdbbe6")
                                        .build())
                        .associations(builder -> builder.gatewayId("igw-8ecdbbe6").routeTableId("rtb-d6b5fdbd"))
                        .build(),
                RouteTable.builder()
                        .vpcId("vpc-8931b212")
                        .routeTableId("rtb-e6b5fdbd")
                        .routes(Route.builder()
                                        .destinationCidrBlock("172.31.0.0/16")
                                        .gatewayId("local")
                                        .build(),
                                Route.builder()
                                        .destinationCidrBlock("0.0.0.0/0")
                                        .gatewayId("igw-8ecdbbe6")
                                        .build())
                        .associations(builder -> builder.subnetId("subnet-02c7511faa4344f83").routeTableId("rtb-e6b5fdbd"))
                        .build()
        );
    }

    @Test
    @DisabledOnNoAwsCredentials
    void export() {
        Maps<Resource> export = exportRouteTables.export(client, null, null);
        log.debug("result => \n{}", export.unmarshall());
    }

    @Test
    void getResourceMaps() {
        // given
        List<RouteTable> routeTables = getRouteTables();

        Maps<Resource> resourceMaps = exportRouteTables.getResourceMaps(routeTables);
        String actual = resourceMaps.unmarshall();
        log.debug("resourceMaps => \n{}", actual);
        String expected = TestDataFileUtils.asString(resourceLoader.getResource("testData/aws/expected/RouteTable.tf"));
        assertEquals(expected, actual);
    }

    @Test
    public void getTFImport() {
        String expected = TestDataFileUtils.asString(resourceLoader.getResource("testData/aws/expected/RouteTable.cmd"));
        String actual = exportRouteTables.getTFImport(getRouteTables()).script();

        assertEquals(expected, actual);
    }
}