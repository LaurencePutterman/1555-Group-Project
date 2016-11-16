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
	constraint reservation_price_limit check (cost >= 0),
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
		delete from reservation_detail
			where reservation_number = cur_reservation;
		delete from reservation
			where reservation_number = cur_reservation;
		--IMPORTANT NOTE: changing the plane size is handled by the planeUpgrade trigger, which triggers on each deletion from reservation_detail!
	END LOOP;
	close c_to_cancel;
END;
/

--This trigger selects the smallest plane that will hold all passengers whenever a reservation_detail entry is inserted or deleted
CREATE OR REPLACE TRIGGER planeUpgrade
before insert or delete
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
	IF INSERTING THEN
		flightNum := :new.flight_number;
		flightDate := :new.flight_date;
	ELSE --DELETING
		flightNum := :old.flight_number;
		flightDate := :old.flight_date;
	END IF;
	--Get the airline for the new reservation
	SELECT airline_id into airlineId
			FROM Flight F
			WHERE F.flight_number = flightNum;
	--get the new number of passengers on that flight
	--ASSUMPTION: all legs with the same flight_Number and same flight_date are the same flight
	SELECT count(*) into numPassengers
		FROM reservation_detail R
		WHERE R.flight_number = :new.flight_number AND R.flight_date = flightDate;
	--Retrieve all planes owned by that airline that can hold the current number of passengers, and retrieve the type of the smallest one (order asc by capacity and select rownum = 1)
	SELECT plane_type into new_plane
		FROM Plane
		WHERE owner_id = airlineId AND plane_capacity >= numPassengers and rownum = 1
		ORDER BY plane_capacity asc;
	--Update the Flight with the (potentially) new plane
	UPDATE Flight SET plane_type = new_plane WHERE flight_number = flightNum;
EXCEPTION
	WHEN NO_DATA_FOUND THEN
		--failed to find a big enough plane - cancel the new reservation
		--should only arrive here during insertions
		DELETE FROM reservation_detail WHERE leg = :new.leg AND reservation_number = :new.reservation_number;
END;
/

create or replace trigger adjustTicket
	after update of low_price, high_price
	on Price
	for each row
	
	DECLARE
	flight_temp varchar2(3);
	reservation_temp varchar2(5);
	leg_temp int;
	leg_max int;
	cursor c_flight_low
		is 
			select flight_number from Flight
			where Flight.airline_id = :new.airline_id
			and Flight.departure_city = :new.departure_city
			and Flight.arrival_city = :new.arrival_city
			and Flight.arrival_time < Flight.departure_time;
	cursor c_flight_high
		is 
			select flight_number from Flight
			where Flight.airline_id = :new.airline_id
			and Flight.departure_city = :new.departure_city
			and Flight.arrival_city = :new.arrival_city
			and Flight.arrival_time > Flight.departure_time;
	
	cursor c_reservation_num_leg (flight_num in varchar2)
		is
			select reservation_number,leg from reservation_detail
			where flight_number = flight_num;
	
	begin
	if :new.high_price != :old.high_price then
		open c_flight_high;	
		LOOP
			fetch c_flight_high into flight_temp;
			open c_reservation_num_leg(flight_temp);
			LOOP
				fetch c_reservation_num_leg into reservation_temp, leg_temp;
				
				select max(leg) into leg_max
				from Reservation_detail
				where reservation_number = reservation_temp
				group by reservation_number;
				
				if leg_temp = 0 or leg_temp = leg_max then
					update reservation
					set cost = cost-:old.high_price + :new.high_price
					where reservation_number = reservation_temp;
				end if;
			END LOOP;
			close c_reservation_num_leg;
		END LOOP;
		close c_flight_high;
	END IF;	
				
			
	
	if :new.low_price != :old.low_price then
		open c_flight_low;
		LOOP
			fetch c_flight_low into flight_temp;
			open c_reservation_num_leg(flight_temp);
			LOOP
				fetch c_reservation_num_leg into reservation_temp, leg_temp;
				
				select max(leg) into leg_max
				from Reservation_detail
				where reservation_number = reservation_temp
				group by reservation_number;
				
				if leg_temp = 0 or leg_temp = leg_max then
					update reservation
					set cost = cost-:old.low_price + :new.low_price
					where reservation_number = reservation_temp;
				end if;
			END LOOP;
			close c_reservation_num_leg;
		END LOOP;
		close c_flight_low;
	END IF;
	END;
	/
				
		
		
	
	