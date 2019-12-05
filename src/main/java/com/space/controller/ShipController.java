package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {

    @Autowired
    private ShipService shipService;

    @GetMapping()
    public List<ShipInfo> getAll(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String planet,
                                 @RequestParam(required = false) ShipType shipType,
                                 @RequestParam(required = false) Long after,
                                 @RequestParam(required = false) Long before,
                                 @RequestParam(required = false) Boolean isUsed,
                                 @RequestParam(required = false) Double minSpeed,
                                 @RequestParam(required = false) Double maxSpeed,
                                 @RequestParam(required = false) Integer minCrewSize,
                                 @RequestParam(required = false) Integer maxCrewSize,
                                 @RequestParam(required = false) Double minRating,
                                 @RequestParam(required = false) Double maxRating,
                                 @RequestParam(required = false) ShipOrder order,
                                 @RequestParam(required = false) Integer pageNumber,
                                 @RequestParam(required = false) Integer pageSize) {
        order = isNull(order) ? ShipOrder.ID : order;
        pageNumber = isNull(pageNumber) ? 0 : pageNumber;
        pageSize = isNull(pageSize) ? 3 : pageSize;

        List<Ship> ships = shipService.getAll(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating, order.getFieldName(), pageNumber, pageSize);
        return ships.stream().map(ShipController::toShipInfo).collect(Collectors.toList());
    }


    @GetMapping("/count")
    public Integer getAllCount(@RequestParam(required = false) String name,
                               @RequestParam(required = false) String planet,
                               @RequestParam(required = false) ShipType shipType,
                               @RequestParam(required = false) Long after,
                               @RequestParam(required = false) Long before,
                               @RequestParam(required = false) Boolean isUsed,
                               @RequestParam(required = false) Double minSpeed,
                               @RequestParam(required = false) Double maxSpeed,
                               @RequestParam(required = false) Integer minCrewSize,
                               @RequestParam(required = false) Integer maxCrewSize,
                               @RequestParam(required = false) Double minRating,
                               @RequestParam(required = false) Double maxRating) {

        return shipService.getAllCount(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating);
    }

    @PostMapping
    public ResponseEntity<ShipInfo> createShip(@RequestBody ShipInfo info) {
        if (StringUtils.isEmpty(info.name) || info.name.length() > 50) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (StringUtils.isEmpty(info.planet) || info.planet.length() > 50) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (isNull(info.shipType)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (isNull(info.prodDate) || info.prodDate < 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (isNull(info.speed) || info.speed < 0.01 || info.speed > 0.99) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (isNull(info.crewSize) || info.crewSize < 1 || info.crewSize > 9999) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        LocalDate localDate = new Date(info.prodDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        if (year < 2800 || year > 3019) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        boolean isUsed = isNull(info.isUsed) ? false : info.isUsed;

        Ship ship = shipService.createShip(info.name, info.planet, info.shipType, info.prodDate, isUsed,
                info.speed, info.crewSize);
        return ResponseEntity.status(HttpStatus.OK).body(toShipInfo(ship));
    }

    @GetMapping("/{ID}")
    public ResponseEntity<ShipInfo> getShip(@PathVariable("ID") long id) {
        if (id <= 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        Ship ship = shipService.getShip(id);
        if (isNull(ship)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(toShipInfo(ship));
        }
    }

    @PostMapping("/{ID}")
    public ResponseEntity<ShipInfo> updateShip(@PathVariable("ID") long id,
                                               @RequestBody ShipInfo info) {
        if (id <= 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (nonNull(info.name) && (info.name.length() > 50 || info.name.isEmpty())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (nonNull(info.planet) && (info.planet.length() > 50 || info.planet.isEmpty())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (nonNull(info.crewSize) && (info.crewSize < 1 || info.crewSize > 9999)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (nonNull(info.prodDate) && info.prodDate < 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        if (nonNull(info.speed) && (info.speed < 0.01 || info.speed > 0.99)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        if (nonNull(info.prodDate)) {
            LocalDate localDate = new Date(info.prodDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int year = localDate.getYear();
            if (year < 2800 || year > 3019) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Ship ship = shipService.updateShip(id, info.name, info.planet, info.shipType, info.prodDate, info.isUsed,
                info.speed, info.crewSize);
        if (isNull(ship)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(toShipInfo(ship));
        }
    }

    @DeleteMapping("/{ID}")
    public ResponseEntity delete(@PathVariable("ID") long id) {
        if (id <= 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        Ship ship = shipService.delete(id);
        if (isNull(ship)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    private static ShipInfo toShipInfo(Ship ship) {
        if (isNull(ship)) return null;

        ShipInfo result = new ShipInfo();
        result.id = ship.getId();
        result.name = ship.getName();
        result.planet = ship.getPlanet();
        result.shipType = ship.getShipType();
        result.prodDate = ship.getProdDate().getTime();
        result.isUsed = ship.getIsUsed();
        result.speed = ship.getSpeed();
        result.crewSize = ship.getCrewSize();
        result.rating = ship.getRating();
        return result;
    }
}