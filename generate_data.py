import random
import string

airline_tups = {}
plane_tups = {}
customer_tups = {}
reservation_tups = {}
reservation_details = {}
flight_tups = {}
price = {}

def randomStringGenerator(length):
	return ''.join(random.choice(string.lowercase) for x in range(length))

def randomDate(startYear = 1900):
	m = random.randint(1,12)
	d = random.randint(1,30)
	y = random.randint(startYear,2016)
	return str(m).zfill(2)+"-"+str(d).zfill(2)+"-"+str(y)

def dateAfterGivenDate(date):
	dArr = date.split("-")
	m = int(dArr[0])
	d = int(dArr[1])
	if d < 30:
		d += 1
	else:
		d = 1
		m += 1
	if m>12:
		m = 1
	y = dArr[2]
	return str(m).zfill(2)+"-"+str(d).zfill(2)+"-"+str(y)

def dateToSql(date):
	return "to_date('"+date+"', 'DD-MM-YYYY')"

def weekly_scheduleGenerator():
	letters = ["S","M","T","W","T","F","S"]
	schedule = ""
	for x in letters:
		r = random.randint(0,100)
		if r > 25:
			schedule += x
		else:
			schedule += "-"
	return schedule
def generateAirline(num = 10):
	global airline_tups

	for x in range(num):
		airline_tups[x] = (x,"'"+randomStringGenerator(50)+"'","'"+randomStringGenerator(10)+"'",random.randint(1900, 2016))
	return airline_tups

def generatePlane(num = 30):
	global plane_tups

	manufacturers = ["Airbus","Boeing", "Lockheed", "Embraer"]
	for x in range(num):
		m = random.choice(manufacturers)
		p_type = "'"+m[0]+str(random.randint(0,999)).zfill(3)+"'"
		while p_type in plane_tups:
			p_type = "'"+m[0]+str(random.randint(0,999)).zfill(3)+"'"

		plane_tups[p_type] = (p_type,"'"+m+"'",random.randint(10,200),dateToSql(randomDate()),random.randint(1900, 2016),random.choice(airline_tups.keys()))
	return plane_tups

def generateFlight(num = 100):
	global airline_tups
	global plane_tups
	global price
	global flight_tups

	airports = ["ATL","LAX","ORD","DFW","JFK","DEN","SFO","CLT","LAS","PHX","MIA","IAH","SEA","MCO","EWR","MSP","BOS","DTW","PHL","LGA","FLL","BWI","DCA", "PIT"]
	for x in range(num):
		fn = "'"+str(x)+"'"
		air_id = random.choice(airline_tups.keys())
		p_type = random.choice(plane_tups.keys())
		departure_city = "'"+random.choice(airports)+"'"
		arrival_city = "'"+random.choice(airports)+"'"
		departure_time = random.randint(0,2359)
		arrival_time = random.randint(0,2359)
		weekly_schedule = "'"+weekly_scheduleGenerator()+"'"
		while arrival_time == departure_time:
			arrival_time = random.randint(0,2359)
		while departure_city == arrival_city or (departure_city,arrival_city) in price:
			arrival_city = "'"+random.choice(airports)+"'"

		flight_tups[fn] = (fn,air_id,p_type,departure_city,arrival_city,departure_time,arrival_time,weekly_schedule)

		price[(departure_city,arrival_city)] = (departure_city,arrival_city,air_id,random.randint(500, 1000), random.randint(100,499))


	return flight_tups


def generateCustomer(num = 200):
	global airline_tups
	global customer_tups

	salutations = ["Mr","Mrs","Ms"]
	first_names = ["Aaron","Bill","Bob","Richard","Robert","Laurence","Tim","George","Sam","Emily","Tiffany","Mackenzie","Victoria","Jessie","Jessica","Kylie"]
	last_names = ["Smith","White","Black","Keeton","Brown","Fox","Swift","Eastwood"]

	for x in range(num):
		cid = "'"+str(x)+"'"
		salutation = "'"+random.choice(salutations)+"'"
		first_name = "'"+random.choice(first_names)+"'"
		last_name = "'"+random.choice(last_names)+"'"
		credit_card_num = "'"+str(random.randint(1111111111111111,9999999999999999))+"'"
		credit_card_expire = dateToSql(randomDate())
		street = "'"+randomStringGenerator(10)+" Road'"
		city = "'"+randomStringGenerator(10)+"'"
		state = "'"+randomStringGenerator(2)+"'"
		phone = "'"+str(random.randint(1111111111,9999999999))+"'"
		email = "'"+first_name+last_name+"@gmail.com'"
		frequent_miles = random.choice(airline_tups.keys())

		customer_tups[cid] = (cid,salutation,first_name,last_name,credit_card_num,credit_card_expire,street,city,state,phone,email,frequent_miles)

	return customer_tups


def generateReservations(num = 300):
	global flight_tups
	global reservation_tups
	global reservation_details

	ticketed = ["'Y'","'N'"]
	for x in range(num):
		reservation_number = "'"+str(x)+"'"
		num_legs = random.randint(1,5)
		cid = random.choice(customer_tups.keys())
		credit_card = customer_tups[cid][4]
		flights = []
		date = randomDate()
		flight_date = date
		cost = 0
		ticketSelection = random.choice(ticketed)
		for l in range(num_legs):

			flight = random.choice(flight_tups.keys())
			while flight in flights:
				flight = random.choice(flight_tups.keys())
			flights.append(flight)
			flight_date = dateAfterGivenDate(flight_date)
			reservation_details[(reservation_number,l)] = (reservation_number,flight,dateToSql(flight_date),l)
			flight_info = flight_tups[flight]
			price_info = price[(flight_info[3],flight_info[4])]
			if flight_info[5] > flight_info[6]:
				cost += price_info[3]
			else:
				cost += price_info[4]

		reservation_tups[reservation_number] = (reservation_number,cid,cost,credit_card,dateToSql(date),ticketSelection)

def exportTupsToSql(d,tableName):
	returnString = ""
	for x in d:
		returnString += "INSERT INTO "+tableName+" VALUES ("+','.join(map(str, d[x]))+");\n"
	return returnString

if __name__ == "__main__":


	generateAirline()
	generatePlane()
	generateFlight()
	generateCustomer()
	generateReservations()

	with open('sample_data.sql', 'a') as the_file:
	    the_file.write(exportTupsToSql(airline_tups,"Airline")+"\n\n")
	    the_file.write(exportTupsToSql(plane_tups,"Plane")+"\n\n")
	    the_file.write(exportTupsToSql(flight_tups,"Flight")+"\n\n")
	    the_file.write(exportTupsToSql(price,"Price")+"\n\n")
	    the_file.write(exportTupsToSql(customer_tups,"Customer")+"\n\n")
	    the_file.write(exportTupsToSql(reservation_tups,"Reservation")+"\n\n")
	    the_file.write(exportTupsToSql(reservation_details,"Reservation_detail")+"\n\n")































