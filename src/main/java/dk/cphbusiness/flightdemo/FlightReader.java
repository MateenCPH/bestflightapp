package dk.cphbusiness.flightdemo;

import dk.cphbusiness.utils.Utils;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        FlightReader flightReader = new FlightReader();
        try {
            List<DTOs.FlightDTO> flightList = flightReader.getFlightsFromFile("flights.json");
            List<DTOs.FlightInfo> flightInfoList = flightReader.getFlightInfoDetails(flightList);
            flightInfoList.forEach(f -> {
                System.out.println("\n" + f);
            });

            //Calculating total flight time for a certain airline.
            long totalFightTime = flightInfoList.stream()
                    .filter(flightInfo -> "Lufthansa".equals(flightInfo.getAirline()))
                    .mapToLong(flightInfo -> flightInfo.getDuration().toHours())
                    .sum();
            System.out.printf("The total flight time for Lufthansa : %d hours%n", totalFightTime);

            //List of flights operated between two specific airports
            String airport1 = "Finke";
            String airport2 = "Alice Springs";
            List<DTOs.FlightInfo> flightsBetweenAirports = flightInfoList.stream()
                    .filter(flightInfo -> airport1.equals(flightInfo.getOrigin()) || airport1.equals(flightInfo.getDestination()) ||
                            airport2.equals(flightInfo.getOrigin()) || airport2.equals(flightInfo.getDestination()))
                    .collect(Collectors.toList());
            flightsBetweenAirports.forEach(System.out::println);

            //All flights departing before 08:00
            flightInfoList.stream()
                    .filter(flightInfo -> flightInfo.getDeparture().toLocalTime().isBefore(LocalTime.of(8, 0)))
                    .forEach(System.out::println);

            List<DTOs.FlightInfo> luftHansaInfo = filterByAirline(flightInfoList, "Lufthansa");
            luftHansaInfo.forEach(System.out::println);
            System.out.println(findAvgDuration(luftHansaInfo));

            Map<String, Double> avgDurationPerAirline = getAvgDurationPerAirline(flightInfoList);
            avgDurationPerAirline.forEach((airline, avgDuration) ->
                    System.out.println(airline + ": " + avgDuration + " minutes"));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


//    public List<FlightDTO> jsonFromFile(String fileName) throws IOException {
//        List<FlightDTO> flights = getObjectMapper().readValue(Paths.get(fileName).toFile(), List.class);
//        return flights;
//    }


    public List<DTOs.FlightInfo> getFlightInfoDetails(List<DTOs.FlightDTO> flightList) {
        List<DTOs.FlightInfo> flightInfoList = flightList.stream().map(flight -> {
            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
            DTOs.FlightInfo flightInfo = DTOs.FlightInfo.builder()
                    .name(flight.getFlight().getNumber())
                    .iata(flight.getFlight().getIata())
                    .airline(flight.getAirline().getName())
                    .duration(duration)
                    .departure(flight.getDeparture().getScheduled().toLocalDateTime())
                    .arrival(flight.getArrival().getScheduled().toLocalDateTime())
                    .origin(flight.getDeparture().getAirport())
                    .destination(flight.getArrival().getAirport())
                    .build();

            return flightInfo;
        }).toList();
        return flightInfoList;
    }

    public List<DTOs.FlightDTO> getFlightsFromFile(String filename) throws IOException {
        DTOs.FlightDTO[] flights = new Utils().getObjectMapper().readValue(Paths.get(filename).toFile(), DTOs.FlightDTO[].class);

        List<DTOs.FlightDTO> flightList = Arrays.stream(flights).toList();
        return flightList;
    }

    public static List<DTOs.FlightInfo> filterByAirline(List<DTOs.FlightInfo> flightInfos, String airline) {
        return flightInfos.stream()
                .filter(flightInfo -> airline.equals(flightInfo.getAirline()))
                .collect(Collectors.toList());
    }

    public static Map<String, Double> getAvgDurationPerAirline(List<DTOs.FlightInfo> flightInfos) {
        return flightInfos.stream()
                .collect(Collectors.groupingBy(
                        flightInfo -> flightInfo.getAirline() != null ? flightInfo.getAirline() : "Unknown Airline",  // Provide default value
                        Collectors.averagingDouble(flightInfo -> flightInfo.getDuration().toMinutes())
                ));
    }


    public static double findAvgDuration(List<DTOs.FlightInfo> flightInfos) {
        return flightInfos.stream()
                .mapToLong(flightInfo -> flightInfo.getDuration().toMinutes())
                .average().orElse(0.0);
    }


}