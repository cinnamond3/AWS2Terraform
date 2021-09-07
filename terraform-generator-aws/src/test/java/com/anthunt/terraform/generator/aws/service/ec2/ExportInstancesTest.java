package com.anthunt.terraform.generator.aws.service.ec2;

import com.anthunt.terraform.generator.aws.client.AmazonClients;
import com.anthunt.terraform.generator.aws.service.ec2.dto.ReservationDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;

@Slf4j
@SpringBootTest(classes = {AmazonClients.class},
        properties = { "logging.level.com.anthunt.terraform.generator.aws=DEBUG" }
)
class ExportInstancesTest {

    private static ExportInstances exportInstances;

    @BeforeAll
    public static void beforeAll() {
        exportInstances = new ExportInstances();
    }

    @Test
    public void describeInstancesResponse() {
        AmazonClients amazonClients = AmazonClients.builder().profileName("default").region(Region.AP_NORTHEAST_2).build();
        Ec2Client ec2Client = amazonClients.getEc2Client();
        DescribeInstancesResponse describeInstancesResponse = ec2Client.describeInstances();
        log.debug("describeInstancesResponse => " + describeInstancesResponse);
    }

    @Test
    public void getDescribeInstancesResponse() {
        AmazonClients amazonClients = AmazonClients.builder().profileName("default").region(Region.AP_NORTHEAST_2).build();
        Ec2Client ec2Client = amazonClients.getEc2Client();

        List<ReservationDto> reservations = exportInstances.getReservations(ec2Client);
        log.debug("reservations => " + reservations);
    }

}