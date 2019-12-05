package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class ShipService {
    @Autowired
    private ShipRepository shipRepository;

    public List<Ship> getAll(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed,
                             Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                             Double minRating, Double maxRating, String order, int pageNumber, int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order));
        Date afterDate = isNull(after) ? null : new Date(after);
        Date beforeDate = isNull(before) ? null : new Date(before);
        return shipRepository.getAll(name, planet, shipType, afterDate, beforeDate, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating, pageable);
    }

    public Integer getAllCount(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed,
                               Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                               Double minRating, Double maxRating) {
        Date afterDate = isNull(after) ? null : new Date(after);
        Date beforeDate = isNull(before) ? null : new Date(before);

        return shipRepository.getAllCount(name, planet, shipType, afterDate, beforeDate, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating);
    }

    public Ship createShip(String name, String planet, ShipType shipType, long prodDate, boolean isUsed,
                           double speed, int crewSize) {
        double rating = getRating(prodDate, isUsed, speed);

        Ship ship = new Ship();
        ship.setName(name);
        ship.setPlanet(planet);
        ship.setShipType(shipType);
        ship.setProdDate(new Date(prodDate));
        ship.setIsUsed(isUsed);
        ship.setSpeed(speed);
        ship.setCrewSize(crewSize);
        ship.setRating(rating);

        return shipRepository.save(ship);
    }

    public Ship getShip(long id) {
        return shipRepository.findById(id).orElse(null);
    }

    public Ship updateShip(long id, String name, String planet, ShipType shipType, Long prodDate, Boolean isUsed,
                           Double speed, Integer crewSize) {

        Ship ship = shipRepository.findById(id).orElse(null);
        if (isNull(ship)) return null;

        boolean needUpdate = false;

        if (!StringUtils.isEmpty(name) && name.length() <= 50) {
            ship.setName(name);
            needUpdate = true;
        }
        if (!StringUtils.isEmpty(planet) && planet.length() <= 50) {
            ship.setPlanet(planet);
            needUpdate = true;
        }
        if (nonNull(shipType)) {
            ship.setShipType(shipType);
            needUpdate = true;
        }
        if (nonNull(prodDate)) {
            LocalDate localDate = new Date(prodDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int year = localDate.getYear();
            if (year >= 2800 && year <= 3019) {
                ship.setProdDate(new Date(prodDate));
                needUpdate = true;
            }
        }

        if (nonNull(isUsed)) {
            ship.setIsUsed(isUsed);
            needUpdate = true;
        }
        if (nonNull(speed)) {
            ship.setSpeed(speed);
            needUpdate = true;
        }
        if (nonNull(crewSize)) {
            ship.setCrewSize(crewSize);
            needUpdate = true;
        }

        if (needUpdate) {
            ship.setRating(getRating(ship.getProdDate().getTime(), ship.getIsUsed(), ship.getSpeed()));
            shipRepository.save(ship);
        }

        return ship;
    }

    private double getRating(long prodDate, boolean isUsed, double speed) {
        LocalDate localDate = new Date(prodDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        double result = 80 * speed * (isUsed ? 0.5 : 1) / (3019 - year + 1);
        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        return Double.parseDouble(df.format(result));
    }


    public Ship delete(long id) {
        Ship ship = shipRepository.findById(id).orElse(null);
        if (isNull(ship)) return null;

        shipRepository.delete(ship);
        return ship;
    }
}