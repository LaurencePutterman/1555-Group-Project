drop table Airline cascade constraints;
create table Airline(
	airline_id varchar2(5) not null,
	airline_name varchar2(50),
	airline_abbreviation varchar2(10) not null,
	year_founded int,
	constraint airline_pk primary key (airline_id)
		deferrable,
	constraint airline_founded check (year_founded > 1900)
);

drop table Plane cascade constraints;
create table Plane(
	plane_type char(4) not null,
	manufacture varchar2(10),
	plane_capacity int,
	last_service date,
	year int,
	owner_id varchar2(5),
	constraint plane_pk primary key (plane_type,owner_id)
		deferrable,
	constraint plane_owner_fk foreign key (owner_id) references Airline(airline_id)
		deferrable,
	constraint plane_capacity_limit check (plane_capacity > 0)
);

drop table Flight cascade constraints;
create table Flight(
	flight_number varchar2(3) not null,
	airline_id varchar2(5),
	plane_type char(4),
	departure_city varchar2(3),
	arrival_city varchar2(3),
	departure_time varchar2(4),
	arrival_time varchar2(4),
	weekly_schedule varchar2(7),
	constraint flight_pk primary key (flight_number)
		deferrable,
	constraint flight_plane_type_fk foreign key (plane_type,airline_id) references Plane(plane_type,owner_id)
		deferrable,
	constraint flight_airline_fk foreign key (airline_id) references Airline(airline_id)
		deferrable,
	constraint flight_departure_city_check check (length(departure_city) = 3),
	constraint flight_arrival_city_check check (length(arrival_city) = 3)
);

drop table price cascade constraints;
create table Price(
	departure_city varchar2(3) not null,
	arrival_city varchar2(3) not null,
	airline_id varchar2(5),
	high_price int,
	low_price int,
	constraint price_pk primary key (departure_city, arrival_city)
		deferrable,
	constraint price_airline_fk foreign key (airline_id) references Airline(airline_id)
		deferrable,
	constraint price_high_price_limit check (high_price >= 0),
	constraint price_low_price_limit check (low_price >= 0),
	constraint price_high_low check (high_price > low_price)
		initially deferred deferrable
);

drop table customer cascade constraints;
create table customer(
	cid varchar2(9) not null,
	salutation varchar2(3),
	first_name varchar2(30),
	last_name varchar2(30),
	credit_car_num varchar2(16),
	credit_card_expire date,
	street varchar2(30),
	city varchar2(30),
	state varchar2(2),
	phone varchar2(10),
	email varchar2(30),
	frequent_miles varchar2(5),
	constraint customer_pk primary key (cid)
		deferrable,
	constraint customer_salutations check (salutation in ('Mr','Mrs','Ms')),
	constraint customer_frequent_miles_fk foreign key (frequent_miles) references Airline(airline_id)
		deferrable
);

drop table Reservation cascade constraints;
create table Reservation(
	reservation_number varchar2(5) not null,
	cid varchar2(9),
	cost int,
	credit_car_num varchar2(16),
	reservation_date date,
	ticketed varchar2(1),
	start_city varchar2(3),
	end_city varchar2(3),
	constraint reservation_pk primary key (reservation_number)
		deferrable,
	constraint reservation_cid_fk foreign key (cid) references Customer(cid)
		deferrable,
	--constraint reservation_price_limit check (cost >= 0),
	constraint reservation_ticketed_set check (ticketed in ('Y','N'))
);

drop table Reservation_detail cascade constraints;
create table Reservation_detail(
	reservation_number varchar2(5) not null,
	flight_number varchar2(3),
	flight_date date,
	leg int not null,
	constraint res_det_pk primary key (reservation_number, leg)
		deferrable,
	constraint res_det_number_fk foreign key (reservation_number) references Reservation(reservation_number)
		deferrable,
	constraint res_det_flightnum_fk foreign key (flight_number) references Flight(flight_number)
		deferrable,
	constraint res_det_leg_limit check (leg >= 1)
);

drop table SystemDate cascade constraints;
create table SystemDate(
	c_date date not null,
	constraint date_pk primary key (c_date)
		deferrable
);

--given a flight number and date, return the passenger count
CREATE OR REPLACE FUNCTION getNumPassengers (flightNum IN varchar, flightDate IN date)
RETURN int
AS
numPassengers int;
BEGIN
	SELECT count(*) into numPassengers
	FROM reservation_detail
	WHERE flight_number = flightNum AND flight_date = flightDate;
	
	return numPassengers;
END;
/

--given a reservation number, delete all associated reservation_details and move the Flight to a smaller plane if appropriate
CREATE OR REPLACE PROCEDURE deleteAndDownsize(resNum in varchar)
AS
	cursor c_trip_legs is
		select *
		from reservation_detail
		where reservation_number = resNum
		order by leg asc;
	currentLeg reservation_detail%ROWTYPE;
	passengerCount integer;
	currentAirline Airline.airline_id%type;
	new_plane plane.plane_type%type;
BEGIN
	IF NOT c_trip_legs%ISOPEN THEN
		open c_trip_legs;
	END IF;
	LOOP
		FETCH c_trip_legs INTO currentLeg;
		EXIT WHEN c_trip_legs%NOTFOUND;
		--get the number of passengers currently on this flight
		passengerCount := getNumPassengers(currentLeg.flight_number, currentLeg.flight_date);
		--after deletion, count of passengers will be current passengerCount - 1
		passengerCount := passengerCount - 1;
		--get the airline for this flight
		SELECT Airline_id INTO currentAirline FROM Flight WHERE flight_number = currentLeg.flight_number;
		--Find the smallest plane that will be able to hold the new number of passengers
		SELECT plane_type into new_plane
		FROM (SELECT plane_type FROM Plane
			WHERE owner_id = currentAirline AND plane_capacity >= (passengerCount)
			ORDER BY plane_capacity asc)
		WHERE rownum = 1;
		--Update the Flight with the (potentially) new plane
		UPDATE Flight SET plane_type = new_plane WHERE flight_number = currentLeg.flight_number;
		--Delete the current leg from reservation_detail
		DELETE FROM Reservation_detail WHERE reservation_number = currentLeg.reservation_number AND leg = currentLeg.leg;
		
	END LOOP;
END;
/

--assumption for this trigger: c_date is incremented every second, and this trigger will run each time and check
--for reservation_detail entries that are exactly 12 hours away
CREATE OR REPLACE TRIGGER cancelReservation
after update of c_date
on SystemDate
for each row
DECLARE
	cursor c_to_cancel is 
		select reservation_number
		from reservation NATURAL JOIN reservation_detail
		where ticketed = 'N' and
			leg = 1 and --first leg of trip should always occur first, so use as starting time of reservation
			(flight_date - :new.c_date) = 0.5; --date minus a date yields an amount of days and 12h = 0.5 day
			
	cur_reservation reservation.reservation_number%type;
BEGIN
	IF NOT c_to_cancel%ISOPEN THEN
		open c_to_cancel;
	END IF;
	LOOP
		--cancel the current reservation if it's not ticketed
		FETCH c_to_cancel INTO cur_reservation;
		EXIT WHEN c_to_cancel%NOTFOUND;
		--have to delete from reservation_detail first because of integrity constraints
		deleteAndDownsize(cur_reservation);
		delete from reservation
			where reservation_number = cur_reservation;
	END LOOP;
	close c_to_cancel;
END;
/

--This trigger selects the smallest plane that will hold all passengers whenever a reservation_detail entry is inserted
CREATE OR REPLACE TRIGGER planeUpgrade
before insert
on reservation_detail
for each row
DECLARE
	airlineId Airline.airline_id%type;
	new_plane plane.plane_type%type;
	numPassengers int;
	resDetData reservation_detail%rowtype;
	flightNum reservation_detail.flight_number%type;
	flightDate reservation_detail.flight_date%type;
BEGIN
	--set up variables so trigger works with both insertion and delete
	--IF INSERTING THEN
		flightNum := :new.flight_number;
		flightDate := :new.flight_date;
	--ELSE --DELETING
	--	flightNum := :old.flight_number;
	--	flightDate := :old.flight_date;
	--END IF;
	--Get the airline for the new reservation
	SELECT airline_id into airlineId
			FROM Flight F
			WHERE F.flight_number = flightNum;
	--get the new number of passengers on that flight
	--ASSUMPTION: all legs with the same flight_Number and same flight_date are the same flight
	SELECT count(*) into numPassengers
		FROM reservation_detail R
		WHERE R.flight_number = flightNum AND R.flight_date = flightDate;
	--Retrieve all planes owned by that airline that can hold the current number of passengers, and retrieve the type of the smallest one (order asc by capacity and select rownum = 1)
	SELECT plane_type into new_plane
		FROM (SELECT plane_type FROM Plane
			WHERE owner_id = airlineId AND plane_capacity >= (numPassengers + 1) --passenger hasn't been inserted yet, so actual count is numPassengers + 1s
			ORDER BY plane_capacity asc)
		WHERE rownum = 1;
	--Update the Flight with the (potentially) new plane
	UPDATE Flight SET plane_type = new_plane WHERE flight_number = flightNum;
--THE BELOW DOES NOT WORK
--EXCEPTION
	--WHEN NO_DATA_FOUND THEN
		--failed to find a big enough plane - cancel the new reservation
		--should only arrive here during insertions
		--DELETE FROM reservation_detail WHERE leg = :new.leg AND reservation_number = :new.reservation_number;
END;
/

set serveroutput on format wrapped;
create or replace trigger adjustTicket
	after update of low_price, high_price
	on Price
	for each row
	
	DECLARE

	reservation_temp varchar2(5);
	date1 date;
	date2 date;
	leg_max int;
	rn number;

	--Cursor to find non-ticketed flights affected by the price change
	cursor reservations_with_dest
		is 
			select distinct Reservation.reservation_number  from Reservation
			where Reservation.start_city = :new.departure_city
			and Reservation.end_city = :new.arrival_city
			and Reservation.ticketed = 'N';

	BEGIN
	if UPDATING('low_price') then
		open reservations_with_dest;
		LOOP
			fetch reservations_with_dest into reservation_temp;
			EXIT WHEN reservations_with_dest%NOTFOUND;

			--Find the max number of legs. We calculate price on different criteria depending on the number of legs.
			select max(leg) into leg_max from reservation_detail
			where reservation_number = reservation_temp;

			--If the flight is direct, then we check to see if the flight arrival time is before its departure time, indicating a red-eye and thus a low price flight
			if leg_max = 1 THEN	
				select ROWNUM into rn from reservation_detail
				inner join Flight on reservation_detail.flight_number = Flight.flight_number
				where reservation_detail.reservation_number = reservation_temp 
				and reservation_detail.leg = 1 
				and flight.arrival_time < flight.departure_time;

				--If this flight is a low price flight, subtract the old low price and add the new one.
				if rn = 1 then
					update reservation
					set cost = cost-:old.low_price + :new.low_price
					where reservation_number = reservation_temp;
				end if;

			--If the trip has layover, then we check to see whether the flights depart on the same day. Lower priced flights will not depart on the same day.
			ELSIF leg_max >= 2 THEN
				select flight_date into date1 from reservation_detail
				where reservation_detail.reservation_number = reservation_temp 
				and reservation_detail.leg = 1;

				select flight_date  into date2 from reservation_detail
				where reservation_detail.reservation_number = reservation_temp 
				and reservation_detail.leg = 2;

				if date1 <> date2 then
					update reservation
					set cost = cost-:old.low_price + :new.low_price
					where reservation_number = reservation_temp;
				end if;
			end if;
		END LOOP;
		close reservations_with_dest;
	END IF;
	if UPDATING('high_price') then
		open reservations_with_dest;
		LOOP
			fetch reservations_with_dest into reservation_temp;
			EXIT WHEN reservations_with_dest%NOTFOUND;

			select max(leg) into leg_max from reservation_detail
			where reservation_number = reservation_temp;

			--If the flight is direct, then we check to see if the flight if the arrival time is after the departure time, it's a same day flight and thus high price.
			if leg_max = 1 THEN	
				select ROWNUM into rn from reservation_detail
				inner join Flight on reservation_detail.flight_number = Flight.flight_number
				where reservation_detail.reservation_number = reservation_temp 
				and reservation_detail.leg = 1 
				and flight.arrival_time > flight.departure_time;

				if rn = 1 then
					update reservation
					set cost = cost-:old.high_price + :new.high_price
					where reservation_number = reservation_temp;
				end if;

			--If the trip has layover, then we check to see whether the flights depart on the same day. Higher priced flights will  depart on the same day.
			ELSIF leg_max >= 2 THEN
				select flight_date into date1 from reservation_detail
				where reservation_detail.reservation_number = reservation_temp 
				and reservation_detail.leg = 1;

				select flight_date  into date2 from reservation_detail
				where reservation_detail.reservation_number = reservation_temp 
				and reservation_detail.leg = 2;

				if date1 = date2 then
					update reservation
					set cost = cost-:old.high_price + :new.high_price
					where reservation_number = reservation_temp;
				end if;
			end if;
		END LOOP;
		close reservations_with_dest;
	END IF;
	EXCEPTION
	WHEN NO_DATA_FOUND THEN
		dbms_output.put_line('Data not found error in adjustTicket trigger');
	END;
	/
				
--Trigger to set price of a trip given legs (otherwise there is nothing preventing the cost in Reservation from not matching the cost of the flights corresponding to the reservation's legs)
CREATE OR REPLACE TRIGGER setPrice
before insert
on Reservation_detail
for each row
DECLARE
	frequentAirline Customer.frequent_miles%type;
	startCity Reservation.start_city%type;
	endCity Reservation.end_city%type;
	currentFlightNumber Reservation_detail.flight_number%type;
	currentFlight Flight%rowtype;
	totalPrice int := 0;
	currentPrice int := 0;
	isOvernight boolean := false;
	endLoop boolean := false;
	cursor trip is
		SELECT flight_number
		FROM Reservation_detail
		WHERE Reservation_number = :new.Reservation_number
		ORDER BY leg asc;
BEGIN
	--get the frequent airline for the customer (to determine if discount should be applied)
	SELECT frequent_miles INTO frequentAirline
		FROM Customer C JOIN Reservation R ON C.CID = R.CID
		WHERE Reservation_number = :new.Reservation_number;
	--get the start city and end city for the trip
	SELECT start_city, end_city INTO startCity, endCity
		FROM Reservation
		WHERE reservation_number = :new.reservation_number;
		
	IF NOT trip%ISOPEN THEN
		open trip;
	END IF;
	LOOP
		FETCH trip INTO currentFlightNumber;
		--handle the fact that this is a before trigger (after trigger gave me mutating table errors), reservation_detail entry being inserted is not in cursor yet
		IF trip%NOTFOUND AND endLoop = false THEN
			currentFlightNumber := :new.flight_number;
			endLoop := true;
		ELSIF trip%NOTFOUND AND endLoop = true THEN
			EXIT;
		END IF;
		--get the flight for the current leg
		SELECT * INTO currentFlight
			FROM Flight
			WHERE flight_number = currentFlightNumber;
		--if the flight is overnight, the military time for the departure will be greater than that of the arrival, assuming no flight exceeds 24h in length (which seems safe)
		--also note that this comparison works even though they're strings, because the 1 comes before 2, 2 before 3, etc. in "alphabetical" order
		--also assumption: you receive the low_price if any of the legs crosses a day boundary
		IF currentFlight.departure_time > currentFlight.arrival_time THEN
			isOvernight := true;
		END IF;
		IF currentFlight.arrival_city = endCity THEN
			--we've arrived at the destination, time to calculate the price for this direction
			IF isOvernight = true THEN
				--use low price
				SELECT low_price into currentPrice
					FROM Price
					WHERE departure_city = startCity AND arrival_city = endCity; --AND airline_id = currentFlight.airline_id;
			ELSE
				--not overnight - use high price
				SELECT high_price into currentPrice
					FROM Price
					WHERE departure_city = startCity AND arrival_city = endCity; --AND airline_id = currentFlight.airline_id;
			END IF;
			IF currentFlight.airline_id = frequentAirline THEN
				--the customer is flying on their frequent airline and receives a 10% discount
				currentPrice := currentPrice * 0.9;
			END IF;
			--add currentPrice to the total
			totalPrice := currentPrice + totalPrice;
			--reset isOvernight in case this is a round trip
			isOvernight := false;
		END IF;
		IF currentFlight.arrival_City = startCity THEN
			--we've arrived home from a round trip
			IF isOvernight = true THEN
				--use low price
				SELECT low_price into currentPrice
					FROM Price
					WHERE departure_city = endCity AND arrival_city = startCity; --AND airline_id = currentFlight.airline_id;
			ELSE
				--not overnight
					SELECT high_price into currentPrice
						FROM Price
						WHERE departure_city = endCity AND arrival_city = startCity; --AND airline_id = currentFlight.airline_id;
			END IF;
			IF currentFlight.airline_id = frequentAirline THEN
				--customer is flying their frequent airline
				currentPrice := currentPrice * 0.9;
			END IF;
			--add the price for the return trip to the total
			totalPrice := currentPrice + totalPrice;			
		END IF;
	END LOOP;
	close trip;

	--done calculating price - set price for the current reservation
	UPDATE Reservation
		SET cost = totalPrice
		WHERE reservation_number = :new.reservation_number;
EXCEPTION
	WHEN NO_DATA_FOUND THEN
		dbms_output.put_line('Error in set price trigger: customer for the reservation_detail may not exist in the db');
END;
/

		
	
	