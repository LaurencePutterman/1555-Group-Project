import random
import string
from datetime import datetime

airline_tups = {}
plane_tups = {}
customer_tups = {}
reservation_tups = {}
reservation_details = {}
flight_tups = {}
price = {}

def randomStringGenerator(length):
	return ''.join(random.choice(string.lowercase) for x in range(length))

def randomDate(startYear = 2000):
	m = random.randint(1,12)
	if m == 2:
		d = random.randint(1,28)
	else:
		d = random.randint(1,30)
	y = random.randint(startYear,2016)
	return str(m).zfill(2)+"-"+str(d).zfill(2)+"-"+str(y)

def dateAfterGivenDate(date):
	dArr = date.split("-")
	m = int(dArr[0])
	d = int(dArr[1])
	y = int(dArr[2])
	if m == 2 and d == 28:
		m += 1
		d = 1
	if d < 30 or (m == 2 and d < 28):
		d += 1
	else:
		d = 1
		if m >= 12:
			m = 1
			y += 1
		else:
			m += 1
	return str(m).zfill(2)+"-"+str(d).zfill(2)+"-"+str(y)

def dateToSql(date):
	return "to_date('"+date+"', 'MM-DD-YYYY')"

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
	airline_words = ["United","Continental","Service","Air","Airline","American","Delta","South","North","East","West","Northeast","Northwest","Southeast","Southwest","Airways","Jet","Spirit","Frontier","Incredible","Discount"]
	for x in range(1,num+1):
		num_words = random.randint(2,5)
		airline_name_arr = []
		airline_abbreviation = ""
		for i in range(num_words):
			temp = random.choice(airline_words)
			airline_name_arr.append(temp)
			airline_abbreviation += temp[0]
		airline_tups[x] = (str(x),"'"+" ".join(airline_name_arr)+"'","'"+airline_abbreviation+"'",random.randint(1900, 2016))
	return airline_tups

def generatePlane(num = 30):
	global plane_tups

	manufacturers = ["Airbus","Boeing", "Lockheed", "Embraer"]
	for x in range(1,num+1):
		m = random.choice(manufacturers)
		p_type = "'"+m[0]+str(random.randint(0,999)).zfill(3)+"'"
		owner_id = random.choice(airline_tups.keys())
		while p_type in plane_tups:
			p_type = "'"+m[0]+str(random.randint(0,999)).zfill(3)+"'"

		plane_tups[(p_type,owner_id)] = (p_type,"'"+m+"'",random.randint(10,200),dateToSql(randomDate()),random.randint(1900, 2016),owner_id)
	return plane_tups

def generateFlight(num = 106):
	global airline_tups
	global plane_tups
	global price
	global flight_tups

	airports = ["ATL","LAX","ORD","DFW","JFK","DEN","SFO","CLT","LAS","PHX","MIA","IAH","SEA","MCO","EWR","MSP","BOS","DTW","PHL","LGA","FLL","BWI","DCA", "PIT"]
	for x in range(1,num+1,6):
		i = x
		#flight there
		constant_air_id = ""
		fn = "'"+str(i)+"'"
		plane = random.choice(plane_tups.keys())
		p_type = plane[0]
		air_id = str(plane[1])
		constant_air_id = air_id
		departure_city = "'"+random.choice(airports)+"'"
		arrival_city = "'"+random.choice(airports)+"'"
		departure_time = random.randint(0,2359)
		arrival_time = random.randint(0,2359)
		weekly_schedule = "'"+weekly_scheduleGenerator()+"'"
		while arrival_time == departure_time:
			arrival_time = random.randint(0,2359)
		while departure_city == arrival_city or (departure_city,arrival_city) in price:
			arrival_city = "'"+random.choice(airports)+"'"

		flight_tups[fn] = (fn,"'"+air_id+"'",p_type,departure_city,arrival_city,departure_time,arrival_time,weekly_schedule)

		price[(departure_city,arrival_city)] = (departure_city,arrival_city,"'"+air_id+"'",random.randint(500, 1000), random.randint(100,499))

		#make sure a connection exists
		i += 1
		fn = "'"+str(i)+"'"
		plane = random.choice(plane_tups.keys())
		while str(plane[1]) != constant_air_id:
			plane = random.choice(plane_tups.keys())
		p_type = plane[0]
		air_id = str(plane[1])
		departure_city_connect1 = departure_city
		arrival_city_connect1 = "'"+random.choice(airports)+"'"
		departure_time = random.randint(0,2359)
		arrival_time = random.randint(0,2359)
		weekly_schedule = "'"+weekly_scheduleGenerator()+"'"
		while arrival_time == departure_time:
			arrival_time = random.randint(0,2359)
		while departure_city_connect1 == arrival_city_connect1 or (departure_city_connect1,arrival_city_connect1) in price:
			arrival_city_connect1 = "'"+random.choice(airports)+"'"

		flight_tups[fn] = (fn,"'"+air_id+"'",p_type,departure_city_connect1,arrival_city_connect1,departure_time,arrival_time,weekly_schedule)

		price[(departure_city_connect1,arrival_city_connect1)] = (departure_city_connect1,arrival_city_connect1,"'"+air_id+"'",random.randint(500, 1000), random.randint(100,499))

		#connection 2
		i += 1
		fn = "'"+str(i)+"'"
		plane = random.choice(plane_tups.keys())
		while str(plane[1]) != constant_air_id:
			plane = random.choice(plane_tups.keys())
		p_type = plane[0]
		air_id = str(plane[1])
		departure_city_connect2 = arrival_city_connect1
		arrival_city_connect2 = arrival_city
		departure_time = random.randint(0,2359)
		arrival_time = random.randint(0,2359)
		weekly_schedule = "'"+weekly_scheduleGenerator()+"'"
		while arrival_time == departure_time:
			arrival_time = random.randint(0,2359)

		flight_tups[fn] = (fn,"'"+air_id+"'",p_type,departure_city_connect2,arrival_city_connect2,departure_time,arrival_time,weekly_schedule)

		price[(departure_city_connect2,arrival_city_connect2)] = (departure_city_connect2,arrival_city_connect2,"'"+air_id+"'",random.randint(500, 1000), random.randint(100,499))


		#flight back
		i += 1
		fn = "'"+str(i)+"'"
		plane = random.choice(plane_tups.keys())
		# while plane[1] != air_id:
		# 	plane = random.choice(plane_tups.keys())
		p_type = plane[0]
		air_id = str(plane[1])
		departure_city_back = arrival_city
		arrival_city_back = departure_city
		departure_time = random.randint(0,2359)
		arrival_time = random.randint(0,2359)
		weekly_schedule = "'"+weekly_scheduleGenerator()+"'"
		while arrival_time == departure_time:
			arrival_time = random.randint(0,2359)

		flight_tups[fn] = (fn,"'"+air_id+"'",p_type,departure_city_back,arrival_city_back,departure_time,arrival_time,weekly_schedule)

		price[(departure_city_back,arrival_city_back)] = (departure_city_back,arrival_city_back,"'"+air_id+"'",random.randint(500, 1000), random.randint(100,499))

		#make sure a connection exists
		i += 1
		fn = "'"+str(i)+"'"
		plane = random.choice(plane_tups.keys())
		while str(plane[1]) != constant_air_id:
			plane = random.choice(plane_tups.keys())
		p_type = plane[0]
		air_id = str(plane[1])
		departure_city_connect1 = departure_city_back
		arrival_city_connect1 = departure_city_connect2
		departure_time = random.randint(0,2359)
		arrival_time = random.randint(0,2359)
		weekly_schedule = "'"+weekly_scheduleGenerator()+"'"
		while arrival_time == departure_time:
			arrival_time = random.randint(0,2359)

		flight_tups[fn] = (fn,"'"+air_id+"'",p_type,departure_city_connect1,arrival_city_connect1,departure_time,arrival_time,weekly_schedule)

		price[(departure_city_connect1,arrival_city_connect1)] = (departure_city_connect1,arrival_city_connect1,"'"+air_id+"'",random.randint(500, 1000), random.randint(100,499))

		#connection 2
		i += 1
		fn = "'"+str(i)+"'"
		plane = random.choice(plane_tups.keys())
		while str(plane[1]) != constant_air_id:
			plane = random.choice(plane_tups.keys())
		p_type = plane[0]
		air_id = str(plane[1])
		departure_city_connect2 = arrival_city_connect1
		arrival_city_connect2 = departure_city
		departure_time = random.randint(0,2359)
		arrival_time = random.randint(0,2359)
		weekly_schedule = "'"+weekly_scheduleGenerator()+"'"
		while arrival_time == departure_time:
			arrival_time = random.randint(0,2359)
			
		flight_tups[fn] = (fn,"'"+air_id+"'",p_type,departure_city_connect2,arrival_city_connect2,departure_time,arrival_time,weekly_schedule)

		price[(departure_city_connect2,arrival_city_connect2)] = (departure_city_connect2,arrival_city_connect2,"'"+air_id+"'",random.randint(500, 1000), random.randint(100,499))

	return flight_tups


def generateCustomer(num = 200):
	global airline_tups
	global customer_tups

	salutations = ["Mr","Mrs","Ms"]
	first_names = ["Aaron","Bill","Bob","Richard","Robert","Laurence","Tim","George","Sam","Emily","Tiffany","Mackenzie","Victoria","Jessie","Jessica","Kylie"]
	last_names = ["Smith","White","Black","Keeton","Brown","Fox","Swift","Eastwood"]

	for x in range(1,num+1):
		cid = "'"+str(x)+"'"
		salutation = "'"+random.choice(salutations)+"'"
		first_name = random.choice(first_names)
		last_name = random.choice(last_names)
		email = "'"+first_name+last_name+"@gmail.com'"
		first_name = "'"+first_name+"'"
		last_name = "'"+last_name+"'"
		credit_card_num = "'"+str(random.randint(1111111111111111,9999999999999999))+"'"
		credit_card_expire = dateToSql(randomDate())
		street = "'"+randomStringGenerator(10)+" Road'"
		city = "'"+randomStringGenerator(10)+"'"
		state = "'"+randomStringGenerator(2)+"'"
		phone = "'"+str(random.randint(1111111111,9999999999))+"'"
		frequent_miles = random.choice(airline_tups.keys())

		customer_tups[cid] = (cid,salutation,first_name,last_name,credit_card_num,credit_card_expire,street,city,state,phone,email,frequent_miles)

	return customer_tups

def reservationsHelper(reservation_number,flight,flight_info,start_city,end_city,resdate,l=1):

	price_info = price[(start_city,end_city)]
	legs = random.randint(1,2)
	if flight_info[5] > flight_info[6]:
		cost = price_info[3]
	else:
		cost = price_info[4]
	if legs == 1:
		flight_date = dateAfterGivenDate(resdate)
		d = datetime(1900,1,1)
		try:
			d = datetime.strptime(flight_date, '%m-%d-%Y').date()
		except:
			print(flight_date)
		
		while flight_info[7][d.weekday()] == "-":
			flight_date = dateAfterGivenDate(flight_date)
			d = datetime.strptime(flight_date, '%m-%d-%Y').date()
		
		reservation_details[(reservation_number,l)] = (reservation_number,flight,dateToSql(flight_date),l)
		flight_date2 = flight_date
		l += 1
	else:
		for p1 in price:
			for p2 in price:
				if p1[0]==start_city and p2[1]==end_city and p1[1] == p2[0]:
					price1 = p1
					price2 = p2
					break
		for f in flight_tups:
			connect1 = f
			if price1[0] == flight_tups[connect1][3] and price1[1] == flight_tups[connect1][4]:
				break
		for f in flight_tups:
			connect2 = f
			if price2[0] == flight_tups[connect2][3] and price2[1] == flight_tups[connect2][4]:
				break

		flight_date1 = dateAfterGivenDate(resdate)
		d = datetime.strptime(flight_date1, '%m-%d-%Y').date()
		
		while flight_tups[connect1][7][d.weekday()] == "-":
			flight_date1 = dateAfterGivenDate(flight_date1)
			d = datetime.strptime(flight_date1, '%m-%d-%Y').date()
		
		flight_date2 =  flight_date1
		d = datetime.strptime(flight_date2, '%m-%d-%Y').date()

		if flight_tups[connect2][7][d.weekday()] == "-" or flight_tups[connect2][5] > flight_tups[connect1][6]:
			while flight_tups[connect2][7][d.weekday()] == "-":
				flight_date2 = dateAfterGivenDate(flight_date2)
				d = datetime.strptime(flight_date2, '%m-%d-%Y').date()
		

		reservation_details[(reservation_number,l)] = (reservation_number,connect1,dateToSql(flight_date1),l)
		l += 1
		reservation_details[(reservation_number,l)] = (reservation_number,connect2,dateToSql(flight_date2),l)
		l += 1
	return (cost,l,flight_date2)



def generateReservations(num = 300):
	global flight_tups
	global reservation_tups
	global reservation_details

	ticketed = ["'Y'","'N'"]
	for x in range(1,num+1):
		reservation_number = "'"+str(x)+"'"
		cid = random.choice(customer_tups.keys())
		credit_card = customer_tups[cid][4]
		flights = []
		resdate = randomDate()
		cost = 0
		ticketSelection = random.choice(ticketed)
		oneWayOrRoundTripSelect = random.randint(1,100)
		roundTrip = False
		flight = random.choice(flight_tups.keys())
		flight_info = flight_tups[flight]
		start_city = flight_info[3]
		end_city = flight_info[4]

		results = reservationsHelper(reservation_number,flight,flight_info,start_city,end_city,resdate)
		cost += results[0]
		if oneWayOrRoundTripSelect > 50: #round trip
			for f in flight_tups:
				return_flight_info = flight_tups[f]
				flight = f
				if start_city == return_flight_info[4] and end_city == return_flight_info[3]:
					break
			results2 = reservationsHelper(reservation_number,flight,return_flight_info,end_city,start_city,results[2],results[1])
			cost += results2[0]

		reservation_tups[reservation_number] = (reservation_number,cid,cost,credit_card,dateToSql(resdate),ticketSelection,start_city,end_city)

def exportTupsToSql(d,tableName):
	returnString = ""
	for x in d:
		returnString += "INSERT INTO "+tableName+" VALUES ("+','.join(map(str, d[x]))+");\n"
	return returnString
def exportTupsToCSV(d):
	returnString = ""
	for x in d:
		returnString += ",".join(map(str,d[x])) +"\n"
	return returnString
if __name__ == "__main__":

	for i in range(10):
		airline_tups = {}
		plane_tups = {}
		customer_tups = {}
		reservation_tups = {}
		reservation_details = {}
		flight_tups = {}
		price = {}
		generateAirline()
		generatePlane()
		generateFlight()
		generateCustomer()
		generateReservations()

		# with open('sample_data.sql', 'a') as the_file:
		#     the_file.write(exportTupsToSql(airline_tups,"Airline")+"\ncommit;\n")
		#     the_file.write(exportTupsToSql(plane_tups,"Plane")+"\ncommit;\n")
		#     the_file.write(exportTupsToSql(flight_tups,"Flight")+"\ncommit;\n")
		#     the_file.write(exportTupsToSql(price,"Price")+"\ncommit;\n")
		#     the_file.write(exportTupsToSql(customer_tups,"Customer")+"\ncommit;\n")
		#     the_file.write(exportTupsToSql(reservation_tups,"Reservation")+"\ncommit;\n")
		#     the_file.write(exportTupsToSql(reservation_details,"Reservation_detail")+"\ncommit;\n")


		with open('airline_information-'+str(i)+'.csv', 'a') as the_file:
			the_file.write(exportTupsToCSV(airline_tups))

		with open('schedule_information-'+str(i)+'.csv', 'a') as the_file:
			the_file.write(exportTupsToCSV(flight_tups))

		with open('pricing_information-'+str(i)+'.csv', 'a') as the_file:
			the_file.write(exportTupsToCSV(price))

		with open('plane_information-'+str(i)+'.csv', 'a') as the_file:
			the_file.write(exportTupsToCSV(plane_tups))
































